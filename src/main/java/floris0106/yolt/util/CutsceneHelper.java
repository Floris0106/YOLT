package floris0106.yolt.util;

import floris0106.yolt.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class CutsceneHelper
{
    private static final BlockState VAULT_BLOCK_STATE = Blocks.VAULT.defaultBlockState()
        .setValue(VaultBlock.OMINOUS, true)
        .setValue(VaultBlock.STATE, VaultState.ACTIVE);
    private static final CompoundTag VAULT_BLOCK_DATA = new CompoundTag();

    public static void spawnPresent(ServerLevel level, RandomSource random, BlockPos pos)
    {
        FallingBlockEntity vault = FallingBlockEntity.fall(level, pos, VAULT_BLOCK_STATE);
        vault.blockData = VAULT_BLOCK_DATA;
        vault.time = Integer.MIN_VALUE;
        vault.dropItem = false;

        ArmorStand armorStand = new ArmorStand(
            level,
            vault.getX(),
            vault.getY() + Mth.nextDouble(random, Config.getMinimumPresentHeight(), Config.getMaximumPresentHeight()),
            vault.getZ()
        );
        armorStand.setSmall(true);
        armorStand.setInvisible(true);
        armorStand.addTag("yolt_remove_when_on_ground");
        armorStand.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, -1));
        Objects.requireNonNull(armorStand.getAttribute(Attributes.SCALE)).setBaseValue(0.0625);
        level.addFreshEntity(armorStand);

        vault.startRiding(armorStand, true, false);
    }

    static
    {
        CompoundTag config = new CompoundTag();
        config.putString("loot_table", "yolt:present");
        CompoundTag keyItem = new CompoundTag();
        keyItem.putString("id", "minecraft:ominous_trial_key");
        config.put("key_item", keyItem);
        VAULT_BLOCK_DATA.put("config", config);
    }
}