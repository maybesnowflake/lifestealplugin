package com.lifesteal.utils;

import com.lifesteal.LifeSteal;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemUtils {

    private static final int HEART_MODEL_DATA = 1001;
    private static final int REVIVE_MODEL_DATA = 1002;

    public static ItemStack createHeartItem(int amount) {
        LifeSteal plugin = LifeSteal.getInstance();
        ItemStack item = new ItemStack(Material.NETHER_STAR, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§l하트");
        meta.setLore(List.of("§7우클릭하여 사용", "§7최대 체력이 §c+1§7 증가합니다"));
        meta.setCustomModelData(HEART_MODEL_DATA);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createReviveBook(int amount) {
        ItemStack item = new ItemStack(Material.BOOK, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d§l생환서");
        meta.setLore(List.of("§7우클릭하여 밴된 플레이어 부활시키기"));
        meta.setCustomModelData(REVIVE_MODEL_DATA);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHeartItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;
        return meta.getCustomModelData() == HEART_MODEL_DATA;
    }

    public static boolean isReviveBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;
        return meta.getCustomModelData() == REVIVE_MODEL_DATA;
    }
}
