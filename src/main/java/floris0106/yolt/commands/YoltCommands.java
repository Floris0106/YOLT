package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.*;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import floris0106.yolt.Yolt;
import floris0106.yolt.util.Language;

public class YoltCommands
{
	public static void register()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
		{
			dispatcher.register(
				literal(Yolt.MOD_ID)
					.executes(context ->
					{
						context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.success",
								Component.literal("Twice").withStyle(ChatFormatting.AQUA),
								Component.literal("YOLT").withStyle(ChatFormatting.BOLD),
								Component.literal("Floris0106").withStyle(ChatFormatting.GOLD)
							), false);
						return 1;
					})
					.then(RoleCommand.register())
					.then(ResetCommand.register())
					.then(LivesCommand.register())
                    .then(HealthCommand.register())
					.then(DebugCommand.register())
					.then(ConfigCommand.register())
					.then(ClaimCommand.register())
			);
		});
	}
}