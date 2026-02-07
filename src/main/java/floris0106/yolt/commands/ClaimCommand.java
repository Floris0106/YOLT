package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.*;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;

import floris0106.yolt.util.Language;
import floris0106.yolt.util.Role;
import floris0106.yolt.util.ServerPlayerExtension;

public class ClaimCommand
{
	public static ArgumentBuilder<CommandSourceStack, ?> register()
	{
		return literal("claim")
			.then(argument("player", EntityArgument.player())
				.executes(ClaimCommand::claimKill));
	}

	private static int claimKill(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
		ServerPlayer hunter = Objects.requireNonNull(context.getSource().getPlayer());
		ServerPlayer victim = EntityArgument.getPlayer(context, "player");

		ServerPlayerExtension hunterExtension = (ServerPlayerExtension) hunter;
		ServerPlayerExtension victimExtension = (ServerPlayerExtension) victim;

		PlayerList playerList = context.getSource().getServer().getPlayerList();
		if (victimExtension.yolt$getRole() == Role.EXTRA_NAUGHTY)
		{
			hunter.addItem(new ItemStack(Items.GOLDEN_APPLE));
			victimExtension.yolt$setRole(Role.NEUTRAL);

			playerList.broadcastSystemMessage(Language.translatable("commands.yolt.claim.extra_naughty.success", hunter.getDisplayName(), Component.literal("EXTRA NAUGHTY").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), victim.getDisplayName()), false);
		}
		else if (hunterExtension.yolt$getRole() != Role.HUNTER)
		{
			context.getSource().sendFailure(Language.translatable("commands.yolt.claim.victim.not_hunter"));
			return 0;
		}
		else if (victimExtension.yolt$getRole() != Role.VICTIM)
		{
			context.getSource().sendFailure(Language.translatable("commands.yolt.claim.victim.not_victim", victim.getDisplayName()));
			return 0;
		}

		victimExtension.yolt$setRole(Role.NEUTRAL);
		hunterExtension.yolt$setRole(Role.NEUTRAL);

		playerList.broadcastSystemMessage(Language.translatable("commands.yolt.claim.victim.success", hunter.getDisplayName(), victim.getDisplayName()), false);

		return 1;
	}
}