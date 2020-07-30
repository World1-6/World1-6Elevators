package com.andrew121410.mc.world16elevators.tabcomplete;

import com.andrew121410.mc.world16elevators.Main;
import com.andrew121410.mc.world16elevators.objects.ElevatorController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ElevatorTab implements TabCompleter {

    private List<String> tabCompleteList;
    private Map<String, ElevatorController> elevatorControllerMap;

    private Main plugin;

    public ElevatorTab(Main plugin) {
        this.plugin = plugin;
        this.tabCompleteList = new ArrayList<>();
        tabCompleteList.add("controller");
        tabCompleteList.add("create");
        tabCompleteList.add("floor");
        tabCompleteList.add("delete");
        tabCompleteList.add("call");
        tabCompleteList.add("stop");
        tabCompleteList.add("queue");
        tabCompleteList.add("click");
        tabCompleteList.add("rename");
        tabCompleteList.add("tostring");
        tabCompleteList.add("shaft");
        this.elevatorControllerMap = this.plugin.getSetListMap().getElevatorControllerMap();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String ailes, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player p = (Player) sender;

        if (!cmd.getName().equalsIgnoreCase("elevator") || !p.hasPermission("world16.elevator")) {
            return null;
        }

        List<String> controllerList = new ArrayList<>(this.elevatorControllerMap.keySet());

        if (args.length == 1) {
            return getContainsString(args[0], tabCompleteList);
        } else if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            }
            return null;
        } else if (args[0].equalsIgnoreCase("controller")) {
            if (args.length == 2) {
                return getContainsString(args[1], Arrays.asList("create", "delete"));
            } else if (args[1].equalsIgnoreCase("delete")) {
                return getContainsString(args[2], controllerList);
            }
            return null;
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            }
            return null;
        } else if (args[0].equalsIgnoreCase("rename")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            }
            return null;
        } else if (args[0].equalsIgnoreCase("floor")) {
            if (args.length == 2) {
                return getContainsString(args[1], Arrays.asList("create", "door", "sign", "delete"));
            } else if (args.length == 3) {
                return getContainsString(args[2], controllerList);
            }
            return null;
        } else if (args[0].equalsIgnoreCase("call")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            }
            return null;
        } else if (args[0].equalsIgnoreCase("shaft")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 4) {
                return getContainsString(args[3], Arrays.asList("ticksPerSecond", "doorHolderTicksPerSecond", "elevatorWaiterTicksPerSecond"));
            }
            return null;
        } else if (args[0].equalsIgnoreCase("stop")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            }
            return null;
        } else if (args[0].equalsIgnoreCase("tostring")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            }
            return null;
        }
        return null;
    }

    public static List<String> getContainsString(String args, List<String> oldArrayList) {
        List<String> list = new ArrayList<>();

        for (String mat : oldArrayList) {
            if (mat.contains(args.toLowerCase())) {
                list.add(mat);
            }
        }

        return list;
    }
}