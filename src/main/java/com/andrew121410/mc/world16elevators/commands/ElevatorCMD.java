package com.andrew121410.mc.world16elevators.commands;

import com.andrew121410.mc.world16elevators.Main;
import com.andrew121410.mc.world16elevators.manager.ElevatorManager;
import com.andrew121410.mc.world16elevators.objects.*;
import com.andrew121410.mc.world16elevators.tabcomplete.ElevatorTab;
import com.andrew121410.mc.world16elevators.utils.API;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.math.SimpleMath;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Map;

public class ElevatorCMD implements CommandExecutor {

    private Main plugin;
    private API api;

    private WorldEditPlugin worldEditPlugin;
    private ElevatorManager elevatorManager;

    private Map<String, ElevatorController> elevatorControllerMap;

    private SimpleMath simpleMath;

    public ElevatorCMD(Main plugin) {
        this.plugin = plugin;

        this.api = new API(this.plugin);
        this.simpleMath = new SimpleMath(this.plugin);

        this.worldEditPlugin = this.plugin.getOtherPlugins().getWorldEditPlugin();
        this.elevatorControllerMap = this.plugin.getSetListMap().getElevatorControllerMap();
        this.elevatorManager = this.plugin.getElevatorManager();

        this.plugin.getCommand("elevator").setExecutor(this);
        this.plugin.getCommand("elevator").setTabCompleter(new ElevatorTab(this.plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {

            if (!(sender instanceof BlockCommandSender)) {
                return true;
            }

            BlockCommandSender cmdblock = (BlockCommandSender) sender;
            Block commandblock = cmdblock.getBlock();

            if (args[0].equalsIgnoreCase("call")) {
                if (args.length == 4) {
                    String controllerName = args[1].toLowerCase();
                    String elevatorName = args[2].toLowerCase();
                    int floorNum = api.asIntOrDefault(args[3], 0);

                    ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                    if (elevatorController == null) return true;

                    ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                    if (elevatorObject == null) return true;

                    if (elevatorObject.getFloor(floorNum) == null) return true;

                    elevatorObject.goToFloor(floorNum, ElevatorStatus.DONT_KNOW, ElevatorWho.COMMAND_BLOCK);
                    return true;
                } else if (args.length == 5 && api.isBoolean(args[4])) {
                    String controllerName = args[1].toLowerCase();
                    String elevatorName = args[2].toLowerCase();
                    int floorNum = api.asIntOrDefault(args[3], 0);
                    boolean goUp = api.asBooleanOrDefault(args[4], false);

                    ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                    if (elevatorController == null) return true;

                    ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                    if (elevatorObject == null) return true;

                    if (elevatorObject.getFloor(floorNum) == null) return true;

                    ElevatorStatus elevatorStatus = ElevatorStatus.DONT_KNOW;
                    elevatorObject.goToFloor(floorNum, elevatorStatus.upOrDown(goUp), ElevatorWho.COMMAND_BLOCK);
                    return true;
                } else if (args.length == 5 && !api.isBoolean(args[3])) {
                    String controllerName = args[1].toLowerCase();
                    String elevatorName = args[2].toLowerCase();
                    int floorNum = api.asIntOrDefault(args[3], 0);
                    int toFloorNum = api.asIntOrDefault(args[4], 0);

                    ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                    if (elevatorController == null) return true;

                    ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                    if (elevatorObject == null) return true;

                    if (elevatorObject.getFloor(floorNum) == null || elevatorObject.getFloor(toFloorNum) == null)
                        return true;

                    elevatorObject.callElevator(floorNum, toFloorNum, ElevatorWho.COMMAND_BLOCK);
                    return true;
                }
                return true;
            }
            return true;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("world16.elevator")) {
            p.sendMessage(com.andrew121410.mc.world16utils.chat.Translate.chat("&bYou don't have permission to use this comamnd."));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(Translate.chat("Elevator Help"));
            p.sendMessage(Translate.chat("/elevator controller <Shows help for creation of a controller.>"));
            p.sendMessage(Translate.chat("/elevator create <Shows help for creation of a elevator.>"));
            p.sendMessage(Translate.chat("/elevator delete <Controller> <ElevatorName>"));
            p.sendMessage(Translate.chat("/elevator floor <Shows help for the floor."));
            p.sendMessage(Translate.chat("/elevator call <Shows help to call the elevator."));
            p.sendMessage(Translate.chat("/elevator rename <Controller> <ElevatorName> <TOElevatorName>"));
            p.sendMessage(Translate.chat("/elevator shaft <Shows help for the shaft.>"));
            return true;
            //CREATE
        } else if (args[0].equalsIgnoreCase("controller")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("/elevator controller create <NAME>"));
                p.sendMessage(Translate.chat("/elevator controller delete <NAME>"));
                return true;
            } else if (args.length == 3 && args[1].equalsIgnoreCase("create")) {
                String controllerName = args[2].toLowerCase();

                if (elevatorControllerMap.get(controllerName) != null) {
                    p.sendMessage(Translate.chat("Looks like that's already a controller name."));
                    return true;
                }

                this.elevatorControllerMap.putIfAbsent(controllerName, new ElevatorController(plugin, controllerName));
                p.sendMessage(Translate.chat("ElevatorController has been registered with the name of " + controllerName));
                return true;
            } else if (args.length == 3 && args[1].equalsIgnoreCase("delete")) {
                String controllerName = args[2].toLowerCase();

                if (elevatorControllerMap.get(controllerName) == null) {
                    p.sendMessage(Translate.chat("Looks like that's not a valid controller."));
                    return true;
                }

                this.elevatorManager.deleteElevatorController(controllerName);
                p.sendMessage(Translate.chat("Controller has been deleted."));
                return true;
            }
            return true;
        } else if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("[Elevator creation]"));
                p.sendMessage(Translate.chat("/elevator create <Controller> <ElevatorName> <XAX> <XAY> <XAZ> <XBX> <XBY> <XAZ>"));
                return true;
            } else if (args.length == 9) {
                String controllerName = args[1].toLowerCase();
                String elevatorName = args[2].toLowerCase();

                int XAX = api.asIntOrDefault(args[3], 0);
                int XAY = api.asIntOrDefault(args[4], 0);
                int XAZ = api.asIntOrDefault(args[5], 0);

                int XBX = api.asIntOrDefault(args[6], 0);
                int XBY = api.asIntOrDefault(args[7], 0);
                int XBZ = api.asIntOrDefault(args[8], 0);

                Region region = getSelection(p);

                if (region == null) {
                    p.sendMessage("Please make a selection with WorldEdit and then redo the command please.");
                    return true;
                }

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                Location one = new Location(p.getWorld(), region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ());
                Location two = new Location(p.getWorld(), region.getMaximumPoint().getX(), region.getMaximumPoint().getY(), region.getMaximumPoint().getZ());

                ElevatorMovement elevatorMovement = new ElevatorMovement(0, this.api.getBlockPlayerIsLookingAt(p).getLocation(), one, two);
                BoundingBox boundingBox = SimpleMath.toBoundingBox(new Vector(XAX, XAY, XAZ), new Vector(XBX, XBY, XBZ));
                ElevatorObject elevatorObject = new ElevatorObject(false, plugin, p.getWorld().getName(), elevatorName, elevatorMovement, boundingBox);

                elevatorController.registerElevator(elevatorName, elevatorObject);
                p.sendMessage(Translate.chat("The elevator: " + elevatorName + " has been registered to " + controllerName));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("floor")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("[Elevator Floor Setup]"));
                p.sendMessage(Translate.chat("/elevator floor create <Controller> <ElevatorName> <FloorNumber>"));
                p.sendMessage(Translate.chat("/elevator floor delete <Controller> <ElevatorName> <FloorNumber>"));
                p.sendMessage(Translate.chat("/elevator floor sign <Controller> <ElevatorName> <FloorNumber>"));
                p.sendMessage(Translate.chat("/elevator floor door <Controller> <ElevatorName> <ADD OR DELETE> <Floor>"));
                return true;
            } else if (args.length == 5 && args[1].equalsIgnoreCase("create")) {
                String controllerName = args[2].toLowerCase();
                String elevatorName = args[3].toLowerCase();
                int floorNum = api.asIntOrDefault(args[4], 0);

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                if (elevatorObject == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }

                elevatorObject.addFloor(new FloorObject(floorNum, api.getBlockPlayerIsLookingAt(p).getLocation()));
                p.sendMessage(Translate.chat("[Create] Floor: " + floorNum + " has been added to the elevator: " + elevatorName));
                return true;
            } else if (args.length == 5 && args[1].equalsIgnoreCase("delete")) {
                String controllerName = args[2].toLowerCase();
                String elevatorName = args[3].toLowerCase();
                int floorNum = api.asIntOrDefault(args[4], 0);

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                if (elevatorObject == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }

                if (elevatorObject.getFloorsMap().get(floorNum) == null) {
                    p.sendMessage(Translate.chat("This floor doesn't exist."));
                    return true;
                }

                this.elevatorManager.deleteFloorOfElevator(controllerName, elevatorName, floorNum);
                p.sendMessage(Translate.chat("The floor: " + floorNum + " has been removed from the elevator: " + elevatorName));
                return true;
            } else if (args.length == 5 && args[1].equalsIgnoreCase("sign")) {
                String controllerName = args[2].toLowerCase();
                String elevatorName = args[3].toLowerCase();
                int floorNum = api.asIntOrDefault(args[4], 0);

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                if (elevatorObject == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }

                FloorObject floorObject = elevatorObject.getFloorsMap().get(floorNum);
                if (floorObject == null) {
                    p.sendMessage(Translate.chat("This floor doesn't exist."));
                    return true;
                }

                floorObject.getSignList().add(new SignObject(this.api.getBlockPlayerIsLookingAt(p).getLocation()));
                p.sendMessage(Translate.chat("Sign has been set"));
                return true;
            } else if (args.length == 6 && args[1].equalsIgnoreCase("door")) {
                Location location = api.getBlockPlayerIsLookingAt(p).getLocation();

                String controllerName = args[2].toLowerCase();
                String elevatorName = args[3].toLowerCase();
                String addOrRemove = args[4];
                int floorNum = api.asIntOrDefault(args[5], 0);

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                if (elevatorObject == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }

                FloorObject floorObject = elevatorObject.getFloorsMap().get(floorNum);
                if (floorObject == null) {
                    p.sendMessage(Translate.chat("This floor doesn't exist."));
                    return true;
                }

                if (addOrRemove.equalsIgnoreCase("add")) {
                    floorObject.getDoorList().add(location);
                    p.sendMessage(Translate.chat("The door for the floor: " + floorNum + " has been added to the elevator: " + elevatorName));
                } else if (addOrRemove.equalsIgnoreCase("remove") || addOrRemove.equalsIgnoreCase("delete")) {
                    floorObject.getDoorList().remove(location);
                    p.sendMessage(Translate.chat("The door for the floor: " + floorNum + " has been deleted for the elvator: " + elevatorName));
                }
                return true;
            }
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("delete")) {
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2].toLowerCase();

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                p.sendMessage("Elevator controller was not found.");
                return true;
            }

            if (elevatorController.getElevatorsMap().get(elevatorName) == null) {
                p.sendMessage(Translate.chat("That elevator doesn't exist."));
                return true;
            }

            this.plugin.getElevatorManager().deleteElevator(controllerName, elevatorName);
            p.sendMessage(Translate.chat("Elevator: " + elevatorName + " has been deleted from controller: " + controllerName));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
            this.plugin.getElevatorManager().saveAllElevators();
            p.sendMessage(Translate.chat("All elevators have been saved."));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("load")) {
            this.plugin.getElevatorManager().loadAllElevators();
            p.sendMessage(Translate.chat("All elevators have been loaded in memory."));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            elevatorControllerMap.clear();
            p.sendMessage(Translate.chat("All elevators have been cleared in memory."));
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("toString")) {
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2].toLowerCase();

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                p.sendMessage("Elevator controller was not found.");
                return true;
            }

            if (elevatorController.getElevatorsMap().get(elevatorName) == null) {
                p.sendMessage(Translate.chat("That elevator doesn't exist."));
                return true;
            }

            this.plugin.getServer().getConsoleSender().sendMessage(elevatorController.getElevatorsMap().get(elevatorName).toString());
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("stop")) {
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2].toLowerCase();

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                p.sendMessage("Elevator controller was not found.");
                return true;
            }

            ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
            if (elevatorObject == null) {
                p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                return true;
            }

            elevatorObject.emergencyStop();
            p.sendMessage(Translate.chat("emergency stop has been activated."));
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("click")) {
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2].toLowerCase();

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                p.sendMessage("Elevator controller was not found.");
                return true;
            }

            ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
            if (elevatorObject == null) {
                p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                return true;
            }

            elevatorObject.clickMessageGoto(p);
            return true;
        } else if (args[0].equalsIgnoreCase("call")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("/elevator call <Controller> <ElevatorName> <FloorNumber>"));
                p.sendMessage(Translate.chat("/elevator call <Controller> <FloorNumber>"));
                p.sendMessage(Translate.chat("/elevator call <Controller> <FloorNumber> <Goup?>"));
            } else if (args.length == 4 && !api.isBoolean(args[3])) {
                String controllerName = args[1].toLowerCase();
                String elevatorName = args[2].toLowerCase();
                int floorNum = api.asIntOrDefault(args[3], 0);

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                if (elevatorObject == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }

                elevatorObject.goToFloor(floorNum, ElevatorStatus.DONT_KNOW, ElevatorWho.PLAYER_COMMAND);
                p.sendMessage(Translate.chat("Going to floor: " + floorNum + " for the Elevator: " + elevatorName));
                return true;
            } else if (args.length == 3) {
                String controllerName = args[1].toLowerCase();
                int floorNum = api.asIntOrDefault(args[2], 0);

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                elevatorController.callElevatorClosest(floorNum, ElevatorStatus.DONT_KNOW, ElevatorWho.PLAYER_COMMAND);
                p.sendMessage(Translate.chat("Going to floor: " + floorNum + " on controller " + controllerName));
                return true;
            } else if (args.length == 4 && api.isBoolean(args[3])) {
                String controllerName = args[1].toLowerCase();
                int floorNum = api.asIntOrDefault(args[2], 0);
                boolean booleanOrDefault = api.asBooleanOrDefault(args[3], false);

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                ElevatorStatus elevatorStatus = ElevatorStatus.DONT_KNOW;
                elevatorController.callElevatorClosest(floorNum, elevatorStatus.upOrDown(booleanOrDefault), ElevatorWho.PLAYER_COMMAND);
                p.sendMessage(Translate.chat("Called the nearest elevator on controller: " + controllerName + " to go to floor: " + floorNum + " and it when go; " + elevatorStatus.upOrDown(booleanOrDefault)));
                return true;
            }
            return true;
        } else if (args.length == 4 && args[0].equalsIgnoreCase("rename")) {
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2].toLowerCase();
            String toElevatorName = args[3].toLowerCase();

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                p.sendMessage("Elevator controller was not found.");
                return true;
            }

            ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
            if (elevatorObject == null) {
                p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                return true;
            }

            elevatorManager.deleteElevator(controllerName, elevatorName);
            elevatorObject.setElevatorName(toElevatorName);
            elevatorController.registerElevator(toElevatorName, elevatorObject);
            p.sendMessage(Translate.chat("Old Name: " + elevatorName + " new Name: " + toElevatorName));
            return true;
        } else if (args[0].equalsIgnoreCase("shaft")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("/elevator shaft <Controller> <ElevatorName> ticksPerSecond <Value>"));
                p.sendMessage(Translate.chat("/elevator shaft <Controller> <ElevatorName> doorHolderTicksPerSecond <Value>"));
                p.sendMessage(Translate.chat("/elevator shaft <Controller> <ElevatorName> elevatorWaiterTicksPerSecond <Value>"));
            } else if (args.length > 2) {
                String controllerName = args[1].toLowerCase();
                String elevatorName = args[2].toLowerCase();
                String value = args[4];

                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                ElevatorObject elevatorObject = elevatorController.getElevator(elevatorName);
                if (elevatorObject == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }

                if (args[3].equalsIgnoreCase("ticksPerSecond")) {
                    long value1 = api.asLongOrDefault(value, ElevatorMovement.DEFAULT_TICKS_PER_SECOND);
                    elevatorObject.getElevatorMovement().setTicksPerSecond(value1);
                    p.sendMessage(Translate.chat("The ticks per second has been updated to: " + value1));
                } else if (args[3].equalsIgnoreCase("doorHolderTicksPerSecond")) {
                    long value1 = api.asLongOrDefault(value, ElevatorMovement.DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND);
                    elevatorObject.getElevatorMovement().setDoorHolderTicksPerSecond(value1);
                    p.sendMessage(Translate.chat("The door holder ticks per second has been updated to: " + value1));
                } else if (args[3].equalsIgnoreCase("elevatorWaiterTicksPerSecond")) {
                    long value1 = api.asLongOrDefault(value, ElevatorMovement.DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND);
                    elevatorObject.getElevatorMovement().setElevatorWaiterTicksPerSecond(value1);
                    p.sendMessage(Translate.chat("The elevator waiter ticks per second has been updated to: " + value1));
                }
            }
        }
        return true;
    }

    private Region getSelection(Player player) {
        Region region;
        try {
            region = worldEditPlugin.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (Exception ex) {
            return null;
        }
        return region;
    }
}