package floris0106.yolt.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import floris0106.yolt.config.Config;
import floris0106.yolt.util.CutsceneHelper;
import floris0106.yolt.util.Language;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;

import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.Commands.literal;

public class GiftCommand
{
    public static ArgumentBuilder<CommandSourceStack, ?> register()
    {
        return literal("gift")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
            .executes(GiftCommand::sendPresents);
    }

    private static int sendPresents(CommandContext<CommandSourceStack> context)
    {
        ServerLevel level = context.getSource().getLevel();
        WorldBorder worldBorder = level.getWorldBorder();
        int maxDistance = Config.getMaximumPresentDistance();
        int maxY = level.getMaxY();
        level.players().stream().filter(player -> player.gameMode() != GameType.SPECTATOR).forEach(player ->
        {
            for (int i = 0; i < 100; i++)
            {
                int x = Config.getPresentOffset(level.random);
                int z = Config.getPresentOffset(level.random);
                if (x * x + z * z > maxDistance * maxDistance)
                    continue;

                BlockPos pos = player.getOnPos().offset(x, 0, z).atY(maxY);
                if (!worldBorder.isWithinBounds(pos))
                    continue;

                player.sendSystemMessage(Language.translatable("event.yolt.gift").withStyle(ChatFormatting.GRAY));

                ItemStack key = new ItemStack(Items.OMINOUS_TRIAL_KEY);
                key.set(DataComponents.ITEM_NAME, Language.translatable("item.yolt.candy_key"));
                key.set(DataComponents.LORE, new ItemLore(Language.KEY_LORE));
                key.set(DataComponents.RARITY, Rarity.RARE);
                player.addItem(key);

                CutsceneHelper.spawnPresent(level, level.random, pos);
                break;
            }
        });

        return 1;
    }
}