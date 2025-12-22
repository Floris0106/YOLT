package floris0106.yolt.util;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import floris0106.yolt.Yolt;

public class SoundHelper
{
	public static final Holder<SoundEvent> YAWN = Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Yolt.MOD_ID, "event.yawn")));
	public static final Holder<SoundEvent> SLEIGH_BELLS = Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Yolt.MOD_ID, "event.santa.sleigh_bells")));
	public static final Holder<SoundEvent> HO_HO_HO = Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Yolt.MOD_ID, "event.santa.ho_ho_ho")));

	public static void broadcast(ServerLevel level, Holder<SoundEvent> sound, float volume, float pitch)
	{
		long seed = level.getRandom().nextLong();
		for (ServerPlayer player : level.players())
			player.connection.send(new ClientboundSoundEntityPacket(sound, SoundSource.VOICE, player, volume, pitch, seed));
	}
}