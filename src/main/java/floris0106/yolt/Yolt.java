package floris0106.yolt;

import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;

import floris0106.yolt.commands.YoltCommands;
import floris0106.yolt.config.Config;
import floris0106.yolt.util.Events;

public class Yolt implements ModInitializer
{
	public static final String MOD_ID = "yolt";
	public static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize()
	{
		Config.load();
		Events.register();
		YoltCommands.register();

		LOGGER.info("{} initialized", MOD_ID.toUpperCase());
	}
}