package com.lifesteal.commands;

import com.lifesteal.LifeSteal;
import com.lifesteal.managers.CombatManager;
import com.lifesteal.managers.HeartManager;
import com.lifesteal.utils.ItemUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WithdrawCommand implements CommandExecutor {

    private final LifeSteal plugin;
    private final HeartManager heartManager;
    private final CombatManager combatManager;

    public WithdrawCommand(LifeSteal plugin) {
        this.plugin = plugin;
        this.heartManager = plugin.getHeartManager();
        this.combatManager = plugin.getCombatManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.isLifeStealEnabled()) {
            sender.sendMessage("§c라이프스틸 시스템이 현재 비활성화되어 있습니다.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§c사용법: /withdraw <개수>");
            return true;
        }

        if (combatManager.isTagged(player)) {
            player.sendMessage("§c전투 중에는 하트를 withdraw할 수 없습니다! §7(" + combatManager.getRemainingSeconds(player) + "초 남음)");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c올바른 숫자를 입력하세요.");
            return true;
        }

        int maxPerUse = plugin.getConfig().getInt("withdraw.max-per-use", 3);

        if (amount < 1) {
            player.sendMessage("§c1개 이상 입력해주세요.");
            return true;
        }
        if (amount > maxPerUse) {
            player.sendMessage("§c한 번에 최대 §f" + maxPerUse + "개§c까지만 withdraw 가능합니다.");
            return true;
        }

        int currentHearts = heartManager.getHearts(player);
        int minHearts = 1;

        if (currentHearts - amount < minHearts) {
            player.sendMessage("§c하트가 부족합니다. §7(현재: " + currentHearts + ", 최소 " + minHearts + "하트 유지)");
            return true;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§c인벤토리가 가득 찼습니다!");
            return true;
        }

        for (int i = 0; i < amount; i++) {
            heartManager.removeHeart(player);
        }
        ItemStack heartItem = ItemUtils.createHeartItem(amount);
        player.getInventory().addItem(heartItem);
        player.sendMessage("§c❤ §f" + amount + "개의 하트를 아이템으로 변환했습니다. §7(남은 하트: " + heartManager.getHearts(player) + ")");
        return true;
    }
}
