package floris0106.yolt.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class EnumArgument<T extends Enum<T>> implements SuggestionProvider<CommandSourceStack>
{
	public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((object) -> Component.literal("Invalid value \"" + object + "\""));

	private final Class<T> clazz;

	private EnumArgument(Class<T> clazz)
	{
		this.clazz = clazz;
	}

	public static <T extends Enum<T>> ArgumentBuilder<CommandSourceStack, ?> argument(String argument, Class<T> clazz)
	{
		return Commands.argument(argument, StringArgumentType.word()).suggests(new EnumArgument<>(clazz));
	}

	public static <T extends Enum<T>> T getEnum(CommandContext<CommandSourceStack> context, String argument, Class<T> clazz) throws CommandSyntaxException
	{
		String word = StringArgumentType.getString(context, argument);
		try
		{
			return Enum.valueOf(clazz, word.toUpperCase());
		}
		catch (Exception e)
		{
			throw ERROR_INVALID_VALUE.create(word);
		}
	}

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
	{
		for (T value : clazz.getEnumConstants())
			builder.suggest(value.name());
		return builder.buildFuture();
	}
}