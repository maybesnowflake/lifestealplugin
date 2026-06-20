package com.lifesteal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhisperCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c플레이어만 사용할 수 있습니다.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§c사용법: /w <플레이어> <메시지>");
            return true;
        }

        Player from = (Player) sender;
        Player to = from.getServer().getPlayer(args[0]);

        if (to == null || !to.isOnline()) {
            from.sendMessage("§c" + args[0] + " 님을 찾을 수 없습니다.");
            return true;
        }
        if (to.equals(from)) {
            from.sendMessage("§c자신에게 귓속말할 수 없습니다.");
            return true;
        }

        StringBuilder msg = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) msg.append(" ");
            msg.append(args[i]);
        }

        String message = msg.toString();
        from.sendMessage("§7[나 → §f" + to.getName() + "§7] §f" + message);
        to.sendMessage("§7[§f" + from.getName() + "§7 → 나] §f" + message);
        return true;
    }
}
