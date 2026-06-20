package com.lifesteal.managers;

import com.lifesteal.LifeSteal;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class HeartManager {

    private final LifeSteal plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    // 하트 1개 = 2 HP (체력 포인트)
    private static final double HP_PER_HEART = 2.0;

    public HeartManager(LifeSteal plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "hearts.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    // ────────────────────────────────────────────
    // 하트 조회
    // ────────────────────────────────────────────
    public int getHearts(Player player) {
        String key = player.getUniqueId().toString();
        if (!dataConfig.contains(key)) {
            int defaultHearts = plugin.getConfig().getInt("hearts.default", 10);
            setHearts(player, defaultHearts);
            return defaultHearts;
        }
        return dataConfig.getInt(key);
    }

    // ────────────────────────────────────────────
    // 하트 설정 (최소/최대 범위 적용)
    // ────────────────────────────────────────────
    public void setHearts(Player player, int hearts) {
        int min = plugin.getConfig().getInt("hearts.min", 1);
        int max = plugin.getConfig().getInt("hearts.max", 20);
        hearts = Math.max(min, Math.min(max, hearts));

        dataConfig.set(player.getUniqueId().toString(), hearts);
        saveData();
        applyMaxHealth(player, hearts);
    }

    // ────────────────────────────────────────────
    // 하트 추가 / 제거
    // ────────────────────────────────────────────
    public void addHeart(Player player) {
        setHearts(player, getHearts(player) + 1);
    }

    public void removeHeart(Player player) {
        setHearts(player, getHearts(player) - 1);
    }

    // ────────────────────────────────────────────
    // 최대 체력 적용
    // 1.21.4+ 부터 Attribute.GENERIC_MAX_HEALTH → Attribute.MAX_HEALTH 로 변경됨
    // ────────────────────────────────────────────
    private void applyMaxHealth(Player player, int hearts) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        double newMax = hearts * HP_PER_HEART;
        attr.setBaseValue(newMax);

        // 현재 체력이 새 최대치를 초과하면 조정
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }

    // ────────────────────────────────────────────
    // 로그인 시 체력 동기화
    // ────────────────────────────────────────────
    public void syncOnJoin(Player player) {
        applyMaxHealth(player, getHearts(player));
    }

    // ────────────────────────────────────────────
    // 저장
    // ────────────────────────────────────────────
    public void saveAll() {
        saveData();
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
