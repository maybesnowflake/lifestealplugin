package com.lifesteal;

import com.lifesteal.commands.LifeStealAdminCommand;
import com.lifesteal.commands.ReviveCommand;
import com.lifesteal.commands.WhisperCommand;
import com.lifesteal.commands.WithdrawCommand;
import com.lifesteal.gui.ReviveGUI;
import com.lifesteal.gui.RecipeCreatorGUI;
import com.lifesteal.listeners.CombatListener;
import com.lifesteal.listeners.EnchantListener;
import com.lifesteal.listeners.ItemListener;
import com.lifesteal.listeners.PlayerListener;
import com.lifesteal.listeners.RecipeCreatorListener;
import com.lifesteal.listeners.VillagerListener;
import com.lifesteal.managers.BanManager;
import com.lifesteal.managers.CombatManager;
import com.lifesteal.managers.HeartManager;
import com.lifesteal.utils.RecipeUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class LifeSteal extends JavaPlugin {

    private static LifeSteal instance;
    private HeartManager heartManager;
    private CombatManager combatManager;
    private BanManager banManager;
    private ReviveGUI reviveGUI;
    private RecipeCreatorGUI recipeCreatorGUI;
    private boolean lifeStealEnabled;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        lifeStealEnabled = getConfig().getBoolean("lifesteal-enabled", true);

        // 매니저 초기화
        heartManager = new HeartManager(this);
        combatManager = new CombatManager(this);
        banManager = new BanManager(this);
        reviveGUI = new ReviveGUI(this);
        recipeCreatorGUI = new RecipeCreatorGUI(this);

        // 레시피 등록
        RecipeUtil.registerRecipes(this);

        // 리스너 등록
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(reviveGUI, this);
        getServer().getPluginManager().registerEvents(recipeCreatorGUI, this);
        getServer().getPluginManager().registerEvents(new RecipeCreatorListener(this, recipeCreatorGUI), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantListener(this), this);

        // 명령어 등록
        getCommand("withdraw").setExecutor(new WithdrawCommand(this));
        getCommand("w").setExecutor(new WhisperCommand());
        getCommand("lifesteal").setExecutor(new LifeStealAdminCommand(this));
        getCommand("revive").setExecutor(new ReviveCommand(this));

        getLogger().info("LifeSteal 플러그인이 활성화되었습니다!");
    }

    @Override
    public void onDisable() {
        if (combatManager != null) combatManager.saveAll();
        if (heartManager != null) heartManager.saveAll();
        getLogger().info("LifeSteal 플러그인이 비활성화되었습니다.");
    }

    public static LifeSteal getInstance() { return instance; }
    public HeartManager getHeartManager() { return heartManager; }
    public CombatManager getCombatManager() { return combatManager; }
    public BanManager getBanManager() { return banManager; }
    public ReviveGUI getReviveGUI() { return reviveGUI; }
    public RecipeCreatorGUI getRecipeCreatorGUI() { return recipeCreatorGUI; }

    public boolean isLifeStealEnabled() { return lifeStealEnabled; }
    public void setLifeStealEnabled(boolean enabled) {
        this.lifeStealEnabled = enabled;
        getConfig().set("lifesteal-enabled", enabled);
        saveConfig();
    }
}
