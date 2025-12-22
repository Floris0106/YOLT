package floris0106.yolt.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import floris0106.yolt.config.Config;
import floris0106.yolt.util.FloatSupplier;
import floris0106.yolt.util.Language;
import floris0106.yolt.util.ServerPlayerExtension;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;

public class ConfigCommand
{
	private static final Option[] OPTIONS =
		{
			new BooleanOption("yellowNamesEnabled", Config::isYellowEnabled, Config::setYellowEnabled, true),
			new IntegerOption("defaultNumberOfLives", -1, Config::getDefaultLives, Config::setDefaultLives),
			new FloatArrayOption("maxHealthByNumberOfLives", Config::getMaxHealthByLivesArray, Config::setMaxHealthByLivesArray, true),
			new BooleanOption("doPlayersDropHeads", Config::doPlayersDropHeads, Config::setPlayersDropHeads),
            new FloatOption("maxTotalHealth", 1.0f, Config::getMaxTotalHealth, Config::setMaxTotalHealth, true),
            new FloatOption("defaultTotalHealth", 1.0f, Config::getDefaultTotalHealth, Config::setDefaultTotalHealth),
			new BooleanOption("frostedIceMeltsFaster", Config::doesFrostedIceMeltFaster, Config::setFrostedIceMeltsFaster),
			new IntegerOption("containerSearchRange", Config::getContainerSearchRange, Config::setContainerSearchRange),
			new IntegerOption("averagePresentSpawnDistance", Config::getAveragePresentDistance, Config::setAveragePresentDistance),
			new IntegerOption("minimumPresentSpawnDistance", Config::getMinimumPresentDistance, Config::setMinimumPresentDistance),
			new IntegerOption("maximumPresentSpawnDistance", Config::getMaximumPresentDistance, Config::setMaximumPresentDistance)
		};

	public static ArgumentBuilder<CommandSourceStack, ?> register()
	{
		ArgumentBuilder<CommandSourceStack, ?> builder = literal("config")
			.then(literal("reset")
				.executes(context -> {
					Config.reset();
					context.getSource().sendSuccess(() -> Language.translatable("commands.yolt.config.reset.success"), false);
					for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers())
						((ServerPlayerExtension) player).yolt$updateLives();
					return 1;
				})
			);
		for (Option option : OPTIONS)
			builder = builder.then(option.register());
		return builder;
	}

	private static abstract class Option
	{
		protected final String name;
		private final ArgumentType<?> argument;
		private final boolean updateLives;

		public Option(String name, ArgumentType<?> argument, boolean updateLives)
		{
			this.name = name;
			this.argument = argument;
			this.updateLives = updateLives;
		}

		public ArgumentBuilder<CommandSourceStack, ?> register()
		{
			return literal(name)
				.executes(context ->
				{
					context.getSource().sendSuccess(this::getResponse, false);
					return 1;
				})
				.then(argument(name, argument)
					.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
					.executes(context ->
					{
						set(context);
						context.getSource().sendSuccess(this::setResponse, false);
						if (updateLives)
							for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers())
                        	    ((ServerPlayerExtension) player).yolt$updateLives();
						return 1;
					})
				);
		}

		protected Component getResponse()
		{
			return Language.translatable("commands.yolt.config." + name + ".query", get());
		}
		protected Component setResponse()
		{
			return Language.translatable("commands.yolt.config." + name + ".set", get());
		}

		protected abstract Object get();
		protected abstract void set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
	}

	private static class BooleanOption extends Option
	{
		private final BooleanSupplier getter;
		private final BooleanConsumer setter;

		public BooleanOption(String name, BooleanSupplier getter, BooleanConsumer setter, boolean updateLives)
		{
			super(name, BoolArgumentType.bool(), updateLives);
			this.getter = getter;
			this.setter = setter;
		}

		public BooleanOption(String name, BooleanSupplier getter, BooleanConsumer setter)
		{
			this(name, getter, setter, false);
		}

		@Override
		protected Component getResponse()
		{
			return Language.translatable("commands.yolt.config." + name + ".query." + (getter.getAsBoolean() ? "enabled" : "disabled"));
		}

		@Override
		protected Component setResponse()
		{
			return Language.translatable("commands.yolt.config." + name + ".set." + (getter.getAsBoolean() ? "enabled" : "disabled"));
		}

		@Override
		protected Object get()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		protected void set(CommandContext<CommandSourceStack> context)
		{
			setter.accept(BoolArgumentType.getBool(context, name));
		}
	}

	private static class IntegerOption extends Option
	{
		private final IntSupplier getter;
		private final IntConsumer setter;

		public IntegerOption(String name, int min, int max, IntSupplier getter, IntConsumer setter, boolean updateLives)
		{
			super(name, IntegerArgumentType.integer(min, max), updateLives);
			this.getter = getter;
			this.setter = setter;
		}

		public IntegerOption(String name, int min, IntSupplier getter, IntConsumer setter)
		{
			this(name, min, Integer.MAX_VALUE, getter, setter, false);
		}
		public IntegerOption(String name, IntSupplier getter, IntConsumer setter)
		{
			this(name, Integer.MIN_VALUE, Integer.MAX_VALUE, getter, setter, false);
		}

		@Override
		protected Object get()
		{
			return getter.getAsInt();
		}

		@Override
		protected void set(CommandContext<CommandSourceStack> context)
		{
			setter.accept(IntegerArgumentType.getInteger(context, name));
		}
	}

    private static class FloatOption extends Option
    {
        private final FloatSupplier getter;
        private final FloatConsumer setter;

        public FloatOption(String name, float min, float max, FloatSupplier getter, FloatConsumer setter, boolean updateLives)
        {
            super(name, DoubleArgumentType.doubleArg(min, max), updateLives);
            this.getter = getter;
            this.setter = setter;
        }
        public FloatOption(String name, float min, FloatSupplier getter, FloatConsumer setter, boolean updateLives)
        {
            this(name, min, Float.MAX_VALUE, getter, setter, updateLives);
        }

		public FloatOption(String name, float min, FloatSupplier getter, FloatConsumer setter)
		{
			this(name, min, Float.MAX_VALUE, getter, setter, false);
		}

        @Override
        protected Object get()
        {
            return getter.getAsFloat();
        }

        @Override
        protected void set(CommandContext<CommandSourceStack> context)
        {
            setter.accept((float) DoubleArgumentType.getDouble(context, name));
        }
    }


    private static class FloatArrayOption extends Option
	{
		private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(object -> Language.translatable("argument.yolt.float_array.invalid", object));

		private final Supplier<float[]> getter;
		private final Consumer<float[]> setter;

		public FloatArrayOption(String name, Supplier<float[]> getter, Consumer<float[]> setter, boolean updateLives)
		{
			super(name, NbtTagArgument.nbtTag(), updateLives);
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		protected Object get()
		{
			float[] array = getter.get();
			ListTag list = new ListTag();
			for (float value : array)
				list.add(FloatTag.valueOf(value));
			return list.toString();
		}

		@Override
		protected void set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
		{
			Tag tag = NbtTagArgument.getNbtTag(context, name);
			ListTag list = tag.asList().orElseThrow(() -> ERROR_INVALID_VALUE.create(tag));
			float[] array = new float[list.size()];
			for (int i = 0; i < array.length; i++)
			{
				int j = i;
				array[i] = list.getFloat(i).orElseThrow(() -> ERROR_INVALID_VALUE.create(list.get(j)));
			}
			setter.accept(array);
		}
	}
}