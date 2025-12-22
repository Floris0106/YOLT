package floris0106.yolt.util;

import com.google.common.collect.ImmutableMap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import floris0106.yolt.Yolt;

public class Language
{
	private static final Map<String, String> FALLBACK;

	static
	{
		try (InputStream is = Language.class.getResourceAsStream("/assets/" + Yolt.MOD_ID + "/lang/en_us.json"))
		{
			if (is == null)
				throw new IOException("Failed to find language file");
			ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
			net.minecraft.locale.Language.loadFromJson(is, builder::put);
			FALLBACK = builder.build();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false);
	public static final List<Component> KEY_LORE = List.of(
		Language.translatable("event.yolt.santa.key_lore.1").withStyle(LORE_STYLE),
		Language.translatable("event.yolt.santa.key_lore.2").withStyle(LORE_STYLE),
		Language.translatable("event.yolt.santa.key_lore.3").withStyle(LORE_STYLE)
	);
	public static final List<Component> COAL_LORE = List.of(
		Language.translatable("event.yolt.santa.coal_lore.1").withStyle(LORE_STYLE),
		Language.translatable("event.yolt.santa.coal_lore.2").withStyle(LORE_STYLE)
	);

	public static MutableComponent translatable(String key, Object... args)
	{
		return Component.translatableWithFallback(key, FALLBACK.getOrDefault(key, key), args);
	}
}