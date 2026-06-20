package com.lifesteal.listeners;

import com.lifesteal.LifeSteal;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.MerchantRecipe;

public class VillagerListener implements Listener {

    private final LifeSteal plugin;

    public VillagerListener(LifeSteal plugin) {
        this.plugin = plugin;
    }

    // 주민과 상호작용 시 거래 횟수 초기화 (무제한 거래)
    @EventHandler
    public void onVillagerInteract(PlayerInteractEntityEvent event) {
        if (!plugin.getConfig().getBoolean("villager.unlimited-trades", true)) return;
        if (!(event.getRightClicked() instanceof Villager)) return;

        Villager villager = (Villager) event.getRightClicked();
        resetTrades(villager);
    }

    // 새로운 거래 항목 획득 시에도 무제한 처리 적용
    @EventHandler
    public void onTradeAcquire(VillagerAcquireTradeEvent event) {
        if (!plugin.getConfig().getBoolean("villager.unlimited-trades", true)) return;
        if (!(event.getEntity() instanceof Villager)) return;

        MerchantRecipe recipe = event.getRecipe();
        MerchantRecipe newRecipe = new MerchantRecipe(
                recipe.getResult(),
                0, // uses
                Integer.MAX_VALUE, // maxUses - 무제한
                recipe.hasExperienceReward(),
                recipe.getVillagerExperience(),
                recipe.getPriceMultiplier()
        );
        newRecipe.setIngredients(recipe.getIngredients());
        event.setRecipe(newRecipe);
    }

    private void resetTrades(Villager villager) {
        java.util.List<MerchantRecipe> recipes = new java.util.ArrayList<>(villager.getRecipes());
        java.util.List<MerchantRecipe> newRecipes = new java.util.ArrayList<>();
        for (MerchantRecipe r : recipes) {
            MerchantRecipe newRecipe = new MerchantRecipe(
                    r.getResult(),
                    0,
                    Integer.MAX_VALUE,
                    r.hasExperienceReward(),
                    r.getVillagerExperience(),
                    r.getPriceMultiplier()
            );
            newRecipe.setIngredients(r.getIngredients());
            newRecipes.add(newRecipe);
        }
        villager.setRecipes(newRecipes);
    }
}
