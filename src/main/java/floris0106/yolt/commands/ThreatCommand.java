package floris0106.yolt.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import floris0106.yolt.config.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;

import java.util.Objects;

import static net.minecraft.commands.Commands.*;

public class ThreatCommand
{
    public static ArgumentBuilder<CommandSourceStack, ?> register()
    {
        return literal("threat")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
            .executes(ThreatCommand::sendSculk);
    }

    private static int sendSculk(CommandContext<CommandSourceStack> context)
    {
        ServerLevel level = context.getSource().getLevel();
        WorldBorder worldBorder = level.getWorldBorder();
        int maxDistance = Config.getMinimumPresentDistance();
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

                FallingBlockEntity catalyst = FallingBlockEntity.fall(level, pos, Blocks.SCULK_CATALYST.defaultBlockState());
                catalyst.time = Integer.MIN_VALUE;
                catalyst.dropItem = false;

                ArmorStand armorStand = new ArmorStand(
                    level,
                    catalyst.getX(),
                    catalyst.getY() + Mth.nextDouble(level.random, Config.getMinimumPresentHeight(), Config.getMaximumPresentHeight()),
                    catalyst.getZ()
                );
                armorStand.setSmall(true);
                armorStand.setInvisible(true);
                armorStand.addTag("yolt_remove_when_on_ground");
                armorStand.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, -1));
                Objects.requireNonNull(armorStand.getAttribute(Attributes.SCALE)).setBaseValue(0.0625);
                level.addFreshEntity(armorStand);

                catalyst.startRiding(armorStand, true, false);
                break;
            }
        });

        return 1;
    }
}