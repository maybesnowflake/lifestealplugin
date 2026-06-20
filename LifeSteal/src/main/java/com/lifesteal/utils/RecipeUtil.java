package com.lifesteal.utils;

import com.lifesteal.LifeSteal;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Iterator;
import java.util.List;

public class RecipeUtil {

    public static void registerRecipes(LifeSteal plugin) {
        // 바닐라 제작법 비활성화
        if (plugin.getConfig().getBoolean("recipes.disable-vanilla-mace", true)) {
            removeVanillaRecipe(plugin, "mace");
        }
        if (plugin.getConfig().getBoolean("recipes.disable-vanilla-golden-apple", true)) {
            removeVanillaRecipe(plugin, "golden_apple");
        }
        if (plugin.getConfig().getBoolean("netherite.disable-upgrade-recipe", true)) {
            removeVanillaRecipe(plugin, "netherite_sword");
            removeVanillaRecipe(plugin, "netherite_pickaxe");
            removeVanillaRecipe(plugin, "netherite_axe");
            removeVanillaRecipe(plugin, "netherite_shovel");
            removeVanillaRecipe(plugin, "netherite_hoe");
            removeVanillaRecipe(plugin, "netherite_helmet");
            removeVanillaRecipe(plugin, "netherite_chestplate");
            removeVanillaRecipe(plugin, "netherite_leggings");
            removeVanillaRecipe(plugin, "netherite_boots");
        }

        // 커스텀 레시피 등록
        registerCustomRecipe(plugin, "custom_netherite_sword");
        registerCustomRecipe(plugin, "custom_netherite_chestplate");
        registerCustomRecipe(plugin, "custom_totem");
        registerCustomRecipe(plugin, "custom_mace");
        registerCustomRecipe(plugin, "custom_golden_apple");

        // 추가 커스텀 레시피 (인게임 명령어로 추가된 것들)
        ConfigurationSection extraSection = plugin.getConfig().getConfigurationSection("custom-extra-recipes");
        if (extraSection != null) {
            for (String key : extraSection.getKeys(false)) {
                registerCustomRecipeFromPath(plugin, "custom-extra-recipes." + key, "extra_" + key);
            }
        }
    }

    // Minecraft 바닐라 네임스페이스 레시피 제거
    private static void removeVanillaRecipe(LifeSteal plugin, String key) {
        try {
            NamespacedKey nsKey = NamespacedKey.minecraft(key);
            plugin.getServer().removeRecipe(nsKey);
        } catch (Exception e) {
            plugin.getLogger().warning("바닐라 레시피 제거 실패 (" + key + "): " + e.getMessage());
        }
    }

    private static void registerCustomRecipe(LifeSteal plugin, String recipeKey) {
        String basePath = "recipes." + recipeKey;
        if (!plugin.getConfig().getBoolean(basePath + ".enabled", true)) return;
        registerCustomRecipeFromPath(plugin, basePath, recipeKey);
    }

    private static void registerCustomRecipeFromPath(LifeSteal plugin, String basePath, String recipeId) {
        try {
            String matName = plugin.getConfig().getString(basePath + ".result.material");
            Material resultMat = Material.getMaterial(matName);
            if (resultMat == null) {
                plugin.getLogger().warning("알 수 없는 결과 재료: " + matName + " (" + recipeId + ")");
                return;
            }

            int amount = plugin.getConfig().getInt(basePath + ".result.amount", 1);
            ItemStack result = new ItemStack(resultMat, amount);
            ItemMeta meta = result.getItemMeta();

            String name = plugin.getConfig().getString(basePath + ".result.name");
            if (name != null) meta.setDisplayName(name);

            List<String> lore = plugin.getConfig().getStringList(basePath + ".result.lore");
            if (!lore.isEmpty()) meta.setLore(lore);

            if (plugin.getConfig().contains(basePath + ".result.custom-model-data")) {
                meta.setCustomModelData(plugin.getConfig().getInt(basePath + ".result.custom-model-data"));
            }
            result.setItemMeta(meta);

            NamespacedKey key = new NamespacedKey(plugin, recipeId);
            // 이미 등록되어 있다면 제거 후 재등록 (reload 대응)
            plugin.getServer().removeRecipe(key);

            ShapedRecipe recipe = new ShapedRecipe(key, result);

            List<String> shapeList = plugin.getConfig().getStringList(basePath + ".shape");
            if (shapeList.size() != 3) {
                plugin.getLogger().warning("레시피 shape는 정확히 3줄이어야 합니다: " + recipeId);
                return;
            }
            // 빈 슬롯(공백)은 그대로 두고, ' '문자는 자동으로 빈칸 처리됨
            recipe.shape(shapeList.get(0), shapeList.get(1), shapeList.get(2));

            ConfigurationSection ingredients = plugin.getConfig().getConfigurationSection(basePath + ".ingredients");
            if (ingredients != null) {
                for (String ingKey : ingredients.getKeys(false)) {
                    String ingMatName = ingredients.getString(ingKey);
                    Material ingMat = Material.getMaterial(ingMatName);
                    if (ingMat != null) {
                        recipe.setIngredient(ingKey.charAt(0), ingMat);
                    } else {
                        plugin.getLogger().warning("알 수 없는 재료: " + ingMatName + " (" + recipeId + ")");
                    }
                }
            }

            plugin.getServer().addRecipe(recipe);
            plugin.getLogger().info("레시피 등록됨: " + recipeId);
        } catch (Exception e) {
            plugin.getLogger().warning("레시피 등록 실패 (" + recipeId + "): " + e.getMessage());
        }
    }

    // 인게임에서 새 커스텀 레시피를 추가하는 기능에서 사용
    public static void reloadAllRecipes(LifeSteal plugin) {
        registerRecipes(plugin);
    }
}
