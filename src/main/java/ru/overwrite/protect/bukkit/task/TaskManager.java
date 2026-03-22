package ru.overwrite.protect.bukkit.task;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredListener;
import ru.overwrite.protect.bukkit.PlayerManager;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.configuration.data.BossbarSettings;
import ru.overwrite.protect.bukkit.task.runner.Runner;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.time.LocalDateTime;

public final class TaskManager {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PlayerManager playerManager;
    private final Config pluginConfig;
    private final Runner runner;

    public TaskManager(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.api = plugin.getApi();
        this.playerManager = plugin.getPlayerManager();
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    public void startMainCheck(long interval) {
        runner.runPeriodicalAsync(() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (api.isExcluded(onlinePlayer, pluginConfig.getExcludedPlayers().adminPass())) {
                    continue;
                }
                if (api.isCaptured(onlinePlayer)) {
                    continue;
                }
                CaptureReason captureReason = plugin.checkPermissions(onlinePlayer);
                if (captureReason == null) {
                    continue;
                }
                if (!api.isAuthorised(onlinePlayer)) {
                    ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(onlinePlayer, Utils.getIp(onlinePlayer), captureReason);
                    RegisteredListener[] listeners = captureEvent.getHandlers().getRegisteredListeners();
                    if (listeners.length != 0) {
                        captureEvent.callEvent();
                    }
                    if (pluginConfig.getApiSettings().allowCancelCaptureEvent() && captureEvent.isCancelled()) {
                        continue;
                    }
                    api.capturePlayer(onlinePlayer);
                    if (pluginConfig.getSoundSettings().enableSounds()) {
                        Utils.sendSound(pluginConfig.getSoundSettings().onCapture(), onlinePlayer);
                    }
                    if (pluginConfig.getEffectSettings().enableEffects()) {
                        playerManager.giveEffects(onlinePlayer);
                    }
                    playerManager.applyHide(onlinePlayer);
                    if (pluginConfig.getLoggingSettings().loggingPas()) {
                        plugin.logAction(pluginConfig.getLogMessages().captured(), onlinePlayer, LocalDateTime.now());
                    }
                    if (pluginConfig.getBroadcasts() != null) {
                        plugin.sendAlert(onlinePlayer, pluginConfig.getBroadcasts().captured());
                    }
                }
            }
        }, 20L, interval >= 0 ? interval : 30L);
    }

    public void startAdminCheck() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(onlinePlayer) && !plugin.isAdmin(onlinePlayer.getName())) {
                    plugin.checkFail(onlinePlayer.getName(), pluginConfig.getCommands().notInConfig());
                }
            }
        }, 5L, 15L);
    }

    public void startCapturesMessages(FileConfiguration config) {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(onlinePlayer)) {
                    onlinePlayer.sendMessage(pluginConfig.getMessages().message());
                    if (pluginConfig.getMessageSettings().sendTitle()) {
                        Utils.sendTitleMessage(pluginConfig.getTitles().message(), onlinePlayer);
                    }
                }
            }
        }, 5L, config.getInt("message-settings.delay") * 20L);
    }

    public void startOpCheck() {
        runner.runPeriodicalAsync(() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.isOp()
                        && !pluginConfig.getAccessData().opWhitelist().contains(onlinePlayer.getName())
                        && !api.isExcluded(onlinePlayer, pluginConfig.getExcludedPlayers().opWhitelist())) {
                    plugin.checkFail(onlinePlayer.getName(), pluginConfig.getCommands().notInOpWhitelist());
                }
            }
        }, 5L, 15L);
    }

    public void startPermsCheck() {
        runner.runPeriodicalAsync(() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                for (String blacklistedPerm : pluginConfig.getAccessData().blacklistedPerms()) {
                    if (onlinePlayer.hasPermission(blacklistedPerm) &&
                            !api.isExcluded(onlinePlayer, pluginConfig.getExcludedPlayers().blacklistedPerms())) {
                        plugin.checkFail(onlinePlayer.getName(), pluginConfig.getCommands().haveBlacklistedPerm());
                    }
                }
            }
        }, 5L, 15L);
    }

    public void startCapturesTimer() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured()) {
                return;
            }
            BossbarSettings bossbarSettings = pluginConfig.getBossbarSettings();
            int time = pluginConfig.getPunishSettings().time();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.isDead() || !api.isCaptured(onlinePlayer)) {
                    continue;
                }
                String playerName = onlinePlayer.getName();
                Object2IntOpenHashMap<String> perPlayerTime = plugin.getPerPlayerTime();
                int newTime = perPlayerTime.addTo(playerName, 1);
                if (bossbarSettings.enableBossbar()) {
                    int remaining = time - newTime;
                    playerManager.updateBossBar(onlinePlayer, remaining, time);
                }
                if (time - newTime <= 0) {
                    plugin.checkFail(playerName, pluginConfig.getCommands().failedTime());
                    playerManager.removeBossBar(playerName);
                    perPlayerTime.removeInt(playerName);
                }
            }
        }, 5L, 15L);
    }
}
