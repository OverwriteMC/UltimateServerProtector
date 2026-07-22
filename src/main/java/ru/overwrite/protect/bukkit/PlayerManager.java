package ru.overwrite.protect.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.configuration.data.BlockingSettings;
import ru.overwrite.protect.bukkit.configuration.data.BossbarSettings;
import ru.overwrite.protect.bukkit.task.runner.Runner;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {

    private final ServerProtectorManager plugin;
    private final Runner runner;
    private final Config config;

    private final Map<String, Collection<PotionEffect>> oldEffects = new IdentityHashMap<>();
    private final Map<String, BossBar> bossbars = new IdentityHashMap<>();

    public PlayerManager(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.runner = plugin.getRunner();
        this.config = plugin.getPluginConfig();
    }

    public void giveEffects(Player player) {
        runner.runPlayer(() -> {
            Collection<PotionEffect> effects = player.getActivePotionEffects();
            if (!effects.isEmpty()) {
                oldEffects.put(player.getName(), effects);
            }
            player.addPotionEffects(config.getEffectSettings().effects());
        }, player);
    }

    public void removeEffects(Player player) {
        runner.runPlayer(() -> {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            if (oldEffects.isEmpty()) {
                return;
            }
            Collection<PotionEffect> effects = oldEffects.remove(player.getName());
            if (effects != null) {
                player.addPotionEffects(effects);
            }
        }, player);
    }

    public void applyHide(Player player) {
        runner.runPlayer(() -> {
            BlockingSettings blockingSettings = config.getBlockingSettings();
            if (!blockingSettings.hideOnEntering() && !blockingSettings.hideOtherOnEntering()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (blockingSettings.hideOnEntering()) {
                    onlinePlayer.hidePlayer(plugin, player);
                }
                if (blockingSettings.hideOtherOnEntering()) {
                    player.hidePlayer(plugin, onlinePlayer);
                }
            }
        }, player);
    }

    public void checkIpWhitelist(String playerName, String playerIp) {
        if (!config.getSecureSettings().enableIpWhitelist()) {
            return;
        }
        if (!isIpAllowed(playerIp, config.getAccessData().ipWhitelist().get(playerName))
                && !plugin.getApi().isExcluded(playerName, config.getExcludedPlayers().ipWhitelist())) {
            plugin.checkFail(playerName, config.getCommands().notAdminIp());
        }
    }

    private boolean isIpAllowed(String playerIp, List<String> allowedIps) {
        if (allowedIps == null || allowedIps.isEmpty()) {
            return false;
        }

        outer:
        for (int i = 0; i < allowedIps.size(); i++) {
            final String allowedIp = allowedIps.get(i);
            int playerIpLength = playerIp.length();
            int allowedIpLength = allowedIp.length();

            if (playerIpLength != allowedIpLength && !allowedIp.contains("*")) {
                continue;
            }

            for (int n = 0; n < allowedIpLength; n++) {
                char currentChar = allowedIp.charAt(n);
                if (currentChar == '*') {
                    return true;
                }
                if (n >= playerIpLength || currentChar != playerIp.charAt(n)) {
                    continue outer;
                }
            }
            if (playerIpLength == allowedIpLength) {
                return true;
            }
        }
        return false;
    }

    public void showPlayer(Player player) {
        runner.runPlayer(() -> {
            BlockingSettings blockingSettings = config.getBlockingSettings();
            if (!blockingSettings.hideOnEntering() && !blockingSettings.hideOtherOnEntering()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (blockingSettings.hideOnEntering()) {
                    onlinePlayer.showPlayer(plugin, player);
                }
                if (blockingSettings.hideOtherOnEntering()) {
                    player.showPlayer(plugin, onlinePlayer);
                }
            }
        }, player);
    }

    public void addBossBar(String playerName, int totalTime) {
        BossbarSettings blockingSettings = config.getBossbarSettings();
        BossBar bossBar = Bukkit.createBossBar(
                blockingSettings.bossbarMessage().replace("%time%", Integer.toString(totalTime)),
                blockingSettings.barColor(),
                blockingSettings.barStyle()
        );
        bossbars.put(playerName, bossBar);
    }

    public void updateBossBar(Player player, int remainingTime, int totalTime) {
        String playerName = player.getName();
        BossBar bossBar = bossbars.get(playerName);
        if (bossBar == null) {
            addBossBar(playerName, totalTime);
            bossBar = bossbars.get(playerName);
        }
        bossBar.setTitle(config.getBossbarSettings().bossbarMessage().replace("%time%", Integer.toString(remainingTime)));
        double progress = remainingTime / (double) totalTime;
        if (progress > 0) {
            bossBar.setProgress(progress);
            bossBar.addPlayer(player);
        } else {
            removeBossBar(playerName);
        }
    }

    public void removeBossBar(String playerName) {
        BossBar bossBar = bossbars.remove(playerName);
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void clearBossBars() {
        for (BossBar bossBar : bossbars.values()) {
            bossBar.removeAll();
        }
        bossbars.clear();
    }
}
