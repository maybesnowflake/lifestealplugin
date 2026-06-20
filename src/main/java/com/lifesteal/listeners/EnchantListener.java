package com.lifesteal.listeners;

import com.lifesteal.LifeSteal;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantListener implements Listener {

    private final LifeSteal plugin;

    public EnchantListener(LifeSteal plugin) {
        this.plugin = plugin;
    }

    private boolean isFireAspectDisabled() {
        return plugin.getConfig().getBoolean("enchants.remove-fire-aspect", true);
    }

    // 인첸트 테이블에서 발화 인첸트 부여 차단
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (!isFireAspectDisabled()) return;
        if (event.getEnchantsToAdd().containsKey(Enchantment.FIRE_ASPECT)) {
            event.getEnchantsToAdd().remove(Enchantment.FIRE_ASPECT);
        }
    }

    // 모루(주문서)로 발화 인첸트 부여 차단
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!isFireAspectDisabled()) return;
        ItemStack result = event.getResult();
        if (result == null) return;
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;
        if (meta.hasEnchant(Enchantment.FIRE_ASPECT)) {
            meta.removeEnchant(Enchantment.FIRE_ASPECT);
            result.setItemMeta(meta);
            event.setResult(result);
        }
    }
}
