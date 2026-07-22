package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.task.runner.Runner;

public class CommandSendListener implements Listener {

    private final ServerProtectorAPI api;
    private final Config pluginConfig;
    private final Runner runner;

    public CommandSendListener(ServerProtectorManager plugin) {
        this.api = plugin.getApi();
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandSend(PlayerCommandSendEvent e) {
        if (pluginConfig.getBlockingSettings().blockTabComplete() && api.isCaptured(e.getPlayer())) {
            e.getCommands().removeIf(command -> !command.equals(pluginConfig.getMainSettings().pasCommand()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSuccessPassword(ServerProtectorPasswordSuccessEvent e) {
        Player player = e.getPlayer();
        runner.runPlayer(player::updateCommands, player);
    }
}
