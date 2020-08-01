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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;
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
            BlockCommandSender commandBlockSender = (BlockCommandSender) sender;
            Block commandBlock = commandBlockSender.getBlock();
            CommandBlock realCommandBlock = (CommandBlock) commandBlock.getState();
            if (args[0].equalsIgnoreCase("call")) {
                if (args.length >= 3) {
                    ElevatorCommandCustomArguments eleArgs = getArgumentsElevators(args, 2);
                    Integer floorNumber = api.isInteger(eleArgs.getOtherArgs().get(0)) ? Integer.parseInt(eleArgs.getOtherArgs().get(0)) : null;
                    if (floorNumber == null) {
                        commandBlockSender.sendMessage(Translate.chat("FloorNumber can't be null."));
                        return true;
                    }
                    Boolean isGoingUp = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1) != null ? api.isBoolean(eleArgs.getOtherArgs().get(1)) ? Boolean.parseBoolean(eleArgs.getOtherArgs().get(1)) : null : null;

                    ElevatorController elevatorController = eleArgs.getElevatorController();
                    if (elevatorController == null) {
                        commandBlockSender.sendMessage("elevatorController == null");
                        return true;
                    }

                    if (eleArgs.getElevatorObject() != null) {
                        ElevatorObject elevatorObject = eleArgs.getElevatorObject();
                        if (elevatorObject == null) {
                            commandBlockSender.sendMessage("elevatorObject == null");
                            return true;
                        }
                        elevatorObject.goToFloor(floorNumber, isGoingUp != null ? ElevatorStatus.upOrDown(isGoingUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.COMMAND_BLOCK);
                    } else {
                        elevatorController.callElevatorClosest(floorNumber, isGoingUp != null ? ElevatorStatus.upOrDown(isGoingUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.COMMAND_BLOCK);
                    }
                    return true;
                }
                return true;
            }
            return true;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("world16elevators.elevator")) {
            p.sendMessage(com.andrew121410.mc.world16utils.chat.Translate.chat("&bYou don't have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(Translate.chat("&6/elevator create &e<Controller> &9<Elevator>"));
            p.sendMessage(Translate.chat("&6/elevator delete &e<Controller> &9<Elevator>"));
            p.sendMessage(Translate.chat("&6/elevator stop &e<Controller> &9<Elevator>"));
            p.sendMessage(Translate.chat("&6/elevator click &e<Controller> &9<Elevator>"));
            p.sendMessage(Translate.chat("&6/elevator rename &e<Controller> &9<Elevator> &a<TOElevatorName>"));
            p.sendMessage("");
            p.sendMessage(Translate.chat("&a&lMORE HELP COMMANDS..."));
            p.sendMessage("");
            p.sendMessage(Translate.chat("&6/elevator controller &d<Shows help for creation of a controller.>"));
            p.sendMessage(Translate.chat("&6/elevator floor &d<Shows help for the floor."));
            p.sendMessage(Translate.chat("&6/elevator shaft &d<Shows help for the shaft.>"));
            p.sendMessage(Translate.chat("&6/elevator call &d<Shows help to call the elevator."));
            return true;
            //Create controller
        } else if (args[0].equalsIgnoreCase("controller")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&6/elevator controller create &e<Controller>"));
                p.sendMessage(Translate.chat("&6/elevator controller delete &e<Controller>"));
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
            //Create elevator
        } else if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&6/elevator create &e<Controller> &9<ElevatorName>"));
                return true;
            } else if (args.length == 3) {
                String controllerName = args[1].toLowerCase();
                String elevatorName = args[2].toLowerCase();
                Region region = getSelection(p);

                if (region == null) {
                    p.sendMessage(Translate.chat("&cYou didn't make a WorldEdit selection... [FAILED]"));
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
                BoundingBox boundingBox = BoundingBox.of(one, two);
                boundingBox.expand(1);
                ElevatorObject elevatorObject = new ElevatorObject(this.plugin, elevatorName, p.getWorld().getName(), elevatorMovement, boundingBox);

                elevatorController.registerElevator(elevatorName, elevatorObject);
                p.sendMessage(Translate.chat("The elevator: " + elevatorName + " has been registered to " + controllerName));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("floor")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&a&l&o[Elevator Floor Help]"));
                p.sendMessage(Translate.chat("&6/elevator floor create &e<Controller> &9<Elevator> &a<FloorNumber>"));
                p.sendMessage(Translate.chat("&6/elevator floor delete &e<Controller> &9<Elevator> &a<FloorNumber>"));
                p.sendMessage(Translate.chat("&6/elevator floor sign &e<Controller> &9<Elevator> &a<FloorNumber>"));
                p.sendMessage(Translate.chat("&6/elevator floor door &e<Controller> &9<Elevator> &b<ADD OR DELETE> &3<FloorNumber>"));
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
        } else if (args[0].equalsIgnoreCase("call")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&a&l&o[Elevator Call Help]"));
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &9<ElevatorName> &b<FloorNumber>"));
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &a<FloorNumber>"));
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &a<FloorNumber> &b<Goup?>"));
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

                ElevatorStatus elevatorStatus = ElevatorStatus.upOrDown(booleanOrDefault);
                elevatorController.callElevatorClosest(floorNum, elevatorStatus, ElevatorWho.PLAYER_COMMAND);
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
                p.sendMessage(Translate.chat("&a&l&o[Elevator Shaft Help]"));
                p.sendMessage(Translate.chat("&6/elevator shaft &e<Controller> &9<Elevator> &bticksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator shaft &e<Controller> &9<Elevator> &bdoorHolderTicksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator shaft &e<Controller> &9<Elevator> &belevatorWaiterTicksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator shaft &e<Controller> &9<Elevator> &bdoElevatorLeveling &3<Value>"));
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
                    return true;
                } else if (args[3].equalsIgnoreCase("doorHolderTicksPerSecond")) {
                    long value1 = api.asLongOrDefault(value, ElevatorMovement.DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND);
                    elevatorObject.getElevatorMovement().setDoorHolderTicksPerSecond(value1);
                    p.sendMessage(Translate.chat("The door holder ticks per second has been updated to: " + value1));
                    return true;
                } else if (args[3].equalsIgnoreCase("elevatorWaiterTicksPerSecond")) {
                    long value1 = api.asLongOrDefault(value, ElevatorMovement.DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND);
                    elevatorObject.getElevatorMovement().setElevatorWaiterTicksPerSecond(value1);
                    p.sendMessage(Translate.chat("The elevator waiter ticks per second has been updated to: " + value1));
                    return true;
                } else if (args[3].equalsIgnoreCase("doElevatorLeveling")) {
                    boolean bool = api.asBooleanOrDefault(args[4], true);
                    elevatorObject.getElevatorMovement().setDoElevatorLeveling(bool);
                    p.sendMessage(Translate.chat("The doLevelingSystem has been set to: " + bool));
                    return true;
                } else if (args[3].equalsIgnoreCase("onlyTwoFloors")) {
                    boolean bool = api.asBooleanOrDefault(args[4], false);
                    elevatorObject.getElevatorMovement().setOnlyTwoFloors(bool);
                    p.sendMessage(Translate.chat("onlyTwoFloors has been set to: " + bool));
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("queue")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&6/elevator queue &e<Controller> floorQueueBuffer list/clear"));
                p.sendMessage(Translate.chat("&6/elevator queue &e<Controller> &9<Elevator> floorQueueBuffer list/clear"));
                return true;
            } else {
                ElevatorCommandCustomArguments eleArgs = getArgumentsElevators(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }
                ElevatorObject elevatorObject = eleArgs.getElevatorObject();
                String whatToRemove = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 0);
                if (whatToRemove == null) {
                    p.sendMessage("whatToBeRemoved cannot be null.");
                    return true;
                }
                String setting = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1);
                if (setting == null) {
                    p.sendMessage(Translate.chat("setting cannot be null."));
                    return true;
                }
                if (whatToRemove.equalsIgnoreCase("floorQueueBuffer")) {
                    if (setting.equalsIgnoreCase("list")) {
                        ComponentBuilder mainComponentBuilder = new ComponentBuilder();
                        TextComponent mainText = new TextComponent("Elevator queue system.");
                        mainText.setColor(ChatColor.GOLD);
                        mainText.setBold(true);
                        mainComponentBuilder.append(mainText).append("\n");
                        if (elevatorObject == null) {
                            elevatorController.getElevatorsMap().forEach((eleName, eleObject) -> mainComponentBuilder.append(makeQueueChatComponent(eleObject).create()));
                        } else
                            mainComponentBuilder.append(makeQueueChatComponent(elevatorObject).create());
                        p.spigot().sendMessage(mainComponentBuilder.create());
                        return true;
                    } else if (setting.equalsIgnoreCase("clear")) {
                        if (elevatorObject == null) {
                            elevatorController.getElevatorsMap().forEach((eleName, eleObject) -> eleObject.getFloorQueueBuffer().clear());
                            p.sendMessage(Translate.chat("FloorQueueBuffer has been cleared on all elevators on the controller."));
                        } else {
                            elevatorObject.getFloorQueueBuffer().clear();
                            p.sendMessage(Translate.chat("FloorQueueBuffer has been cleared for " + elevatorObject.getElevatorName() + " elevator."));
                        }
                        return true;
                    }
                    return true;
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("test")) {
            ElevatorCommandCustomArguments elevatorCommandCustomArguments = getArgumentsElevators(args, 2);
            if (elevatorCommandCustomArguments.getElevatorController() != null) {
                p.sendMessage(Translate.chat("ElevatorController: " + elevatorCommandCustomArguments.getElevatorController().getControllerName()));
            } else p.sendMessage(Translate.chat("ElevatorController: null"));
            if (elevatorCommandCustomArguments.getElevatorObject() != null) {
                p.sendMessage(Translate.chat("ElevatorObject: " + elevatorCommandCustomArguments.getElevatorObject().getElevatorName()));
            } else p.sendMessage(Translate.chat("ElevatorObject: null"));
            if (elevatorCommandCustomArguments.getOtherArgs() != null) {
                p.sendMessage(Translate.chat("OtherArgs: " + elevatorCommandCustomArguments.getOtherArgs()));
            } else p.sendMessage(Translate.chat("OtherArgs: null"));
        }
        return true;
    }

    private ElevatorCommandCustomArguments getArgumentsElevators(String[] args, int start) {
        ElevatorCommandCustomArguments elevatorCommandCustomArguments = new ElevatorCommandCustomArguments();
        String[] newStringArray = Arrays.copyOfRange(args, start - 1, args.length);
        ArrayList<String> otherArgs = new ArrayList<>();
        for (int i = 0; i < newStringArray.length; i++) {
            if (i == 0) {
                elevatorCommandCustomArguments.setElevatorController(this.elevatorControllerMap.get(newStringArray[0]));
            } else if (i == 1 && elevatorCommandCustomArguments.getElevatorController() != null && elevatorCommandCustomArguments.getElevatorController().getElevatorsMap().containsKey((newStringArray[1]))) {
                elevatorCommandCustomArguments.setElevatorObject(elevatorCommandCustomArguments.getElevatorController().getElevatorsMap().get(newStringArray[1]));
            } else otherArgs.add(newStringArray[i]);
        }
        elevatorCommandCustomArguments.setOtherArgs(otherArgs);
        return elevatorCommandCustomArguments;
    }

    private ComponentBuilder makeQueueChatComponent(ElevatorObject eleObject) {
        ComponentBuilder floorQueueObjectStringBuilder = new ComponentBuilder();
        floorQueueObjectStringBuilder.color(ChatColor.BLUE).bold(false);
        floorQueueObjectStringBuilder.append("Elevator: " + eleObject.getElevatorName()).append("\n").color(ChatColor.YELLOW).bold(false);
        for (FloorQueueObject floorQueueObject : eleObject.getFloorQueueBuffer()) {
            ComponentBuilder removeFloorFromFloorQueueBuffer = new ComponentBuilder()
                    .append("Floor: " + floorQueueObject.getFloorNumber())
                    .append(" ")
                    .append("Status: " + floorQueueObject.getElevatorStatus().name())
                    .append("\n");
            floorQueueObjectStringBuilder.append(removeFloorFromFloorQueueBuffer.create());
        }
        return floorQueueObjectStringBuilder;
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

@Getter
@Setter
@NoArgsConstructor
class ElevatorCommandCustomArguments {
    private ElevatorController elevatorController = null;
    private ElevatorObject elevatorObject = null;
    private ArrayList<String> otherArgs = null;
}