package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AddopSubcommand extends AbstractSubCommand {

    public AddopSubcommand(ServerProtectorManager plugin) {
        super(plugin, "addop", "serverprotector.addop", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.getUspMessages();
        if (args.length > 1) {
            String nickname = pluginConfig.normalizeGeyserNickname(args[1]);
            OfflinePlayer targetPlayer = findOfflinePlayer(nickname);
            if (targetPlayer == null) {
                sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", nickname));
                return true;
            }
            nickname = pluginConfig.getStoredNickname(targetPlayer.getName());
            List<String> whitelist = new ArrayList<>(plugin.getConfig().getStringList("op-whitelist"));
            for (String whitelistedPlayer : whitelist) {
                if (whitelistedPlayer.equalsIgnoreCase(nickname)) {
                    sender.sendMessage(uspMessages.alreadyInConfig());
                    return true;
                }
            }
            whitelist.add(nickname);
            plugin.getConfig().set("op-whitelist", whitelist);
            plugin.saveConfig();
            plugin.getPluginConfig().loadAccessData(plugin.getConfig());
            sender.sendMessage(uspMessages.playerAdded().replace("%nick%", nickname));
            return true;
        }

        sendCmdUsage(sender, uspMessages.addOpUsage(), label);
        return true;
    }

    private OfflinePlayer findOfflinePlayer(String nickname) {
        if (Utils.SUB_VERSION >= 16 && plugin.isPaper()) {
            return Bukkit.getOfflinePlayerIfCached(nickname);
        }
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            String playerName = offlinePlayer.getName();
            if (playerName != null && playerName.equalsIgnoreCase(nickname)) {
                return offlinePlayer;
            }
        }
        return null;
    }
}
