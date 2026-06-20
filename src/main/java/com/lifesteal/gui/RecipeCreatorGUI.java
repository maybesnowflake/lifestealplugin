package com.lifesteal.gui;

import com.lifesteal.LifeSteal;
import com.lifesteal.utils.RecipeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 레시피 제작 GUI - 실제 작업대처럼 9칸(3x3) + 결과칸 1개로 구성된 27칸 인벤토리.
 * 관리자가 슬롯에 아이템을 직접 배치하고 결과 슬롯에 완성 아이템을 넣은 뒤
 * "저장" 버튼(초록 유리판)을 클릭하면 레시피가 등록됩니다.
 *
 * 슬롯 배치 (0-26, 6x... 사용하지 않고 3행 9열 구조의 일부만 사용):
 *  0  1  2 |  3  4  5 |  6  7  8
 *  9 10 11 | 12 13 14 | 15 16 17
 * 18 19 20 | 21 22 23 | 24 25 26
 *
 * 실제 사용 슬롯: 1,2,3 / 10,11,12 / 19,20,21 (3x3 제작 그리드)
 * 결과 슬롯: 23
 * 이름 입력은 채팅으로, 저장 버튼: 25
 */
public class RecipeCreatorGUI implements Listener {

    private final LifeSteal plugin;
    private static final String GUI_TITLE = "§b§l커스텀 레시피 제작 - 9칸을 채우고 저장하세요";

    // 3x3 제작 그리드가 차지하는 인벤토리 슬롯 (왼쪽위→오른쪽아래 순서)
    private static final int[] CRAFT_SLOTS = {1, 2, 3, 10, 11, 12, 19, 20, 21};
    private static final int RESULT_SLOT = 23;
    private static final int SAVE_BUTTON_SLOT = 25;
    private static final int NAME_DISPLAY_SLOT = 16;

    // 플레이어별로 입력 대기 중인 레시피 이름
    private final Map<UUID, String> pendingNameInput = new HashMap<>();
    // 플레이어별 임시 저장된 레시피 이름
    private final Map<UUID, String> recipeNames = new HashMap<>();

    public RecipeCreatorGUI(LifeSteal plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // 테두리 장식 (회색 유리판)
        ItemStack border = createGlass(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            boolean isCraftSlot = false;
            for (int slot : CRAFT_SLOTS) if (slot == i) isCraftSlot = true;
            if (i != RESULT_SLOT && i != SAVE_BUTTON_SLOT && i != NAME_DISPLAY_SLOT && !isCraftSlot) {
                gui.setItem(i, border);
            }
        }

        // 화살표 표시 (제작 그리드 → 결과)
        gui.setItem(22, createGlass(Material.LIME_STAINED_GLASS_PANE, "§a→ 결과물을 여기에 →"));

        // 이름 입력 안내 아이템
        gui.setItem(NAME_DISPLAY_SLOT, createNameTag(player));

        // 저장 버튼
        gui.setItem(SAVE_BUTTON_SLOT, createGlass(Material.EMERALD_BLOCK,
                "§a§l저장하기", "§7제작 그리드 + 결과물을", "§7확인하고 레시피로 등록합니다"));

        player.openInventory(gui);
    }

    private ItemStack createNameTag(Player player) {
        String currentName = recipeNames.getOrDefault(player.getUniqueId(), "§7(이름 미설정)");
        ItemStack tag = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = tag.getItemMeta();
        meta.setDisplayName("§e레시피 이름: " + currentName);
        meta.setLore(List.of(
                "§7클릭하여 이름 설정",
                "§7(채팅으로 입력)"
        ));
        tag.setItemMeta(meta);
        return tag;
    }

    private ItemStack createGlass(Material mat, String... text) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (text.length > 0) meta.setDisplayName(text[0]);
        if (text.length > 1) meta.setLore(Arrays.asList(text).subList(1, text.length));
        item.setItemMeta(meta);
        return item;
    }

    public static int[] getCraftSlots() { return CRAFT_SLOTS; }
    public static int getResultSlot() { return RESULT_SLOT; }
    public static int getSaveButtonSlot() { return SAVE_BUTTON_SLOT; }
    public static int getNameDisplaySlot() { return NAME_DISPLAY_SLOT; }
    public static String getGuiTitle() { return GUI_TITLE; }

    public void setPendingNameInput(Player player) {
        pendingNameInput.put(player.getUniqueId(), "waiting");
    }

    public boolean isPendingNameInput(Player player) {
        return pendingNameInput.containsKey(player.getUniqueId());
    }

    public void clearPendingNameInput(Player player) {
        pendingNameInput.remove(player.getUniqueId());
    }

    public void setRecipeName(Player player, String name) {
        recipeNames.put(player.getUniqueId(), name);
    }

    public String getRecipeName(Player player) {
        return recipeNames.get(player.getUniqueId());
    }

    public void clearRecipeName(Player player) {
        recipeNames.remove(player.getUniqueId());
    }

    // GUI 닫을 때 입력 대기 상태 정리 (단, 저장 버튼으로 닫힌 경우는 GUIListener에서 먼저 처리)
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        if (!(event.getPlayer() instanceof Player)) return;
        // 이름은 유지하되, 채팅 입력 대기만 남아있다면 그대로 둠 (채팅으로 입력 가능하게)
    }
}
