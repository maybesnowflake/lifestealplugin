package com.lifesteal.listeners;

import com.lifesteal.LifeSteal;
import com.lifesteal.gui.RecipeCreatorGUI;
import com.lifesteal.utils.RecipeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeCreatorListener implements Listener {

    private final LifeSteal plugin;
    private final RecipeCreatorGUI gui;

    public RecipeCreatorListener(LifeSteal plugin, RecipeCreatorGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(RecipeCreatorGUI.getGuiTitle())) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        int slot = event.getRawSlot();

        // 이름표 슬롯 클릭 → 채팅 입력 대기
        if (slot == RecipeCreatorGUI.getNameDisplaySlot()) {
            event.setCancelled(true);
            gui.setPendingNameInput(player);
            player.closeInventory();
            player.sendMessage("§e채팅으로 레시피 이름을 입력해주세요. §7(취소: 'cancel' 입력)");
            return;
        }

        // 저장 버튼 클릭
        if (slot == RecipeCreatorGUI.getSaveButtonSlot()) {
            event.setCancelled(true);
            handleSave(player, event.getView().getTopInventory());
            return;
        }

        // 제작 그리드(0~8 매핑 슬롯)나 결과 슬롯은 자유롭게 아이템 배치/제거 허용
        boolean isCraftSlot = false;
        for (int s : RecipeCreatorGUI.getCraftSlots()) if (s == slot) isCraftSlot = true;
        boolean isResultSlot = slot == RecipeCreatorGUI.getResultSlot();

        if (isCraftSlot || isResultSlot) {
            return; // 허용 (취소하지 않음)
        }

        // 그 외 슬롯(테두리, 안내 아이템)은 클릭 차단
        event.setCancelled(true);
    }

    private void handleSave(Player player, org.bukkit.inventory.Inventory topInv) {
        String name = gui.getRecipeName(player);
        if (name == null || name.isBlank()) {
            player.sendMessage("§c먼저 레시피 이름을 설정해주세요! (이름표 클릭)");
            return;
        }

        ItemStack result = topInv.getItem(RecipeCreatorGUI.getResultSlot());
        if (result == null || result.getType() == Material.AIR) {
            player.sendMessage("§c결과 슬롯에 완성될 아이템을 넣어주세요!");
            return;
        }

        int[] craftSlots = RecipeCreatorGUI.getCraftSlots();
        Material[] grid = new Material[9];
        boolean hasAnyIngredient = false;
        for (int i = 0; i < 9; i++) {
            ItemStack item = topInv.getItem(craftSlots[i]);
            if (item == null || item.getType() == Material.AIR) {
                grid[i] = null;
            } else {
                grid[i] = item.getType();
                hasAnyIngredient = true;
            }
        }

        if (!hasAnyIngredient) {
            player.sendMessage("§c제작 그리드에 최소 1개 이상의 재료를 배치해주세요!");
            return;
        }

        // shape 문자 매핑
        char[] letters = new char[9];
        Map<Material, Character> matToChar = new LinkedHashMap<>();
        char nextChar = 'A';
        for (int i = 0; i < 9; i++) {
            if (grid[i] == null) {
                letters[i] = ' ';
                continue;
            }
            if (!matToChar.containsKey(grid[i])) {
                matToChar.put(grid[i], nextChar++);
            }
            letters[i] = matToChar.get(grid[i]);
        }

        String row1 = "" + letters[0] + letters[1] + letters[2];
        String row2 = "" + letters[3] + letters[4] + letters[5];
        String row3 = "" + letters[6] + letters[7] + letters[8];

        String basePath = "custom-extra-recipes." + name;
        plugin.getConfig().set(basePath + ".shape", List.of(row1, row2, row3));
        for (Map.Entry<Material, Character> entry : matToChar.entrySet()) {
            plugin.getConfig().set(basePath + ".ingredients." + entry.getValue(), entry.getKey().name());
        }
        plugin.getConfig().set(basePath + ".result.material", result.getType().name());
        plugin.getConfig().set(basePath + ".result.amount", result.getAmount());

        if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
            plugin.getConfig().set(basePath + ".result.name", result.getItemMeta().getDisplayName());
        } else {
            plugin.getConfig().set(basePath + ".result.name", "§f" + name);
        }
        if (result.hasItemMeta() && result.getItemMeta().hasLore()) {
            plugin.getConfig().set(basePath + ".result.lore", result.getItemMeta().getLore());
        }

        plugin.saveConfig();
        RecipeUtil.reloadAllRecipes(plugin);

        player.sendMessage("§a✔ 레시피 '" + name + "' 가 성공적으로 저장되었습니다!");
        player.closeInventory();
        gui.clearRecipeName(player);
    }

    // 채팅으로 레시피 이름 입력 받기
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!gui.isPendingNameInput(player)) return;

        event.setCancelled(true);
        gui.clearPendingNameInput(player);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage("§7이름 설정을 취소했습니다.");
            plugin.getServer().getScheduler().runTask(plugin, () -> gui.open(player));
            return;
        }

        gui.setRecipeName(player, message);
        player.sendMessage("§a레시피 이름이 '" + message + "' 로 설정되었습니다.");
        plugin.getServer().getScheduler().runTask(plugin, () -> gui.open(player));
    }
}
