package com.lifesteal.gui;

import com.lifesteal.LifeSteal;
import com.lifesteal.managers.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ReviveGUI implements Listener {

    private final LifeSteal plugin;
    private final BanManager banManager;
    private static final String GUI_TITLE = "§d§l생환서 - 부활할 플레이어 선택";

    public ReviveGUI(LifeSteal plugin) {
        this.plugin = plugin;
        this.banManager = plugin.getBanManager();
    }

    public void openReviveGUI(Player player) {
        List<OfflinePlayer> bannedPlayers = new ArrayList<>();
        for (OfflinePlayer op : Bukkit.getBannedPlayers()) {
            bannedPlayers.add(op);
        }

        if (bannedPlayers.isEmpty()) {
            player.sendMessage("§a현재 밴된 플레이어가 없습니다!");
            return;
        }

        int size = Math.min(54, (int) Math.ceil(bannedPlayers.size() / 9.0) * 9);
        if (size < 9) size = 9;
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        for (int i = 0; i < Math.min(bannedPlayers.size(), size); i++) {
            OfflinePlayer banned = bannedPlayers.get(i);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(banned);
            meta.setDisplayName("§f" + banned.getName());
            meta.setLore(Arrays.asList(
                    "§7클릭하여 부활시키기",
                    "§c§l밴된 플레이어"
            ));
            skull.setItemMeta(meta);
            gui.setItem(i, skull);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String targetName = clicked.getItemMeta().getDisplayName().replace("§f", "");

        clicker.closeInventory();
        boolean success = banManager.revivePlayer(targetName, clicker);

        if (success) {
            consumeReviveBook(clicker);
        }
    }

    private void consumeReviveBook(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && com.lifesteal.utils.ItemUtils.isReviveBook(item)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItem(i, null);
                }
                return;
            }
        }
    }
}
