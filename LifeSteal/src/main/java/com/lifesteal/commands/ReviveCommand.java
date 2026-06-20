package com.lifesteal.commands;

import com.lifesteal.LifeSteal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReviveCommand implements CommandExecutor {

    private final LifeSteal plugin;

    public ReviveCommand(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lifesteal.admin")) {
            sender.sendMessage("§c권한이 없습니다.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§c사용법: /revive <플레이어>");
            return true;
        }

        String targetName = args[0];
        plugin.getBanManager().revivePlayer(targetName,
                sender instanceof Player ? (Player) sender : null);
        return true;
    }
}
