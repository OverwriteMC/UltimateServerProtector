package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

import java.util.ArrayList;
import java.util.List;

public class RemopSubcommand extends AbstractSubCommand {

    public RemopSubcommand(ServerProtectorManager plugin) {
        super(plugin, "remop", "serverprotector.remop", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.getUspMessages();
        if (args.length > 1) {
            String nickname = pluginConfig.getStoredNickname(args[1]);
            List<String> wl = new ArrayList<>(plugin.getConfig().getStringList("op-whitelist"));
            boolean removed = false;
            for (int i = wl.size() - 1; i >= 0; i--) {
                if (wl.get(i).equalsIgnoreCase(nickname)) {
                    wl.remove(i);
                    removed = true;
                }
            }
            if (!removed) {
                sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", nickname));
                return true;
            }
            plugin.getConfig().set("op-whitelist", wl);
            plugin.saveConfig();
            plugin.getPluginConfig().loadAccessData(plugin.getConfig());
            sender.sendMessage(uspMessages.playerRemoved().replace("%nick%", nickname));
            return true;
        }
        sendCmdUsage(sender, uspMessages.remOpUsage(), label);
        return true;
    }
}
