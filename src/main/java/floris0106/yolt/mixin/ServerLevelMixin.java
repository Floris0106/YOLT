package floris0106.yolt.mixin;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import floris0106.yolt.config.Config;
import floris0106.yolt.util.Language;
import floris0106.yolt.util.SoundHelper;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin
{
	@Unique
	private int yolt$sleepingCutsceneCounter = 0;

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"))
	private boolean yolt$sleepingCutscene(SleepStatus sleepStatus, int sleepingPercentage, List<ServerPlayer> players, Operation<Boolean> original)
	{
		ServerLevel level = (ServerLevel) (Object) this;
		players = level.getServer().getPlayerList().getPlayers();
		if (!original.call(sleepStatus, 100, players))
			return false;

		if (yolt$sleepingCutsceneCounter == 0)
			SoundHelper.broadcast(level, SoundHelper.SLEIGH_BELLS, 1.0f, 1.0f);
		else if (yolt$sleepingCutsceneCounter == 100)
			SoundHelper.broadcast(level, SoundHelper.HO_HO_HO, 1.0f, 1.0f);

		yolt$sleepingCutsceneCounter++;

		if (yolt$sleepingCutsceneCounter <= 240)
			return false;

		RandomSource random = level.getRandom();
		List<ServerPlayer> nicePlayers = Lists.newArrayList();
		for (ServerPlayer player : players)
		{
			BaseContainerBlockEntity[] nearbyContainers = BlockPos.betweenClosedStream(
					AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(player.getSleepingPos().get()))
						.inflate(Config.getContainerSearchRange())
				)
				.map(level::getBlockEntity)
				.flatMap(blockEntity -> blockEntity instanceof BaseContainerBlockEntity container ? Stream.of(container) : Stream.empty())
				.toArray(BaseContainerBlockEntity[]::new);
			for (int i = 0; i < nearbyContainers.length; i++)
			{
				int j = random.nextInt(i, nearbyContainers.length);
				BaseContainerBlockEntity a = nearbyContainers[i];
				BaseContainerBlockEntity b = nearbyContainers[j];
				nearbyContainers[i] = b;
				nearbyContainers[j] = a;
			}

			Arrays.stream(nearbyContainers).filter(container ->
				container.hasAnyMatching(itemStack -> itemStack.is(Items.MILK_BUCKET)) &&
				container.hasAnyMatching(itemStack -> itemStack.is(Items.COOKIE))
			).findAny().ifPresentOrElse(container -> {
				nicePlayers.add(player);

				ItemStack key = new ItemStack(Items.OMINOUS_TRIAL_KEY);
				key.set(DataComponents.ITEM_NAME, Language.translatable("item.yolt.candy_key"));
				key.set(DataComponents.LORE, new ItemLore(Language.KEY_LORE));
				key.set(DataComponents.RARITY, Rarity.RARE);

				for (int i = 0; i < container.getContainerSize(); i++)
				{
					ItemStack itemStack = container.getItem(i);
					if (itemStack.is(Items.MILK_BUCKET))
						container.setItem(i, Items.BUCKET.getDefaultInstance());
					else if (itemStack.is(Items.COOKIE))
					{
						container.removeItemNoUpdate(i);
						if (key != null)
						{
							container.setItem(i, key);
							key = null;
						}
					}
				}
			}, () ->
			{
				ItemStack coal = new ItemStack(Items.COAL);
				coal.set(DataComponents.LORE, new ItemLore(Language.COAL_LORE));

				for (BaseContainerBlockEntity container : nearbyContainers)
				{
					int size = container.getContainerSize();
					if (coal != null)
						for (int i = 0; i < size; i++)
							if (container.getItem(i).isEmpty())
							{
								container.setItem(i, coal);
								coal = null;
								break;
							}

					for (int i = 0; i < size; i++)
					{
						int j = random.nextInt(i, size);
						ItemStack a = container.removeItemNoUpdate(i);
						ItemStack b = container.removeItemNoUpdate(j);
						container.setItem(i, b);
						container.setItem(j, a);
					}
				}

				if (coal != null)
					player.getInventory().add(coal);
			});

			player.sendSystemMessage(Language.translatable("event.yolt.santa.check_chests").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}

		WorldBorder worldBorder = level.getWorldBorder();
		int minDistance = Config.getMinimumPresentDistance();
		int maxDistance = Config.getMaximumPresentDistance();
		for (ServerPlayer player : nicePlayers)
			for (int i = 0; i < 100; i++)
			{
				int x = Config.getPresentOffset(random);
				int z = Config.getPresentOffset(random);
				if (x * x + z * z > maxDistance * maxDistance)
					continue;

				BlockPos pos = player.getSleepingPos().get().offset(x, 0, z);
				if (!worldBorder.isWithinBounds(pos))
					continue;

				boolean tooClose = false;
				for (ServerPlayer other : players)
					if (other.getSleepingPos().get().distSqr(pos) < minDistance * minDistance)
					{
						tooClose = true;
						break;
					}
				if (tooClose)
					continue;

				ArmorStand armorStand = new ArmorStand(level, pos.getX() + 0.5, level.getMaxY() + 2.0 + 8.0 * random.nextDouble(), pos.getZ() + 0.5);
				armorStand.setSmall(true);
				armorStand.setInvisible(true);
				armorStand.addTag("yolt_remove_when_on_ground");
				armorStand.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, -1));
				Objects.requireNonNull(armorStand.getAttribute(Attributes.SCALE)).setBaseValue(0.0625);

				FallingBlockEntity vault = new FallingBlockEntity(level, 0.0, 0.0, 0.0, Blocks.VAULT.defaultBlockState()
					.setValue(VaultBlock.OMINOUS, true)
					.setValue(VaultBlock.STATE, VaultState.ACTIVE)
				);
				vault.blockData = new CompoundTag();
				CompoundTag config = new CompoundTag();
				config.putString("loot_table", "yolt:present");
				CompoundTag keyItem = new CompoundTag();
				keyItem.putString("id", "minecraft:ominous_trial_key");
				config.put("key_item", keyItem);
				vault.blockData.put("config", config);
				vault.time = Integer.MIN_VALUE;
				vault.dropItem = false;

				level.addFreshEntity(armorStand);
				level.addFreshEntity(vault);
				vault.startRiding(armorStand, true, false);

				break;
			}

		yolt$sleepingCutsceneCounter = 0;
		level.getGameRules().set(GameRules.ADVANCE_TIME, true, level.getServer());
		return true;
	}
}