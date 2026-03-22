package ru.overwrite.protect.bukkit;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredListener;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordEnterEvent;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordFailEvent;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.configuration.data.EncryptionSettings;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.time.LocalDateTime;
import java.util.List;

public final class PasswordHandler {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PlayerManager playerManager;
    private final Config pluginConfig;

    @Getter
    private final Object2IntOpenHashMap<String> attempts = new Object2IntOpenHashMap<>();

    public PasswordHandler(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.api = plugin.getApi();
        this.playerManager = plugin.getPlayerManager();
        this.pluginConfig = plugin.getPluginConfig();
    }

    public void checkPassword(Player player, String input, boolean resync) {
        Runnable run = () -> {
            RegisteredListener[] listeners = ServerProtectorPasswordEnterEvent.getHandlerList().getRegisteredListeners();
            if (pluginConfig.getApiSettings().callEventOnPasswordEnter() && listeners.length != 0 && !new ServerProtectorPasswordEnterEvent(player, input).callEvent()) {
                return;
            }
            String playerPass = pluginConfig.getPerPlayerPasswords().get(player.getName());
            if (playerPass == null) {
                this.failedPassword(player);
                return;
            }
            EncryptionSettings encryptionSettings = pluginConfig.getEncryptionSettings();
            String salt = null;
            String pass = encryptionSettings.enableEncryption()
                    ? Utils.encryptPassword(input, salt = playerPass.split(":")[0], encryptionSettings.encryptMethods())
                    : input;
            if (pass.equals(playerPass)) {
                this.correctPassword(player);
                return;
            }
            List<List<String>> oldMethods = encryptionSettings.oldEncryptMethods();
            if (encryptionSettings.enableEncryption() && !oldMethods.isEmpty()) {
                for (int i = 0; i < oldMethods.size(); i++) {
                    List<String> oldEncryptMethod = oldMethods.get(i);
                    String oldgenPass = Utils.encryptPassword(input, salt, oldEncryptMethod);
                    if (oldgenPass.equals(playerPass)) {
                        this.correctPassword(player);
                        return;
                    }
                }
            }
            this.failedPassword(player);
            if (pluginConfig.getPunishSettings().enableAttempts() && isAttemptsMax(player.getName())) {
                plugin.checkFail(player.getName(), pluginConfig.getCommands().failedPass());
            }
        };
        if (resync) {
            plugin.getRunner().runPlayer(run, player);
        } else {
            run.run();
        }
    }

    private boolean isAttemptsMax(String playerName) {
        int playerAttempts = attempts.getInt(playerName);
        return playerAttempts >= pluginConfig.getPunishSettings().maxAttempts();
    }

    public void failedPassword(Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        String playerName = player.getName();
        if (pluginConfig.getPunishSettings().enableAttempts()) {
            attempts.addTo(playerName, 1);
        }
        RegisteredListener[] failListeners = ServerProtectorPasswordFailEvent.getHandlerList().getRegisteredListeners();
        if (failListeners.length != 0) {
            if (!new ServerProtectorPasswordFailEvent(player, attempts.getInt(playerName)).callEvent()) {
                return;
            }
        }
        player.sendMessage(pluginConfig.getMessages().incorrect());
        if (pluginConfig.getMessageSettings().sendTitle()) {
            Utils.sendTitleMessage(pluginConfig.getTitles().incorrect(), player);
        }
        if (pluginConfig.getSoundSettings().enableSounds()) {
            Utils.sendSound(pluginConfig.getSoundSettings().onPasFail(), player);
        }
        if (pluginConfig.getLoggingSettings().loggingPas()) {
            plugin.logAction(pluginConfig.getLogMessages().failed(), player, LocalDateTime.now());
        }
        if (pluginConfig.getBroadcasts() != null) {
            plugin.sendAlert(player, pluginConfig.getBroadcasts().failed());
        }
    }

    public void correctPassword(Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        RegisteredListener[] successListeners = ServerProtectorPasswordSuccessEvent.getHandlerList().getRegisteredListeners();
        if (successListeners.length != 0) {
            if (!new ServerProtectorPasswordSuccessEvent(player).callEvent()) {
                return;
            }
        }
        String playerName = player.getName();
        api.uncapturePlayer(playerName);
        player.sendMessage(pluginConfig.getMessages().correct());
        if (pluginConfig.getMessageSettings().sendTitle()) {
            Utils.sendTitleMessage(pluginConfig.getTitles().correct(), player);
        }
        plugin.getPerPlayerTime().removeInt(playerName);
        api.clearRejoins(playerName);
        if (pluginConfig.getSoundSettings().enableSounds()) {
            Utils.sendSound(pluginConfig.getSoundSettings().onPasCorrect(), player);
        }
        if (pluginConfig.getEffectSettings().enableEffects()) {
            playerManager.removeEffects(player);
        }
        playerManager.showPlayer(player);
        api.authorisePlayer(player);
        if (pluginConfig.getSessionSettings().sessionTimeEnabled()) {
            plugin.getRunner().runDelayedAsync(() -> {
                if (!api.isAuthorised(player)) {
                    api.deauthorisePlayer(player);
                }
            }, pluginConfig.getSessionSettings().sessionTime() * 20L);
        }
        if (pluginConfig.getLoggingSettings().loggingPas()) {
            plugin.logAction(pluginConfig.getLogMessages().passed(), player, LocalDateTime.now());
        }
        if (pluginConfig.getBossbarSettings().enableBossbar()) {
            playerManager.removeBossBar(playerName);
        }
        if (pluginConfig.getBroadcasts() != null) {
            plugin.sendAlert(player, pluginConfig.getBroadcasts().passed());
        }
    }
}
