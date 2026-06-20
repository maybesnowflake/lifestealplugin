package com.lifesteal.listeners;

import com.lifesteal.LifeSteal;
import com.lifesteal.managers.BanManager;
import com.lifesteal.managers.CombatManager;
import com.lifesteal.managers.HeartManager;
import com.lifesteal.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PlayerListener implements Listener {

    private final LifeSteal plugin;
    private final HeartManager heartManager;
    private final CombatManager combatManager;
    private final BanManager banManager;
    private final Random random = new Random();

    public PlayerListener(LifeSteal plugin) {
        this.plugin = plugin;
        this.heartManager = plugin.getHeartManager();
        this.combatManager = plugin.getCombatManager();
        this.banManager = plugin.getBanManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        heartManager.initPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.isLifeStealEnabled()) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        Location deathLoc = victim.getLocation();

        boolean shouldLoseHeart = true;
        if (killer == null && !heartManager.loseOnNaturalDeath()) {
            shouldLoseHeart = false;
        }

        if (shouldLoseHeart) {
            // 킬러가 최대 체력인 경우 → 하트 아이템으로 드롭
            if (killer != null && heartManager.getHearts(killer) >= heartManager.getMaxHearts()) {
                boolean survived = heartManager.removeHeart(victim);
                dropHeartItem(deathLoc);
                killer.sendMessage("§c이미 최대 체력이라 하트가 바닥에 드롭되었습니다!");
                combatManager.untagPlayer(killer);
                combatManager.untagPlayer(victim);
                if (!survived) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> banManager.banPlayer(victim), 20L);
                }
                applyDeathDropRadius(event, deathLoc);
                return;
            }

            boolean survived = heartManager.removeHeart(victim);
            if (!survived) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> banManager.banPlayer(victim), 20L);
            }
        }

        if (killer != null) {
            heartManager.addHeart(killer);
            combatManager.untagPlayer(killer);
            combatManager.untagPlayer(victim);
        }

        applyDeathDropRadius(event, deathLoc);
    }

    private void applyDeathDropRadius(PlayerDeathEvent event, Location center) {
        double radius = plugin.getConfig().getDouble("death.drop-radius", 0);
        if (radius <= 0) return;

        for (ItemStack drop : event.getDrops()) {
            if (drop == null || drop.getType() == Material.AIR) continue;
            double offsetX = (random.nextDouble() * 2 - 1) * radius;
            double offsetZ = (random.nextDouble() * 2 - 1) * radius;
            Location dropLoc = center.clone().add(offsetX, 0, offsetZ);
            center.getWorld().dropItemNaturally(dropLoc, drop);
        }
        event.getDrops().clear();
    }

    private void dropHeartItem(Location location) {
        ItemStack heart = ItemUtils.createHeartItem(1);
        location.getWorld().dropItemNaturally(location, heart);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                heartManager.applyHearts(event.getPlayer(), heartManager.getHearts(event.getPlayer())), 5L);
    }

    // Combat Tag 중 로그아웃 → 사망 처리
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isLifeStealEnabled()) return;
        Player player = event.getPlayer();
        if (combatManager.isTagged(player) && !player.hasPermission("lifesteal.bypass.combat")) {
            plugin.getServer().broadcastMessage("§c[LifeSteal] §f" + player.getName() + " §c님이 전투 중 로그아웃하여 사망 처리되었습니다!");
            player.setHealth(0);
            boolean survived = heartManager.removeHeart(player);
            heartManager.saveAll();
            if (!survived) banManager.banPlayer(player);
        }
        combatManager.untagPlayer(player);
    }

    // ────────────────────────────────────────────
    // 급류 삼지창: Combat Tag 중에만 차단 (설정 가능, combat.restrict-riptide)
    // ────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRiptideUse(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("combat.restrict-riptide", true)) return;

        Player player = event.getPlayer();
        if (!combatManager.isTagged(player)) return; // 전투 중이 아니면 허용

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.TRIDENT) return;
        if (!item.hasItemMeta()) return;
        if (item.getItemMeta().getEnchants().containsKey(org.bukkit.enchantments.Enchantment.RIPTIDE)) {
            event.setCancelled(true);
            player.sendMessage("§c전투 중에는 급류 삼지창을 사용할 수 없습니다! §7(" + combatManager.getRemainingSeconds(player) + "초 남음)");
        }
    }
}
