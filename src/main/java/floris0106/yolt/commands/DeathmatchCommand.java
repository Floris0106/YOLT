package floris0106.yolt.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import floris0106.yolt.util.Events;
import floris0106.yolt.util.Language;
import floris0106.yolt.util.SoundHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.border.WorldBorder;

import static net.minecraft.commands.Commands.*;

public class DeathmatchCommand
{
    public static ArgumentBuilder<CommandSourceStack, ?> register()
    {
        return literal("deathmatch")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
            .then(literal("start").executes(DeathmatchCommand::startDeathmatch));
    }

    private static int startDeathmatch(CommandContext<CommandSourceStack> context)
    {
        ServerLevel level = context.getSource().getLevel();
        MinecraftServer server = level.getServer();
        PlayerList playerList = server.getPlayerList();

        SoundHelper.broadcast(level, SoundEvents.ELDER_GUARDIAN_AMBIENT, 1.0f, 0.9f);
        playerList.broadcastAll(new ClientboundSetTitleTextPacket(Language.translatable("event.yolt.deathmatch.start.1").withStyle(ChatFormatting.RED)));
        playerList.broadcastSystemMessage(Language.translatable("event.yolt.deathmatch.start.1").withStyle(ChatFormatting.RED), false);

        Events.schedule(server.getTickCount() + 80, () ->
        {
            SoundHelper.broadcast(level, SoundEvents.ELDER_GUARDIAN_AMBIENT, 1.0f, 0.9f);
            playerList.broadcastSystemMessage(Language.translatable("event.yolt.deathmatch.start.2").withStyle(ChatFormatting.RED), false);
        });

        Events.schedule(server.getTickCount() + 160, () ->
        {
            SoundHelper.broadcast(level, SoundEvents.ELDER_GUARDIAN_AMBIENT, 1.0f, 0.7f);
            playerList.broadcastSystemMessage(Language.translatable("event.yolt.deathmatch.start.3", Component.literal("consequences").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)).withStyle(ChatFormatting.RED), false);
        });

        Events.schedule(server.getTickCount() + 280, () ->
        {
            SoundHelper.broadcast(level, SoundEvents.WITHER_SPAWN, 1.0f, 0.9f);
            playerList.broadcastAll(new ClientboundSetTitleTextPacket(Language.translatable("event.yolt.deathmatch.start.4").withStyle(ChatFormatting.RED)));
            playerList.broadcastSystemMessage(Language.translatable("event.yolt.deathmatch.start.4").withStyle(ChatFormatting.RED), false);

            WorldBorder worldBorder = level.getWorldBorder();
            worldBorder.setWarningTime(0);
            worldBorder.setWarningBlocks(64);
            worldBorder.lerpSizeBetween(1728, 64, 144000, level.getGameTime());
        });

        return 1;
    }
}