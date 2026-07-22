package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

import java.util.List;

public class AddipSubcommand extends AbstractSubCommand {

    public AddipSubcommand(ServerProtectorManager plugin) {
        super(plugin, "addip", "serverprotector.addip", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.getUspMessages();
        if (args.length > 2) {
            String nickname = pluginConfig.getStoredNickname(args[1]);
            List<String> ips = List.of(args).subList(2, args.length);
            List<String> ipwl = plugin.getConfig().getStringList("ip-whitelist." + nickname);
            boolean added = false;
            for (String ip : ips) {
                if (!ipwl.contains(ip)) {
                    ipwl.add(ip);
                    added = true;
                }
            }
            if (!added) {
                sender.sendMessage(uspMessages.alreadyInConfig());
                return true;
            }
            plugin.getConfig().set("ip-whitelist." + nickname, ipwl);
            plugin.saveConfig();
            plugin.getPluginConfig().loadAccessData(plugin.getConfig());
            sender.sendMessage(uspMessages.ipAdded().replace("%nick%", nickname).replace("%ip%", ips.toString()));
            return true;
        }
        sendCmdUsage(sender, uspMessages.addIpUsage(), label);
        return true;
    }
}
