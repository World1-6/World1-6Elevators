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
import org.bukkit.Sound;
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
                    String stringFloor = eleArgs.getOtherArgs().get(0);
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
                        if (elevatorObject.getFloor(stringFloor) == null) {
                            commandBlockSender.sendMessage("getFloor == null");
                            return true;
                        }
                        elevatorObject.goToFloor(stringFloor, isGoingUp != null ? ElevatorStatus.upOrDown(isGoingUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.COMMAND_BLOCK);
                    } else {
                        elevatorController.callElevatorClosest(stringFloor, isGoingUp != null ? ElevatorStatus.upOrDown(isGoingUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.COMMAND_BLOCK);
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
            p.sendMessage(Translate.chat("&6/elevator create &e<Controller> &9<Elevator> &a<FloorName>"));
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
                p.sendMessage(Translate.chat("&6/elevator create &e<Controller> &9<ElevatorName> &a<FloorName>"));
                return true;
            } else if (args.length == 4) {
                String controllerName = args[1].toLowerCase();
                String elevatorName = args[2].toLowerCase();
                String floorName = args[3];
                Block block = api.getBlockPlayerIsLookingAt(p);
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

                ElevatorMovement elevatorMovement = new ElevatorMovement(1, block.getLocation().clone(), one, two);
                BoundingBox boundingBox = BoundingBox.of(one, two);
                boundingBox.expand(1);
                ElevatorObject elevatorObject = new ElevatorObject(this.plugin, elevatorName, p.getWorld().getName(), elevatorMovement, boundingBox);
                FloorObject floorObject = new FloorObject(1, floorName, block.getLocation().clone());
                elevatorObject.addFloor(floorObject);

                elevatorController.registerElevator(elevatorName, elevatorObject);
                p.sendMessage(Translate.chat("The elevator: " + elevatorName + " has been registered to " + controllerName));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("floor")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&a&l&o[Elevator Floor Help]"));
                p.sendMessage(Translate.chat("&6/elevator floor create &e<Controller> &9<Elevator> &a<Floor>"));
                p.sendMessage(Translate.chat("&6/elevator floor delete &e<Controller> &9<Elevator> &a<Floor>"));
                p.sendMessage(Translate.chat("&6/elevator floor sign &e<Controller> &9<Elevator> &a<Floor>"));
                p.sendMessage(Translate.chat("&6/elevator floor door &e<Controller> &9<Elevator> &b<ADD OR DELETE> &3<Floor>"));
                return true;
            } else if (args.length == 5 && args[1].equalsIgnoreCase("create")) {
                String controllerName = args[2].toLowerCase();
                String elevatorName = args[3].toLowerCase();
                String floorName = args[4];

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

                elevatorObject.addFloor(new FloorObject(floorName, api.getBlockPlayerIsLookingAt(p).getLocation()));
                p.sendMessage(Translate.chat("[Create] Floor: " + floorName + " has been added to the elevator: " + elevatorName));
                return true;
            } else if (args.length == 5 && args[1].equalsIgnoreCase("delete")) {
                String controllerName = args[2].toLowerCase();
                String elevatorName = args[3].toLowerCase();
                String floorName = args[4];

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

                if (elevatorObject.getFloor(floorName) == null) {
                    p.sendMessage(Translate.chat("This floor doesn't exist."));
                    return true;
                }

                FloorObject floorObject = elevatorObject.getFloor(floorName);
                this.elevatorManager.deleteFloorOfElevator(controllerName, elevatorName, floorObject.getFloor());
                p.sendMessage(Translate.chat("The floor: " + floorName + " has been removed from the elevator: " + elevatorName));
                return true;
            } else if (args.length == 5 && args[1].equalsIgnoreCase("sign")) {
                String controllerName = args[2].toLowerCase();
                String elevatorName = args[3].toLowerCase();
                String floorName = args[4];

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

                FloorObject floorObject = elevatorObject.getFloor(floorName);
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
                String floorName = args[5];

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

                FloorObject floorObject = elevatorObject.getFloor(floorName);
                if (floorObject == null) {
                    p.sendMessage(Translate.chat("This floor doesn't exist."));
                    return true;
                }

                if (addOrRemove.equalsIgnoreCase("add")) {
                    floorObject.getDoorList().add(location);
                    p.sendMessage(Translate.chat("The door for the floor: " + floorObject.getFloor() + " has been added to the elevator: " + elevatorName));
                } else if (addOrRemove.equalsIgnoreCase("remove") || addOrRemove.equalsIgnoreCase("delete")) {
                    floorObject.getDoorList().remove(location);
                    p.sendMessage(Translate.chat("The door for the floor: " + floorObject.getFloor() + " has been deleted for the elevator: " + elevatorName));
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
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &9<ElevatorName> &b<Floor>"));
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &9<ElevatorName> &a<Floor> &b<Goup?>"));
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &a<Floor>"));
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &a<Floor> &b<Goup?>"));
                return true;
            } else {
                ElevatorCommandCustomArguments eleArgs = getArgumentsElevators(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                ElevatorObject elevatorObject = eleArgs.getElevatorObject();
                String floorName = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 0);
                Boolean goUp = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1) != null ? api.isBoolean(eleArgs.getOtherArgs().get(1)) ? Boolean.parseBoolean(eleArgs.getOtherArgs().get(1)) : null : null;
                if (elevatorController == null) {
                    p.sendMessage(Translate.chat("elevatorController cannot be null."));
                    return true;
                }
                if (floorName == null) {
                    p.sendMessage(Translate.chat("floorName cannot be null."));
                    return true;
                }

                if (elevatorObject != null) {
                    elevatorObject.goToFloor(floorName, goUp != null ? ElevatorStatus.upOrDown(goUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.PLAYER_COMMAND);
                    p.sendMessage(Translate.chat("Calling: " + elevatorObject.getElevatorName() + " to go to floor: " + floorName));
                    return true;
                }
                elevatorController.callElevatorClosest(floorName, goUp != null ? ElevatorStatus.upOrDown(goUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.PLAYER_COMMAND);
                p.sendMessage(Translate.chat("Called for the nearest elevator to go to floor: " + floorName + " on controller: " + elevatorController.getControllerName()));
                return true;
            }
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
        } else if (args[0].equalsIgnoreCase("settings")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&a&l&o[Elevator Settings Help]"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bticksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bdoorHolderTicksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &belevatorWaiterTicksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bdoElevatorLeveling &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &barrivalSound &3<Sound> <Volume> <Pitch>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bpassingByFloorSound &3<Sound> <Volume> <Pitch"));
            } else if (args.length > 2) {
                ElevatorCommandCustomArguments eleArgs = getArgumentsElevators(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                ElevatorObject elevatorObject = eleArgs.getElevatorObject();
                String setting = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 0);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }
                if (elevatorObject == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }
                if (setting == null) {
                    p.sendMessage(Translate.chat("Setting option cannot be null."));
                    return true;
                }

                if (setting.equalsIgnoreCase("ticksPerSecond")) {
                    long valueLong = api.asLongOrDefault(api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1), ElevatorSettings.DEFAULT_TICKS_PER_SECOND);
                    elevatorObject.getElevatorSettings().setTicksPerSecond(valueLong);
                    p.sendMessage(Translate.chat("The ticks per second has been updated to: " + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("doorHolderTicksPerSecond")) {
                    long valueLong = api.asLongOrDefault(api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1), ElevatorSettings.DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND);
                    elevatorObject.getElevatorSettings().setDoorHolderTicksPerSecond(valueLong);
                    p.sendMessage(Translate.chat("The door holder ticks per second has been updated to: " + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("elevatorWaiterTicksPerSecond")) {
                    long valueLong = api.asLongOrDefault(api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1), ElevatorSettings.DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND);
                    elevatorObject.getElevatorSettings().setElevatorWaiterTicksPerSecond(valueLong);
                    p.sendMessage(Translate.chat("The elevator waiter ticks per second has been updated to: " + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("doElevatorLeveling")) {
                    boolean bool = api.asBooleanOrDefault(api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1), true);
                    elevatorObject.getElevatorSettings().setDoElevatorLeveling(bool);
                    p.sendMessage(Translate.chat("The doLevelingSystem has been set to: " + bool));
                    return true;
                } else if (setting.equalsIgnoreCase("onlyTwoFloors")) {
                    boolean bool = api.asBooleanOrDefault(api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1), false);
                    elevatorObject.getElevatorSettings().setOnlyTwoFloors(bool);
                    p.sendMessage(Translate.chat("onlyTwoFloors has been set to: " + bool));
                    return true;
                } else if (setting.equalsIgnoreCase("arrivalSound") || setting.equalsIgnoreCase("passingByFloorSound")) {
                    String fakeSound = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 1);
                    String fakeVolume = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 2);
                    String fakePitch = api.getIndexFromStringArrayList(eleArgs.getOtherArgs(), 3);
                    if (fakeSound == null || fakeVolume == null || fakePitch == null) {
                        p.sendMessage(Translate.chat("sound is null or volume is null or pitch is null."));
                        return true;
                    }
                    if (fakeSound.equalsIgnoreCase("null")) {
                        if (setting.equalsIgnoreCase("arrivalSound")) {
                            elevatorObject.getElevatorSettings().setArrivalSound(null);
                            p.sendMessage(Translate.chat("Removed arrival sound."));
                        } else if (setting.equalsIgnoreCase("passingByFloorSound")) {
                            elevatorObject.getElevatorSettings().setPassingByFloorSound(null);
                            p.sendMessage(Translate.chat("Removed passing by floor sound."));
                        }
                        return true;
                    }

                    Sound sound = Sound.valueOf(fakeSound);
                    float volume = api.asFloatOrDefault(fakeVolume, 99.1F);
                    float pitch = api.asFloatOrDefault(fakePitch, 99.1F);

                    if (volume == 91.1F || pitch == 91.1F) {
                        p.sendMessage(Translate.chat("Volume or pitch is messed up."));
                        return true;
                    }
                    ElevatorSound elevatorSound = new ElevatorSound(sound, volume, pitch);
                    if (setting.equalsIgnoreCase("arrivalSound")) {
                        elevatorObject.getElevatorSettings().setArrivalSound(elevatorSound);
                    } else if (setting.equalsIgnoreCase("passingByFloorSound")) {
                        elevatorObject.getElevatorSettings().setPassingByFloorSound(elevatorSound);
                    }

                    return true;
                }
                return true;
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
            if (ElevatorObject.isDoor(api.getBlockPlayerIsLookingAt(p).getLocation()) != null) {
                p.sendMessage(Translate.chat("Is a door.."));
            } else p.sendMessage(Translate.chat("Isn't a door."));
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