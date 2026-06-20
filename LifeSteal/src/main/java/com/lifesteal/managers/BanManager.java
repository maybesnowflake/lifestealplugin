package com.lifesteal.managers;

import com.lifesteal.LifeSteal;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BanManager {

    private final LifeSteal plugin;

    public BanManager(LifeSteal plugin) {
        this.plugin = plugin;
    }

    public void banPlayer(Player player) {
        String message = plugin.getConfig().getString("ban.message", "§c체력이 0이 되어 밴되었습니다!");
        plugin.getServer().getBanList(BanList.Type.NAME)
                .addBan(player.getName(), message, null, "LifeSteal");
        player.kickPlayer(message);
        plugin.getServer().broadcastMessage("§c§l[LifeSteal] §f" + player.getName() + " §c님이 탈락했습니다!");
    }

    public boolean isBanned(OfflinePlayer player) {
        return plugin.getServer().getBanList(BanList.Type.NAME).isBanned(player.getName());
    }

    public boolean revivePlayer(String playerName, Player admin) {
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(playerName);
        if (!isBanned(target)) {
            if (admin != null) admin.sendMessage("§c" + playerName + " 님은 밴 상태가 아닙니다.");
            return false;
        }
        plugin.getServer().getBanList(BanList.Type.NAME).pardon(playerName);
        plugin.getServer().broadcastMessage("§a§l[LifeSteal] §f" + playerName + " §a님이 부활했습니다!");
        if (admin != null) admin.sendMessage("§a" + playerName + " 님의 밴이 해제되었습니다.");
        return true;
    }
}
