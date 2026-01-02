package floris0106.yolt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.util.RandomSource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import floris0106.yolt.Yolt;

public class Config
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), Yolt.MOD_ID + ".json");

    private static Config instance;

    private boolean yellowEnabled = true;
    private int defaultLives = 3;
    private float[] maxHealthByLives = {10.0f, 16.0f};
    private boolean playersDropHeads = true;
    private float maxTotalHealth = 40.0f;
    private float defaultTotalHealth = 20.0f;
    private boolean frostedIceMeltsFaster = true;
    private int containerSearchRange = 8;
    private int averagePresentDistance = 64;
    private int minimumPresentDistance = 16;
    private int maximumPresentDistance = 128;
    private float minimumPresentHeight = 2;
    private float maximumPresentHeight = 16;
    private int sleepPercentageDecrementTicks = 120;

    public static void load()
    {
        try (FileReader reader = new FileReader(FILE))
        {
            instance = GSON.fromJson(reader, Config.class);
        }
        catch (Exception e)
        {
            Yolt.LOGGER.info("Failed to read {}, creating new file", FILE.getName());
            reset();
        }
    }

    public static void save()
    {
        try (FileWriter writer = new FileWriter(FILE))
        {
            writer.write(GSON.toJson(instance));
        }
        catch (Exception e)
        {
            Yolt.LOGGER.error("Exception thrown while trying to save config: ", e);
        }
    }

    public static void reset()
    {
        instance = new Config();
        save();
    }

    public static ChatFormatting getColorByLives(int lives)
    {
        if (lives <= 0)
            return ChatFormatting.GRAY;
        if (lives == 1)
            return ChatFormatting.RED;
        if (isYellowEnabled())
            lives--;
        return switch (lives)
        {
            case 1 -> ChatFormatting.YELLOW;
            case 2 -> ChatFormatting.GREEN;
            default -> ChatFormatting.DARK_GREEN;
        };
    }

    public static float getMaxHealthByLives(int lives)
    {
        if (0 >= lives || lives > instance.maxHealthByLives.length)
            return 20.0f;
        return instance.maxHealthByLives[lives - 1];
    }

    public static int getPresentOffset(RandomSource random)
    {
        return (int) Math.round(random.nextGaussian() * instance.averagePresentDistance);
    }

    public static boolean isYellowEnabled()
    {
        return instance.yellowEnabled;
    }
    public static void setYellowEnabled(boolean enabled)
    {
        instance.yellowEnabled = enabled;
        save();
    }

    public static int getDefaultLives()
    {
        return instance.defaultLives;
    }
    public static void setDefaultLives(int lives)
    {
        instance.defaultLives = lives;
        save();
    }

    public static float[] getMaxHealthByLivesArray()
    {
        return instance.maxHealthByLives;
    }
    public static void setMaxHealthByLivesArray(float[] array)
    {
        instance.maxHealthByLives = array;
        save();
    }

    public static boolean doPlayersDropHeads()
    {
        return instance.playersDropHeads;
    }
    public static void setPlayersDropHeads(boolean enabled)
    {
        instance.playersDropHeads = enabled;
        save();
    }

    public static float getMaxTotalHealth()
    {
        return instance.maxTotalHealth;
    }
    public static void setMaxTotalHealth(float maxTotalHealth)
    {
        instance.maxTotalHealth = maxTotalHealth;
        save();
    }

    public static float getDefaultTotalHealth()
    {
        return instance.defaultTotalHealth;
    }
    public static void setDefaultTotalHealth(float defaultTotalHealth)
    {
        instance.defaultTotalHealth = defaultTotalHealth;
        save();
    }

    public static boolean doesFrostedIceMeltFaster()
    {
        return instance.frostedIceMeltsFaster;
    }
    public static void setFrostedIceMeltsFaster(boolean frostedIceMeltsFaster)
    {
        instance.frostedIceMeltsFaster = frostedIceMeltsFaster;
        save();
    }

    public static int getContainerSearchRange()
    {
        return instance.containerSearchRange;
    }
    public static void setContainerSearchRange(int chestSearchRange)
    {
        instance.containerSearchRange = chestSearchRange;
        save();
    }

    public static int getAveragePresentDistance()
    {
        return instance.averagePresentDistance;
    }
    public static void setAveragePresentDistance(int averagePresentDistance)
    {
        instance.averagePresentDistance = averagePresentDistance;
        save();
    }

    public static int getMinimumPresentDistance()
    {
        return instance.minimumPresentDistance;
    }
    public static void setMinimumPresentDistance(int minimumPresentDistance)
    {
        instance.minimumPresentDistance = minimumPresentDistance;
        save();
    }

    public static int getMaximumPresentDistance()
    {
        return instance.maximumPresentDistance;
    }
    public static void setMaximumPresentDistance(int maximumPresentDistance)
    {
        instance.maximumPresentDistance = maximumPresentDistance;
        save();
    }

    public static float getMinimumPresentHeight()
    {
        return instance.minimumPresentHeight;
    }
    public static void setMinimumPresentHeight(float minimumPresentHeight)
    {
        instance.minimumPresentHeight = minimumPresentHeight;
        save();
    }

    public static float getMaximumPresentHeight()
    {
        return instance.maximumPresentHeight;
    }
    public static void setMaximumPresentHeight(float maximumPresentHeight)
    {
        instance.maximumPresentHeight = maximumPresentHeight;
        save();
    }

    public static int getSleepPercentageDecrementTicks()
    {
        return instance.sleepPercentageDecrementTicks;
    }
    public static void setSleepPercentageDecrementTicks(int sleepPercentageDecrementTicks)
    {
        instance.sleepPercentageDecrementTicks = sleepPercentageDecrementTicks;
        save();
    }
}