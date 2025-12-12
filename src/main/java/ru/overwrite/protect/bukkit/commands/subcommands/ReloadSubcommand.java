package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class ReloadSubcommand extends AbstractSubCommand {

    public ReloadSubcommand(ServerProtectorManager plugin) {
        super(plugin, "reload", "serverprotector.reload", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        FileConfiguration newConfig = pluginConfig.getFile(plugin.getDataFolder().getAbsolutePath(), "config.yml");
        plugin.reloadConfigs(newConfig);
        sender.sendMessage(pluginConfig.getUspMessages().reloaded());
        return true;
    }
}
