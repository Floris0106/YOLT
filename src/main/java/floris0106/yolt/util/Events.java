package floris0106.yolt.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gamerules.GameRules;

public class Events
{
	public static void register()
	{
		ServerTickEvents.START_SERVER_TICK.register(Events::onStartServerTick);
	}

	private static void onStartServerTick(MinecraftServer server)
	{
		ServerLevel overworld = server.overworld();
		GameRules gameRules = overworld.getGameRules();
		if (overworld.getDayTime() % 24000 != 18000 || !gameRules.get(GameRules.ADVANCE_TIME))
			return;

		overworld.getGameRules().set(GameRules.ADVANCE_TIME, false, server);
		SoundHelper.broadcast(overworld, SoundHelper.YAWN, 1.0f, Mth.randomBetween(overworld.getRandom(), 0.9f, 1.1f));
	}
}