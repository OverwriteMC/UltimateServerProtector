package ru.overwrite.protect.bukkit.color;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import ru.overwrite.protect.bukkit.color.impl.LegacyColorizer;
import ru.overwrite.protect.bukkit.color.impl.MiniMessageColorizer;

import java.util.Locale;

@UtilityClass
public class ColorizerProvider {
    public Colorizer COLORIZER;

    public void init(ConfigurationSection config) {
        String serializerType = config.getString("serializer", "LEGACY").toUpperCase(Locale.ENGLISH);
        COLORIZER = "MINIMESSAGE".equals(serializerType)
                ? new MiniMessageColorizer()
                : new LegacyColorizer();
    }
}

