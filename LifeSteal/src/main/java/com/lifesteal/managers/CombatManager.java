package com.lifesteal.managers;

import com.lifesteal.LifeSteal;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CombatManager {

    private final LifeSteal plugin;
    private final Map<UUID, Long> combatMap = new HashMap<>();
    private final Map<UUID, Long> heartCooldown = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public CombatManager(LifeSteal plugin) {
        this.plugin = plugin;
        startCombatCheckTask();
        startBossBarUpdateTask();
    }

    public int getCombatDuration() {
        return plugin.getConfig().getInt("combat.tag-duration", 20);
    }

    public int getHeartCooldown() {
        return plugin.getConfig().getInt("combat.heart-use-cooldown", 30);
    }

    public void tagPlayer(Player player) {
        long endTime = System.currentTimeMillis() + (getCombatDuration() * 1000L);
        boolean wasTagged = isTagged(player);
        combatMap.put(player.getUniqueId(), endTime);
        if (!wasTagged) {
            player.sendMessage("§c⚔ 전투가 시작되었습니다! §7(" + getCombatDuration() + "초)");
            showBossBar(player);
        }
    }

    public boolean isTagged(Player player) {
        Long endTime = combatMap.get(player.getUniqueId());
        if (endTime == null) return false;
        if (System.currentTimeMillis() >= endTime) {
            combatMap.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public int getRemainingSeconds(Player player) {
        Long endTime = combatMap.get(player.getUniqueId());
        if (endTime == null) return 0;
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    public void untagPlayer(Player player) {
        if (combatMap.remove(player.getUniqueId()) != null) {
            player.sendMessage("§a전투가 종료되었습니다.");
            long cooldownEnd = System.currentTimeMillis() + (getHeartCooldown() * 1000L);
            heartCooldown.put(player.getUniqueId(), cooldownEnd);
            removeBossBar(player);
        }
    }

    public boolean canUseHeart(Player player) {
        if (isTagged(player)) return false;
        Long cooldownEnd = heartCooldown.get(player.getUniqueId());
        if (cooldownEnd == null) return true;
        return System.currentTimeMillis() >= cooldownEnd;
    }

    public int getHeartCooldownRemaining(Player player) {
        Long cooldownEnd = heartCooldown.get(player.getUniqueId());
        if (cooldownEnd == null) return 0;
        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    private void showBossBar(Player player) {
        removeBossBar(player);
        BossBar bar = Bukkit.createBossBar("§c⚔ 전투 중 | " + getCombatDuration() + "초", BarColor.RED, BarStyle.SOLID);
        bar.addPlayer(player);
        bar.setProgress(1.0);
        bossBars.put(player.getUniqueId(), bar);
    }

    private void removeBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) bar.removeAll();
    }

    private void startBossBarUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, BossBar> entry : new HashMap<>(bossBars).entrySet()) {
                    Player p = Bukkit.getPlayer(entry.getKey());
                    if (p == null) continue;
                    int remaining = getRemainingSeconds(p);
                    int total = getCombatDuration();
                    double progress = Math.max(0.0, Math.min(1.0, (double) remaining / total));
                    entry.getValue().setProgress(progress);
                    entry.getValue().setTitle("§c⚔ 전투 중 | §f" + remaining + "§c초");
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startCombatCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                combatMap.entrySet().removeIf(entry -> {
                    if (now >= entry.getValue()) {
                        Player p = plugin.getServer().getPlayer(entry.getKey());
                        if (p != null) {
                            p.sendMessage("§a전투가 종료되었습니다.");
                            long cooldownEnd = now + (getHeartCooldown() * 1000L);
                            heartCooldown.put(entry.getKey(), cooldownEnd);
                            removeBossBar(p);
                        }
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void saveAll() {
        for (BossBar bar : bossBars.values()) bar.removeAll();
        bossBars.clear();
        combatMap.clear();
    }
}
