package floris0106.yolt.mixin;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import floris0106.yolt.config.Config;
import floris0106.yolt.util.CutsceneHelper;
import floris0106.yolt.util.Events;
import floris0106.yolt.util.Language;
import floris0106.yolt.util.PresentTracker;
import floris0106.yolt.util.Role;
import floris0106.yolt.util.ServerLevelExtension;
import floris0106.yolt.util.ServerPlayerExtension;
import floris0106.yolt.util.SoundHelper;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelExtension
{
	@Unique
	private static final int YOLT$DAY_LENGTH_MULTIPLIER = 3;

	@Unique
	private int yolt$sleepingCutsceneCounter = 0;
	@Unique
	private int yolt$tickCounter = 0;

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"))
	private boolean yolt$sleepingCutscene(SleepStatus sleepStatus, int sleepingPercentage, List<ServerPlayer> players, Operation<Boolean> original)
	{
		ServerLevel level = (ServerLevel) (Object) this;
		players = level.getServer().getPlayerList().getPlayers();
		if (yolt$sleepingCutsceneCounter == 0 && !original.call(sleepStatus, sleepingPercentage, players))
			return false;

		RandomSource random = level.getRandom();
		players = new ArrayList<>(players);
		for (int i = 0; i < players.size(); i++)
		{
			int j = random.nextInt(i, players.size());
			ServerPlayer temp = players.get(i);
			players.set(i, players.get(j));
			players.set(j, temp);
		}

		if (yolt$sleepingCutsceneCounter == 0)
			SoundHelper.broadcast(level, SoundHelper.SLEIGH_BELLS, 1.0f, 1.0f);
		else if (yolt$sleepingCutsceneCounter == 100)
			SoundHelper.broadcast(level, SoundHelper.HO_HO_HO, 1.0f, 1.0f);

		yolt$sleepingCutsceneCounter++;

		if (yolt$sleepingCutsceneCounter <= 300)
			return false;

		players = players.stream()
			.filter(ServerPlayer::isSleeping)
			.filter(player -> ((ServerPlayerExtension) player).yolt$getRole() != Role.VICTIM)
			.toList();

		List<ServerPlayer> nicePlayers = Lists.newArrayList();
		for (ServerPlayer player : players)
		{
			BaseContainerBlockEntity[] nearbyContainers = BlockPos.betweenClosedStream(AABB.unitCubeFromLowerCorner(
				Vec3.atLowerCornerOf(player.getSleepingPos().get())).inflate(Config.getContainerSearchRange()))
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

		Map<ServerPlayer, BlockPos> sleepingPositions = new Reference2ReferenceOpenHashMap<>(players.size());
		int y = level.getMaxY() + 1;
		for (ServerPlayer player : players)
			sleepingPositions.put(player, player.getSleepingPos().get().atY(y));

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

				BlockPos pos = sleepingPositions.get(player).offset(x, 0, z);
				if (!worldBorder.isWithinBounds(pos))
					continue;

				boolean tooClose = false;
				for (BlockPos other : sleepingPositions.values())
					if (other.distSqr(pos) < minDistance * minDistance)
					{
						tooClose = true;
						break;
					}
				if (tooClose)
					continue;

				CutsceneHelper.spawnPresent(level, random, pos);
				break;
			}

		yolt$sleepingCutsceneCounter = 0;
		level.getGameRules().set(GameRules.ADVANCE_TIME, true, level.getServer());
		level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 100, level.getServer());
		return true;
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
	private void yolt$wakeUpEarlier(ServerLevel level, long dayTime, Operation<Void> original)
	{
		original.call(level, (long) 23000);
	}

	@WrapOperation(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
	private void yolt$elongateDay(ServerLevel level, long time, Operation<Void> original)
	{
		if (++yolt$tickCounter < YOLT$DAY_LENGTH_MULTIPLIER)
			return;

		yolt$tickCounter = 0;
		original.call(level, time);
		Events.onTimeTick(level);
	}

	@Override
	public void yolt$addPresentPosition(BlockPos pos)
	{
		PresentTracker tracker = ((ServerLevel) (Object) this).getDataStorage().computeIfAbsent(PresentTracker.DATA_TYPE);
		tracker.getPositions().add(pos);
		tracker.setDirty();
	}

	@Override
	public void yolt$removePresentPosition(BlockPos pos)
	{
		PresentTracker tracker = ((ServerLevel) (Object) this).getDataStorage().computeIfAbsent(PresentTracker.DATA_TYPE);
		tracker.getPositions().remove(pos);
		tracker.setDirty();
	}

	@Override
	public double yolt$getPresentDistance(Vec3 pos)
	{
		ServerLevel level = (ServerLevel) (Object) this;
		double distance = Double.MAX_VALUE;
		for (Entity entity : level.getAllEntities())
			if (entity instanceof FallingBlockEntity fallingBlock && fallingBlock.getBlockState().is(Blocks.VAULT))
				distance = Math.min(distance, fallingBlock.position().distanceTo(pos));

		return Math.min(distance, level.getDataStorage()
			.computeIfAbsent(PresentTracker.DATA_TYPE)
			.getPositions()
			.stream()
			.mapToDouble(blockPos -> Vec3.atCenterOf(blockPos).distanceTo(pos))
			.min()
			.orElse(Double.MAX_VALUE));
	}
}