package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.*;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.Objects;

import floris0106.yolt.util.Language;
import floris0106.yolt.util.Role;
import floris0106.yolt.util.ServerPlayerExtension;

public class ClaimCommand
{
	public static ArgumentBuilder<CommandSourceStack, ?> register()
	{
		return literal("claim")
			.executes(ClaimCommand::claimKill);
	}

	private static int claimKill(CommandContext<CommandSourceStack> context)
	{
		ServerPlayer player = Objects.requireNonNull(context.getSource().getPlayer());
		ServerPlayerExtension extension = (ServerPlayerExtension) player;
		if (extension.yolt$getRole() != Role.HUNTER)
		{
			context.getSource().sendFailure(Language.translatable("commands.yolt.claim.not_hunter"));
			return 0;
		}

		PlayerList playerList = context.getSource().getServer().getPlayerList();

		ServerPlayer victim = null;
		for (ServerPlayer other : playerList.getPlayers())
			if (((ServerPlayerExtension) other).yolt$getRole() == Role.VICTIM)
				victim = other;

		((ServerPlayerExtension) Objects.requireNonNull(victim)).yolt$setRole(Role.NEUTRAL);
		extension.yolt$setRole(Role.NEUTRAL);

		playerList.broadcastSystemMessage(Language.translatable("commands.yolt.claim.success", player.getDisplayName(), Objects.requireNonNull(victim).getDisplayName()), false);

		return 1;
	}
}