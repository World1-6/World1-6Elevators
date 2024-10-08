package com.andrew121410.mc.world16elevators.commands.tabcomplete;

import com.andrew121410.mc.world16elevators.Elevator;
import com.andrew121410.mc.world16elevators.ElevatorController;
import com.andrew121410.mc.world16elevators.ElevatorFloor;
import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16elevators.enums.ElevatorCallButtonType;
import com.andrew121410.mc.world16elevators.enums.ElevatorFloorSelectorType;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElevatorTab implements TabCompleter {

    private final List<String> tabCompleteList;
    private final Map<String, ElevatorController> elevatorControllerMap;

    private final List<String> soundList;

    private final World16Elevators plugin;

    public ElevatorTab(World16Elevators plugin) {
        this.plugin = plugin;
        this.tabCompleteList = new ArrayList<>();
        tabCompleteList.add("controller");
        tabCompleteList.add("create");
        tabCompleteList.add("floor");
        tabCompleteList.add("delete");
        tabCompleteList.add("call");
        tabCompleteList.add("stop");
        tabCompleteList.add("rename");
        tabCompleteList.add("settings");
        tabCompleteList.add("queue");
        tabCompleteList.add("opendoor");
        tabCompleteList.add("copysettingsfrom");
        tabCompleteList.add("tostring");
        tabCompleteList.add("teleport");
        tabCompleteList.add("realign");
        tabCompleteList.add("boundingbox");
        tabCompleteList.add("volume");
        this.elevatorControllerMap = this.plugin.getMemoryHolder().getElevatorControllerMap();
        this.soundList = new ArrayList<>();
        for (Sound value : Sound.values()) {
            this.soundList.add(value.name());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String ailes, String[] args) {
        if (!(sender instanceof Player player)) {
            return null;
        }

        if (!cmd.getName().equalsIgnoreCase("elevator") || !player.hasPermission("world16.elevator")) {
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
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            }
            return null;
        } else if (args[0].equalsIgnoreCase("rename")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            }
            return null;
        } else if (args[0].equalsIgnoreCase("floor")) {
            if (args.length == 2) {
                return getContainsString(args[1], Arrays.asList("create", "delete", "setName", "sign", "door", "permission", "smartCreateFloors"));
            } else if (args.length == 3) {
                return getContainsString(args[2], controllerList);
            } else if (args.length == 4) {
                return this.elevatorControllerMap.containsKey(args[2]) ? new ArrayList<>(this.elevatorControllerMap.get(args[2]).getElevatorsMap().keySet()) : null;
            } else if (args.length == 5 && !args[1].equalsIgnoreCase("create")) {
                ElevatorController elevatorController = this.elevatorControllerMap.get(args[2]);
                if (elevatorController == null) return null;
                Elevator elevator = elevatorController.getElevatorsMap().get(args[3]);
                if (elevator == null) return null;
                return elevator.getFloorsMap().values().stream().map(ElevatorFloor::getName).collect(Collectors.toList());
            } else if (args.length == 6 && args[1].equalsIgnoreCase("smartCreateFloors")) {
                return getContainsString(args[5], Arrays.asList("true", "false"));
            }
            return null;
        } else if (args[0].equalsIgnoreCase("call")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            } else if (args.length == 4) {
                ElevatorController elevatorController = this.elevatorControllerMap.get(args[1]);
                if (elevatorController == null) return null;
                Elevator elevator = elevatorController.getElevatorsMap().get(args[2]);
                if (elevator == null) return null;
                return elevator.getFloorsMap().values().stream().map(ElevatorFloor::getName).collect(Collectors.toList());
            }
            return null;
        } else if (args[0].equalsIgnoreCase("settings")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            } else if (args.length == 4) {
                return getContainsString(args[3], Arrays.asList("ticksPerSecond", "doorHolderTicksPerSecond", "elevatorWaiterTicksPerSecond", "doElevatorLeveling", "onlyTwoFloors", "arrivalSound", "passingByFloorSound", "floorSelectorType", "callButtonType", "signFinderSystem", "teleportElevatorOnEmpty"));
            } else if (args.length == 5) {
                List<String> shouldShowBool = new ArrayList<>();
                shouldShowBool.add("doElevatorLeveling");
                shouldShowBool.add("onlyTwoFloors");
                shouldShowBool.add("signFinderSystem");
                shouldShowBool.add("teleportElevatorOnEmpty");

                if (shouldShowBool.contains(args[3])) {
                    return StringUtil.copyPartialMatches(args[4], Arrays.asList("true", "false"), new ArrayList<>());
                } else if (args[3].equals("arrivalSound") || args[3].equals("passingByFloorSound")) {
                    return StringUtil.copyPartialMatches(args[4], this.soundList, new ArrayList<>());
                } else if (args[3].equalsIgnoreCase("floorSelectorType")) {
                    return StringUtil.copyPartialMatches(args[4], Arrays.stream(ElevatorFloorSelectorType.values()).map(Enum::name).collect(Collectors.toList()), new ArrayList<>());
                } else if (args[3].equalsIgnoreCase("callButtonType")) {
                    return StringUtil.copyPartialMatches(args[4], Arrays.stream(ElevatorCallButtonType.values()).map(Enum::name).collect(Collectors.toList()), new ArrayList<>());
                }
            }
            return null;
        } else if (args[0].equalsIgnoreCase("stop")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            }
            return null;
        } else if (args[0].equalsIgnoreCase("queue")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return getContainsString(args[2], Arrays.asList("floorQueueBuffer"));
            } else if (args.length == 4 && args[2].equalsIgnoreCase("floorQueueBuffer")) {
                return getContainsString(args[3], Arrays.asList("list", "clear"));
            }
            return null;
        } else if (args[0].equalsIgnoreCase("opendoor")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            }
            return null;
        } else if (args[0].equalsIgnoreCase("copysettingsfrom")) {
            if (args.length == 2 || args.length == 4) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3 || args.length == 5) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            }
            return null;
        } else if (args[0].equalsIgnoreCase("tostring")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            }
            return null;
        } else if (args[0].equalsIgnoreCase("teleport")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            }
            return null;
        } else if (args[0].equalsIgnoreCase("realign")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            }
            return null;
        } else if (args[0].equalsIgnoreCase("boundingbox")) {
            if (args.length == 2) {
                return getContainsString(args[1], controllerList);
            } else if (args.length == 3) {
                return this.elevatorControllerMap.containsKey(args[1]) ? new ArrayList<>(this.elevatorControllerMap.get(args[1]).getElevatorsMap().keySet()) : null;
            } else if (args.length == 4) {
                return getContainsString(args[3], Arrays.asList("show", "shift", "fix-with-worldedit", "atdoor"));
            }
            return null;
        }
        return null;
    }

    public static List<String> getContainsString(String args, List<String> oldArrayList) {
        return StringUtil.copyPartialMatches(args, oldArrayList, new ArrayList<>());
    }
}
