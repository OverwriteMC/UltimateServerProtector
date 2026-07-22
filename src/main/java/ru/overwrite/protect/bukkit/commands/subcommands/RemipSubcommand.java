package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

import java.util.List;

public class RemipSubcommand extends AbstractSubCommand {

    public RemipSubcommand(ServerProtectorManager plugin) {
        super(plugin, "remip", "serverprotector.remip", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.getUspMessages();
        if (args.length > 2) {
            String nickname = pluginConfig.getStoredNickname(args[1]);
            List<String> ips = List.of(args).subList(2, args.length);
            List<String> ipwl = plugin.getConfig().getStringList("ip-whitelist." + nickname);
            if (ipwl.isEmpty()) {
                sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", nickname));
                return true;
            }
            boolean removed = false;
            for (String ip : ips) {
                while (ipwl.remove(ip)) {
                    removed = true;
                }
            }
            if (!removed) {
                sender.sendMessage(uspMessages.notInConfig());
                return true;
            }
            plugin.getConfig().set("ip-whitelist." + nickname, ipwl);
            plugin.saveConfig();
            plugin.getPluginConfig().loadAccessData(plugin.getConfig());
            sender.sendMessage(uspMessages.ipRemoved().replace("%nick%", nickname).replace("%ip%", ips.toString()));
            return true;
        }
        sendCmdUsage(sender, uspMessages.remIpUsage(), label);
        return true;
    }
}
