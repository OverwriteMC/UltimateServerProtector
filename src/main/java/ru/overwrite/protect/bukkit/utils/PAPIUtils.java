package ru.overwrite.protect.bukkit.utils;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.color.ColorizerProvider;

@UtilityClass
public class PAPIUtils {

    public String parsePlaceholders(Player player, String message) {
        return ColorizerProvider.COLORIZER.colorize(PlaceholderAPI.setPlaceholders(player, message));
    }
}
