package floris0106.yolt.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import floris0106.yolt.config.Config;
import floris0106.yolt.util.ServerPlayerExtension;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements ServerPlayerExtension
{
	@Unique
	private int yolt$lives;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void yolt$setInitialLives(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation clientInformation, CallbackInfo ci)
	{
        yolt$lives = ((ServerPlayer) (Object) this).gameMode.isSurvival() ? Config.getDefaultLives() : -1;
        yolt$updateMaxHealth();
        yolt$setTotalHealth(Config.getDefaultTotalHealth());
	}

	@Inject(method = "restoreFrom", at = @At("TAIL"))
	private void yolt$restoreLives(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci)
	{
        yolt$lives = ((ServerPlayerExtension) oldPlayer).yolt$getLives();

        if (alive)
            return;

        ServerPlayer newPlayer = (ServerPlayer) (Object) this;
		if (yolt$lives == 0)
			newPlayer.setGameMode(GameType.SPECTATOR);
        else
        {
            yolt$updateMaxHealth();
            yolt$setTotalHealth(Config.getDefaultTotalHealth());
        }
	}

	@Inject(method = "die", at = @At("TAIL"))
	private void yolt$onDeath(DamageSource damageSource, CallbackInfo ci)
	{
		ServerPlayer player = (ServerPlayer) (Object) this;
		if (!player.gameMode.isSurvival() || yolt$lives <= 0)
			return;

		yolt$lives--;
        yolt$updateNameColor();

		if (yolt$lives == 0)
		{
			ServerLevel level = player.level();
			LightningBolt lightning = Objects.requireNonNull(EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.COMMAND));
			lightning.moveOrInterpolateTo(player.position());
			lightning.setVisualOnly(true);
			level.addFreshEntity(lightning);
		}

		if (Config.doPlayersDropHeads())
		{
			ItemStack head = new ItemStack(Items.PLAYER_HEAD);
			head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
			player.spawnAtLocation(player.level(), head);
		}
	}

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void yolt$readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci)
    {
        yolt$lives = valueInput.getIntOr("YoltLives", Config.getDefaultLives());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void yolt$addAdditionalSaveData(ValueOutput valueOutput, CallbackInfo ci)
    {
        valueOutput.putInt("YoltLives", yolt$lives);
    }

	@ModifyReturnValue(method = "getTabListDisplayName", at = @At("RETURN"))
	private Component yolt$overrideNameColor(Component original)
	{
		return ((ServerPlayer) (Object) this).getDisplayName();
	}

	@Override
	public int yolt$getLives()
	{
		return yolt$lives;
	}
	@Override
	public void yolt$setLives(int lives)
	{
		yolt$lives = lives;
        yolt$updateLives();
	}

    @Override
    public float yolt$getTotalHealth()
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        return player.getHealth() + player.getAbsorptionAmount();
    }
    @Override
    public void yolt$setTotalHealth(float total)
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        float health = Math.min(total, player.getMaxHealth());
        player.setHealth(health);
        player.setAbsorptionAmount(total - health);
    }

    @Override
    public void yolt$updateLives()
    {
        float totalHealth = yolt$getTotalHealth();
        yolt$updateNameColor();
        yolt$updateMaxHealth();
        yolt$setTotalHealth(totalHealth);
    }

    @Unique
    public void yolt$updateNameColor()
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        player.level().getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
    }

    @Unique
    public void yolt$updateMaxHealth()
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        float maxHealth = Config.getMaxHealthByLives(yolt$lives);
        Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealth);
        Objects.requireNonNull(player.getAttribute(Attributes.MAX_ABSORPTION)).setBaseValue(Config.getMaxTotalHealth() - maxHealth);
    }
}