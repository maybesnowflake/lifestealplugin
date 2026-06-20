package com.lifesteal.listeners;

import com.lifesteal.LifeSteal;
import com.lifesteal.managers.CombatManager;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class CombatListener implements Listener {

    private final LifeSteal plugin;
    private final CombatManager combatManager;

    private static final Set<Material> NETHERITE_EQUIPMENT = EnumSet.of(
            Material.NETHERITE_SWORD,
            Material.NETHERITE_PICKAXE,
            Material.NETHERITE_AXE,
            Material.NETHERITE_SHOVEL,
            Material.NETHERITE_HOE,
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS
    );

    public CombatListener(LifeSteal plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    // ────────────────────────────────────────────
    // PvP 감지 → Combat Tag
    // ────────────────────────────────────────────
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.isLifeStealEnabled()) return;

        Player victim = null;
        Player attacker = null;

        if (event.getEntity() instanceof Player) {
            victim = (Player) event.getEntity();
        }
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }

        // 엔드 크리스탈 PvP 금지
        if (event.getDamager() instanceof EnderCrystal && victim != null) {
            event.setCancelled(true);
            victim.sendMessage("§c엔드 크리스탈로 PvP할 수 없습니다!");
            return;
        }

        if (victim != null && attacker != null && !victim.equals(attacker)) {
            combatManager.tagPlayer(victim);
            combatManager.tagPlayer(attacker);
        }
    }

    // ────────────────────────────────────────────
    // 겉날개: Combat Tag 중에만 차단 (combat.restrict-elytra)
    // ────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!plugin.getConfig().getBoolean("combat.restrict-elytra", true)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!combatManager.isTagged(player)) return;
        if (event.isGliding()) {
            event.setCancelled(true);
            player.sendMessage("§c전투 중에는 겉날개를 사용할 수 없습니다! §7(" + combatManager.getRemainingSeconds(player) + "초 남음)");
        }
    }

    // ────────────────────────────────────────────
    // 엔더상자: Combat Tag 중에만 차단 (combat.restrict-ender-chest)
    // ────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.getConfig().getBoolean("combat.restrict-ender-chest", true)) return;
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (!combatManager.isTagged(player)) return;
        if (event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            event.setCancelled(true);
            player.sendMessage("§c전투 중에는 엔더상자를 열 수 없습니다! §7(" + combatManager.getRemainingSeconds(player) + "초 남음)");
        }
    }

    // ────────────────────────────────────────────
    // 네더라이트 장비: 손에 들고 사용(우클릭 등) 차단
    // ────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isNetheriteDisabled()) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        if (NETHERITE_EQUIPMENT.contains(item.getType())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c네더라이트 장비는 이 서버에서 사용할 수 없습니다!");
        }
    }

    // ────────────────────────────────────────────
    // 네더라이트 장비: 인벤토리 클릭(착용/이동) 차단
    // SmithingInventory는 SmithItemEvent에서 별도 처리하므로 여기선 제외
    // ────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickNetherite(InventoryClickEvent event) {
        if (!isNetheriteDisabled()) return;
        if (event.getInventory().getType() == InventoryType.SMITHING) return; // SmithItemEvent에서 처리

        ItemStack current = event.getCurrentItem();
        ItemStack cursor  = event.getCursor();

        if ((current != null && NETHERITE_EQUIPMENT.contains(current.getType())) ||
            (cursor  != null && NETHERITE_EQUIPMENT.contains(cursor.getType()))) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage("§c네더라이트 장비는 이 서버에서 사용할 수 없습니다!");
            }
        }
    }

    // ────────────────────────────────────────────
    // 네더라이트 장비: 대장장이 작업대 제작 완료 차단
    // SmithItemEvent = 결과 슬롯에서 아이템을 실제로 꺼낼 때 발생
    // 1.21.11 기준 가장 안정적인 방법
    // ────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSmithItem(SmithItemEvent event) {
        if (!isNetheriteDisabled()) return;
        ItemStack result = event.getCurrentItem();
        if (result == null) return;
        if (NETHERITE_EQUIPMENT.contains(result.getType())) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage("§c네더라이트 장비는 이 서버에서 제작할 수 없습니다!");
            }
        }
    }

    // ────────────────────────────────────────────
    // 드래곤알을 엔더상자에 보관 금지
    // ────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) return;

        ItemStack cursor  = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        boolean isDragonEggCursor  = cursor  != null && cursor.getType()  == Material.DRAGON_EGG;
        boolean isDragonEggClicked = clicked != null && clicked.getType() == Material.DRAGON_EGG;

        if (isDragonEggCursor || isDragonEggClicked) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("§c드래곤알은 엔더상자에 보관할 수 없습니다!");
        }
    }

    // ────────────────────────────────────────────
    // 헬퍼
    // ────────────────────────────────────────────
    private boolean isNetheriteDisabled() {
        return !plugin.getConfig().getBoolean("netherite.enabled", true);
    }
}
