package floris0106.yolt.util;

import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Events
{
	public static final Component EXTRA_NAUGHTY_LIST = Component.literal("EXTRA NAUGHTY LIST").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);

	private static final PriorityQueue<IntObjectImmutablePair<Runnable>> SCHEDULED_TASKS = new PriorityQueue<>(Comparator.comparing(IntObjectImmutablePair::leftInt));

	public static void register()
	{
		UseItemCallback.EVENT.register(Events::onPlayerUseItem);
		ServerTickEvents.END_SERVER_TICK.register(Events::onEndTick);
	}

	public static void schedule(int tick, Runnable task)
	{
		SCHEDULED_TASKS.add(new IntObjectImmutablePair<>(tick, task));
	}

	public static void onTimeTick(ServerLevel overworld)
	{
		MinecraftServer server = overworld.getServer();
		switch ((int) overworld.getDayTime() % 24000)
		{
			case 13000 -> onNightfall(server);
			case 18000 -> onMidnight(overworld);
			case 18101 ->
			{
				ServerPlayer victim = null;
				ServerPlayer hunter = null;
				for (ServerPlayer player : server.getPlayerList().getPlayers())
					switch (((ServerPlayerExtension) player).yolt$getRole())
					{
						case VICTIM -> victim = player;
						case HUNTER -> hunter = player;
					}
				if (victim != null && hunter != null)
				{
					hunter.sendSystemMessage(Language.translatable("event.yolt.santa.grudge.3", victim.getDisplayName()).withStyle(ChatFormatting.GRAY));
					hunter.sendSystemMessage(Language.translatable("event.yolt.santa.grudge.4", Events.EXTRA_NAUGHTY_LIST).withStyle(ChatFormatting.GRAY));
					hunter.sendSystemMessage(Language.translatable("event.yolt.santa.grudge.5").withStyle(ChatFormatting.GRAY));
				}
			}
		}
	}

	private static void onNightfall(MinecraftServer server)
	{
		PlayerList playerList = server.getPlayerList();

		ServerPlayer victim = null;
		ServerPlayer hunter = null;
		for (ServerPlayer player : playerList.getPlayers())
		{
			ServerPlayerExtension extension = ((ServerPlayerExtension) player);
			switch (extension.yolt$getRole())
			{
				case VICTIM -> victim = player;
				case HUNTER -> hunter = player;
				case EXTRA_NAUGHTY ->
				{
					extension.yolt$setRole(Role.NEUTRAL);
					playerList.broadcastSystemMessage(Language.translatable("event.yolt.forgiven", player.getDisplayName(), EXTRA_NAUGHTY_LIST), false);
				}
			}
		}
		if (victim != null && hunter != null)
		{
			((ServerPlayerExtension) victim).yolt$setRole(Role.NEUTRAL);
			((ServerPlayerExtension) hunter).yolt$setRole(Role.EXTRA_NAUGHTY);

			playerList.broadcastSystemMessage(Language.translatable("event.yolt.hunter_fail", hunter.getDisplayName(), EXTRA_NAUGHTY_LIST), false);
			hunter.sendSystemMessage(Language.translatable("event.yolt.hunter_fail.clarification", EXTRA_NAUGHTY_LIST, victim.getDisplayName()).withStyle(ChatFormatting.GRAY));
		}
	}

	private static void onMidnight(ServerLevel overworld)
	{
		SoundHelper.broadcast(overworld, SoundHelper.YAWN, 1.0f, Mth.randomBetween(overworld.getRandom(), 0.9f, 1.1f));
		overworld.setDayTime(18001);

		List<ServerPlayer> players = overworld.getServer().getPlayerList().getPlayers();
		ServerPlayer victim = players.stream().filter(player -> ((ServerPlayerExtension) player).yolt$getLives() > 1).findAny().orElse(null);
		ServerPlayer hunter = players.stream().filter(player -> player != victim && ((ServerPlayerExtension) player).yolt$getRole() == Role.NEUTRAL).findAny().orElse(null);
		if (victim != null && hunter != null)
		{
			((ServerPlayerExtension) victim).yolt$setRole(Role.VICTIM);
			((ServerPlayerExtension) hunter).yolt$setRole(Role.HUNTER);

			hunter.connection.send(new ClientboundSetTitleTextPacket(Language.translatable("event.yolt.santa.grudge.1").withStyle(ChatFormatting.AQUA)));
			hunter.connection.send(new ClientboundSetSubtitleTextPacket(Language.translatable("event.yolt.santa.grudge.2")));
		}
	}

	private static InteractionResult onPlayerUseItem(Player player, Level level, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(Items.OMINOUS_TRIAL_KEY) ||
			!player.isShiftKeyDown() ||
			!(player instanceof ServerPlayer serverPlayer))
			return InteractionResult.PASS;

		double distance = ((ServerLevelExtension) level).yolt$getPresentDistance(player.position());
		MutableComponent component;
		if (distance > 64.0)
			component = Component.literal("Freezing cold").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
		else if (distance > 32.0)
			component = Component.literal("Cold").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
		else if (distance > 16.0)
			component = Component.literal("Warm").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
		else if (distance > 8.0)
			component = Component.literal("Hot").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
		else
			component = Component.literal("Burning hot").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
		serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(component.append(" (" + Math.round(distance) + " blocks)")));

		return InteractionResult.SUCCESS;
	}

	private static void onEndTick(MinecraftServer server)
	{
		while (true)
		{
			IntObjectImmutablePair<Runnable> pair = SCHEDULED_TASKS.peek();
			if (pair == null || pair.leftInt() > server.getTickCount())
				break;

			pair.right().run();
			SCHEDULED_TASKS.poll();
		}
	}
}