package floris0106.yolt.util;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
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
import net.minecraft.world.level.gamerules.GameRules;

import floris0106.yolt.config.Config;

public class Events
{
	public static Component EXTRA_NAUGHTY_LIST = Component.literal("EXTRA NAUGHTY LIST").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);

	private static int tickCounter = 0;

	public static void register()
	{
		UseItemCallback.EVENT.register(Events::onPlayerUseItem);
	}

	public static void onTimeTick(ServerLevel overworld)
	{
		MinecraftServer server = overworld.getServer();
		switch ((int) overworld.getDayTime() % 24000)
		{
			case 13000 -> onNightfall(server);
			case 18000 -> onMidnight(server, overworld);
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

	private static void onMidnight(MinecraftServer server, ServerLevel overworld)
	{
		GameRules gameRules = overworld.getGameRules();

		if (server.getPlayerList().getPlayers().stream().anyMatch(Player::isSleepingLongEnough) && ++tickCounter > Config.getSleepPercentageDecrementTicks())
		{
			gameRules.set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, Math.max(gameRules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE) - 1, 0), server);
			tickCounter = 0;
		}

		if (!gameRules.get(GameRules.ADVANCE_TIME))
			return;

		gameRules.set(GameRules.ADVANCE_TIME, false, server);
		SoundHelper.broadcast(overworld, SoundHelper.YAWN, 1.0f, Mth.randomBetween(overworld.getRandom(), 0.9f, 1.1f));
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
		serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(component.append(" (" + distance + " blocks)")));

		return InteractionResult.SUCCESS;
	}
}