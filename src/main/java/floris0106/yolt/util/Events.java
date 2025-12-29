package floris0106.yolt.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
	private static int tickCounter = 0;

	public static void register()
	{
		ServerTickEvents.START_SERVER_TICK.register(Events::onStartServerTick);
		UseItemCallback.EVENT.register(Events::onPlayerUseItem);
	}

	private static void onStartServerTick(MinecraftServer server)
	{
		ServerLevel overworld = server.overworld();
		if (overworld.getDayTime() % 24000 != 18000)
			return;

		GameRules gameRules = overworld.getGameRules();

		tickCounter++;
		if (tickCounter > Config.getSleepPercentageDecrementTicks())
		{
			gameRules.set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, Math.max(gameRules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE) - 1, 0), server);
			tickCounter = 0;
		}

		if (gameRules.get(GameRules.ADVANCE_TIME))
			return;

		overworld.getGameRules().set(GameRules.ADVANCE_TIME, false, server);
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
		Component component;
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
		serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(component));

		return InteractionResult.SUCCESS;
	}
}