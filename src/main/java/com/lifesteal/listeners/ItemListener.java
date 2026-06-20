package com.lifesteal.listeners;

import com.lifesteal.LifeSteal;
import com.lifesteal.managers.CombatManager;
import com.lifesteal.managers.HeartManager;
import com.lifesteal.utils.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;   // ← 이 줄 추가
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;


public class ItemListener implements Listener {

    private final LifeSteal plugin;
    private final HeartManager heartManager;
    private final CombatManager combatManager;

    public ItemListener(LifeSteal plugin) {
        this.plugin = plugin;
        this.heartManager = plugin.getHeartManager();
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isLifeStealEnabled()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // 하트 아이템 확인
        if (ItemUtils.isHeartItem(item)) {
            event.setCancelled(true);

            if (!combatManager.canUseHeart(player)) {
                if (combatManager.isTagged(player)) {
                    player.sendMessage("§c전투 중에는 하트를 사용할 수 없습니다! §7(" + combatManager.getRemainingSeconds(player) + "초 남음)");
                } else {
                    player.sendMessage("§c아직 하트를 사용할 수 없습니다! §7(" + combatManager.getHeartCooldownRemaining(player) + "초 후 가능)");
                }
                return;
            }

            heartManager.addHeart(player);
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }

        // 생환서 아이템 확인 → GUI 열기
        if (ItemUtils.isReviveBook(item)) {
            event.setCancelled(true);
            plugin.getReviveGUI().openReviveGUI(player);
        }
    }
}
