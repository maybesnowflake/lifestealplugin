package com.lifesteal.commands;

import com.lifesteal.LifeSteal;
import com.lifesteal.utils.ItemUtils;
import com.lifesteal.utils.RecipeUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LifeStealAdminCommand implements CommandExecutor {

    private final LifeSteal plugin;

    public LifeStealAdminCommand(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lifesteal.admin")) {
            sender.sendMessage("§c권한이 없습니다.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on":
                plugin.setLifeStealEnabled(true);
                sender.sendMessage("§a라이프스틸 시스템이 활성화되었습니다.");
                break;

            case "off":
                plugin.setLifeStealEnabled(false);
                sender.sendMessage("§c라이프스틸 시스템이 비활성화되었습니다.");
                break;

            case "reload":
                plugin.reloadConfig();
                RecipeUtil.reloadAllRecipes(plugin);
                sender.sendMessage("§a설정 및 레시피를 다시 불러왔습니다.");
                break;

            case "sethearts":
                if (args.length < 3) { sender.sendMessage("§c사용법: /lifesteal sethearts <플레이어> <하트>"); return true; }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) { sender.sendMessage("§c플레이어를 찾을 수 없습니다."); return true; }
                try {
                    int hearts = Integer.parseInt(args[2]);
                    plugin.getHeartManager().setHearts(target, hearts);
                    sender.sendMessage("§a" + target.getName() + " 님의 하트를 " + hearts + "로 설정했습니다.");
                    target.sendMessage("§a관리자가 하트를 " + hearts + "로 설정했습니다.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c올바른 숫자를 입력하세요.");
                }
                break;

            case "giveheartitem":
                if (args.length < 2) { sender.sendMessage("§c사용법: /lifesteal giveheartitem <플레이어> [개수]"); return true; }
                Player recv = plugin.getServer().getPlayer(args[1]);
                if (recv == null) { sender.sendMessage("§c플레이어를 찾을 수 없습니다."); return true; }
                int amt = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
                recv.getInventory().addItem(ItemUtils.createHeartItem(amt));
                sender.sendMessage("§a하트 아이템 " + amt + "개를 " + recv.getName() + " 에게 지급했습니다.");
                break;

            case "giverevivebook":
                if (args.length < 2) { sender.sendMessage("§c사용법: /lifesteal giverevivebook <플레이어>"); return true; }
                Player recv2 = plugin.getServer().getPlayer(args[1]);
                if (recv2 == null) { sender.sendMessage("§c플레이어를 찾을 수 없습니다."); return true; }
                recv2.getInventory().addItem(ItemUtils.createReviveBook(1));
                sender.sendMessage("§a생환서를 " + recv2.getName() + " 에게 지급했습니다.");
                break;

            case "addrecipe":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§c이 명령어는 플레이어만 사용할 수 있습니다.");
                    return true;
                }
                plugin.getRecipeCreatorGUI().open((Player) sender);
                break;

            default:
                sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== LifeSteal 관리자 명령어 ===");
        sender.sendMessage("§e/lifesteal on §7- 라이프스틸 시스템 켜기");
        sender.sendMessage("§e/lifesteal off §7- 라이프스틸 시스템 끄기");
        sender.sendMessage("§e/lifesteal reload §7- 설정/레시피 리로드");
        sender.sendMessage("§e/lifesteal sethearts <플레이어> <하트> §7- 하트 설정");
        sender.sendMessage("§e/lifesteal giveheartitem <플레이어> [개수] §7- 하트 아이템 지급");
        sender.sendMessage("§e/lifesteal giverevivebook <플레이어> §7- 생환서 지급");
        sender.sendMessage("§e/lifesteal addrecipe §7- 작업대 모양 GUI로 커스텀 레시피 추가");
        sender.sendMessage("§e/revive <플레이어> §7- 밴된 플레이어 부활");
    }
}
