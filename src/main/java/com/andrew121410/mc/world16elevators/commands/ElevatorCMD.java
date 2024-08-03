package com.andrew121410.mc.world16elevators.commands;

import com.andrew121410.mc.world16elevators.*;
import com.andrew121410.mc.world16elevators.commands.tabcomplete.ElevatorTab;
import com.andrew121410.mc.world16elevators.enums.ElevatorCallButtonType;
import com.andrew121410.mc.world16elevators.enums.ElevatorFloorSelectorType;
import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16elevators.enums.ElevatorWho;
import com.andrew121410.mc.world16elevators.storage.ElevatorManager;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.player.PlayerUtils;
import com.andrew121410.mc.world16utils.utils.Utils;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ElevatorCMD implements CommandExecutor {

    private final World16Elevators plugin;

    private final ElevatorManager elevatorManager;

    private final Map<String, ElevatorController> elevatorControllerMap;

    public ElevatorCMD(World16Elevators plugin) {
        this.plugin = plugin;

        this.elevatorControllerMap = this.plugin.getMemoryHolder().getElevatorControllerMap();
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
                    ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                    String stringFloor = eleArgs.getOtherArgumentsAt(0);
                    Boolean isGoingUp = eleArgs.getOtherArgumentsAt(1) != null ? Boolean.parseBoolean(eleArgs.getOtherArgumentsAt(1)) : null;

                    ElevatorController elevatorController = eleArgs.getElevatorController();
                    if (elevatorController == null) {
                        commandBlockSender.sendMessage("elevatorController == null");
                        return true;
                    }

                    if (eleArgs.getElevator() != null) {
                        Elevator elevator = eleArgs.getElevator();
                        if (elevator == null) {
                            commandBlockSender.sendMessage("elevatorObject == null");
                            return true;
                        }
                        if (elevator.getFloor(stringFloor) == null) {
                            commandBlockSender.sendMessage("getFloor == null");
                            return true;
                        }
                        elevator.goToFloor(stringFloor, isGoingUp != null ? ElevatorStatus.upOrDown(isGoingUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.COMMAND_BLOCK);
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
            p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(Translate.color("&6/elevator create &e<Controller> &9<Elevator> &a<FloorName>"));
            p.sendMessage(Translate.color("&6/elevator delete &e<Controller> &9<Elevator>"));
            p.sendMessage(Translate.color("&6/elevator stop &e<Controller> &9<Elevator>"));
            p.sendMessage(Translate.color("&6/elevator click &e<Controller> &9<Elevator>"));
            p.sendMessage(Translate.color("&6/elevator rename &e<Controller> &9<Elevator> &a<TOElevatorName>"));
            p.sendMessage(Translate.color("&6/elevator opendoor &e<Controller> &9<Elevator> <SecondsUntilDoorCloses>"));
            p.sendMessage(Translate.chat("&6/elevator copysettingsfrom &e<Controller> &9<Elevator> &e<Controller> &9<Elevator>"));
            p.sendMessage("");
            p.sendMessage(Translate.color("&a&lMORE HELP COMMANDS..."));
            p.sendMessage("");
            p.sendMessage(Translate.color("&6/elevator controller &d<Shows help for creation of a controller.>"));
            p.sendMessage(Translate.color("&6/elevator floor &d<Shows help for the floor."));
            p.sendMessage(Translate.color("&6/elevator call &d<Shows help to call the elevator.>"));
            p.sendMessage(Translate.color("&6/elevator settings &d<Shows help to change the settings.>"));
            return true;
            //Create controller
        } else if (args[0].equalsIgnoreCase("controller")) {
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&6/elevator controller create &e<Controller>"));
                p.sendMessage(Translate.chat("&6/elevator controller delete &e<Controller>"));
                return true;
            } else if (args.length == 3 && args[1].equalsIgnoreCase("create")) {
                if (!p.hasPermission("world16elevators.controller.create")) {
                    p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                    return true;
                }
                String controllerName = args[2].toLowerCase();

                if (elevatorControllerMap.get(controllerName) != null) {
                    p.sendMessage(Translate.chat("Looks like that's already a controller name."));
                    return true;
                }

                this.elevatorControllerMap.putIfAbsent(controllerName, new ElevatorController(plugin, controllerName));
                p.sendMessage(Translate.chat("ElevatorController has been registered with the name of " + controllerName));
                return true;
            } else if (args.length == 3 && args[1].equalsIgnoreCase("delete")) {
                if (!p.hasPermission("world16elevators.controller.delete")) {
                    p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                    return true;
                }

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
            if (!p.hasPermission("world16elevators.create")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&6/elevator create &e<Controller> &9<ElevatorName> &a<FloorName>"));
                return true;
            } else if (args.length == 4) {
                Block blockPlayerIsLookingAt = PlayerUtils.getBlockPlayerIsLookingAt(p);
                ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                String floorName = eleArgs.getOtherArgumentsAt(1);
                BoundingBox region = this.plugin.getOtherPlugins().getWorld16Utils().getClassWrappers().getWorldEdit().getRegion(p);

                if (region == null) {
                    p.sendMessage(Translate.chat("&cYou didn't make a WorldEdit selection... [FAILED]"));
                    return true;
                }

                ElevatorController elevatorController = eleArgs.getElevatorController();
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }

                if (eleArgs.getElevator() != null) {
                    p.sendMessage("Elevator with that name looks to already exist on that elevator controller.");
                    return true;
                }
                String elevatorName = eleArgs.getOtherArgumentsAt(0);

                ElevatorMovement elevatorMovement = new ElevatorMovement(1, blockPlayerIsLookingAt.getLocation().clone(), region);
                Elevator elevator = new Elevator(this.plugin, elevatorName, p.getWorld().getName(), elevatorMovement);
                ElevatorFloor elevatorFloor = new ElevatorFloor(1, floorName, blockPlayerIsLookingAt.getLocation().clone());
                elevator.addFloor(elevatorFloor);

                elevatorController.registerElevator(elevatorName, elevator);
                p.sendMessage(Translate.chat("The elevator: " + elevatorName + " has been registered to " + elevatorController.getControllerName()));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("floor")) {
            if (args.length >= 5) {
                ElevatorArguments elevatorArguments = getElevatorArguments(args, 3);
                ElevatorController elevatorController = elevatorArguments.getElevatorController();
                Elevator elevator = elevatorArguments.getElevator();
                String floorName = elevatorArguments.getOtherArgumentsAt(0);

                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }
                if (elevator == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }

                if (args[1].equalsIgnoreCase("create")) {
                    if (!p.hasPermission("world16elevators.floor.create")) {
                        p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    elevator.addFloor(new ElevatorFloor(floorName, PlayerUtils.getBlockPlayerIsLookingAt(p).getLocation()));
                    p.sendMessage(Translate.color("&e[&9Elevator&e] &6Floor:" + floorName + " has been added to elevator: " + elevator.getElevatorName()));
                    return true;
                } else if (args[1].equalsIgnoreCase("delete")) {
                    if (!p.hasPermission("world16elevators.floor.delete")) {
                        p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        p.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    elevator.deleteFloor(floorName);
                    p.sendMessage(Translate.chat("The floor: " + floorName + " has been removed from the elevator: " + elevator.getElevatorName()));
                    return true;
                } else if (args[1].equalsIgnoreCase("setname") && args.length == 6) {
                    if (!p.hasPermission("world16elevators.floor.setname")) {
                        p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        p.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    String toFloorName = elevatorArguments.getOtherArgumentsAt(1);
                    elevatorFloor.setName(toFloorName);
                    p.sendMessage(Translate.color("&6Elevator floor name has been set to: " + toFloorName));
                    return true;
                } else if (args[1].equalsIgnoreCase("sign")) {
                    if (!p.hasPermission("world16elevators.floor.sign")) {
                        p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        p.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    elevatorFloor.getSignList().add(new ElevatorSign(PlayerUtils.getBlockPlayerIsLookingAt(p).getLocation()));
                    p.sendMessage(Translate.color("&e[&9Elevator&e] &6Floor: " + elevatorFloor.getName() + " has been set."));
                    return true;
                } else if (args[1].equalsIgnoreCase("door") && args.length == 6) {
                    if (!p.hasPermission("world16elevators.floor.door")) {
                        p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }
                    Location location = ElevatorFloor.ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(PlayerUtils.getBlockPlayerIsLookingAt(p).getLocation()).getLocation();
                    String addOrRemove = elevatorArguments.getOtherArgumentsAt(1);

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        p.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    if (addOrRemove.equalsIgnoreCase("add")) {
                        elevatorFloor.getDoorList().add(location);
                        p.sendMessage(Translate.chat("The door for the floor: " + elevatorFloor.getFloor() + " has been added to the elevator: " + elevator.getElevatorName()));
                    } else if (addOrRemove.equalsIgnoreCase("remove") || addOrRemove.equalsIgnoreCase("delete")) {
                        elevatorFloor.getDoorList().remove(location);
                        p.sendMessage(Translate.chat("The door for the floor: " + elevatorFloor.getFloor() + " has been deleted for the elevator: " + elevator.getElevatorName()));
                    }
                    return true;
                } else if (args[1].equalsIgnoreCase("permission") && args.length == 6) {
                    if (!p.hasPermission("world16elevators.floor.permission")) {
                        p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }
                    String permission = elevatorArguments.getOtherArgumentsAt(1);

                    if (permission.equalsIgnoreCase("null")) permission = null;

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        p.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    elevatorFloor.setPermission(permission);
                    if (permission != null) {
                        p.sendMessage("Changed permission for floor " + elevatorFloor.getName() + " to " + permission);
                    } else {
                        p.sendMessage("Removed permission on floor " + elevatorFloor.getName());
                    }
                    return true;
                } else if (args[1].equalsIgnoreCase("smartCreateFloors") && args.length == 6) {
                    if (!p.hasPermission("world16elevators.floor.smartcreatefloors")) {
                        p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    ElevatorFloor elevatorFloor = elevator.getFloor(elevatorArguments.getOtherArgumentsAt(0));
                    if (elevatorFloor == null) {
                        p.sendMessage(Translate.color("&cCouldn't find floor"));
                        return true;
                    }

                    Boolean bool = Utils.asBooleanOrElse(elevatorArguments.getOtherArgumentsAt(1), null);
                    if (bool == null) {
                        p.sendMessage(Translate.color("Not a true/false"));
                        return true;
                    }

                    elevator.smartCreateFloors(elevatorFloor, bool);
                    p.sendMessage(Translate.color("&esmartCreateFloors has started."));
                    return true;
                }
            } else {
                p.sendMessage(Translate.color("&a&l&o[Elevator Floor Help]"));
                p.sendMessage(Translate.color("&6/elevator floor create &e<Controller> &9<Elevator> &a<Floor>"));
                p.sendMessage(Translate.color("&6/elevator floor delete &e<Controller> &9<Elevator> &a<Floor>"));
                p.sendMessage(Translate.color("&6/elevator floor setName &e<Controller> &9<Elevator> &a<FloorInt> &a<ToName>"));
                p.sendMessage(Translate.color("&6/elevator floor sign &e<Controller> &9<Elevator> &a<Floor>"));
                p.sendMessage(Translate.color("&6/elevator floor door &e<Controller> &9<Elevator> &a<Floor> &b<ADD OR DELETE>"));
                p.sendMessage(Translate.color("&6/elevator floor permission &e<Controller> &9<Elevator> &a<Floor> <Permission>"));
                p.sendMessage(Translate.color("&6/elevator floor smartCreateFloors &e<Controller> &9<Elevator> &a<FromFloor> &c<GoUp>"));
            }
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("delete")) {
            if (!p.hasPermission("world16elevators.delete")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            ElevatorArguments elevatorArguments = getElevatorArguments(args, 2);

            if (elevatorArguments.getElevatorController() == null) {
                p.sendMessage("Elevator controller was not found.");
                return true;
            }
            if (elevatorArguments.getElevator() == null) {
                p.sendMessage(Translate.chat("That elevator doesn't exist."));
                return true;
            }
            String controllerName = elevatorArguments.getElevatorController().getControllerName();
            String elevatorName = elevatorArguments.getElevator().getElevatorName();

            this.plugin.getElevatorManager().deleteElevator(controllerName, elevatorName);
            p.sendMessage(Translate.chat("Elevator: " + elevatorName + " has been deleted from controller: " + controllerName));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
            if (!p.hasPermission("world16elevators.admin")) return true;
            this.plugin.getElevatorManager().saveAllElevators();
            p.sendMessage(Translate.chat("All elevators have been saved."));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("load")) {
            if (!p.hasPermission("world16elevators.admin")) return true;
            this.plugin.getElevatorManager().loadAllElevatorControllers();
            p.sendMessage(Translate.chat("All elevators have been loaded in memory."));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            if (!p.hasPermission("world16elevators.admin")) return true;
            elevatorControllerMap.clear();
            p.sendMessage(Translate.chat("All elevators have been cleared in memory."));
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("toString")) {
            if (!p.hasPermission("world16elevators.admin")) return true;
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2];

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
            if (!p.hasPermission("world16elevators.stop")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }

            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2];

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                p.sendMessage("Elevator controller was not found.");
                return true;
            }

            Elevator elevator = elevatorController.getElevator(elevatorName);
            if (elevator == null) {
                p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                return true;
            }

            elevator.emergencyStop();
            p.sendMessage(Translate.chat("emergency stop has been activated."));
            return true;
        } else if (args[0].equalsIgnoreCase("call")) {
            if (!p.hasPermission("world16elevators.call")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }

            if (args.length == 1) {
                p.sendMessage(Translate.chat("&a&l&o[Elevator Call Help]"));
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &9<ElevatorName> &b<Floor>"));
                p.sendMessage(Translate.chat("&6/elevator call &e<Controller> &a<Floor>"));
                return true;
            } else {
                ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                Elevator elevator = eleArgs.getElevator();
                String floorName = eleArgs.getOtherArgumentsAt(0);
                Boolean goUp = eleArgs.getOtherArgumentsAt(1) != null ? Boolean.parseBoolean(eleArgs.getOtherArgumentsAt(1)) : null;
                if (elevatorController == null) {
                    p.sendMessage(Translate.chat("elevatorController cannot be null."));
                    return true;
                }
                if (floorName == null) {
                    p.sendMessage(Translate.chat("floorName cannot be null."));
                    return true;
                }

                if (elevator != null) {
                    elevator.goToFloor(p, floorName, goUp != null ? ElevatorStatus.upOrDown(goUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.PLAYER_COMMAND);
                    return true;
                } else {
                    elevatorController.callElevatorClosest(floorName, goUp != null ? ElevatorStatus.upOrDown(goUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.PLAYER_COMMAND);
                    p.sendMessage(Translate.chat("Called for the nearest elevator to go to floor: " + floorName + " on controller: " + elevatorController.getControllerName()));
                }
                return true;
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("rename")) {
            if (!p.hasPermission("world16elevators.rename")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2];
            String toElevatorName = args[3];

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                p.sendMessage("Elevator controller was not found.");
                return true;
            }

            Elevator elevator = elevatorController.getElevator(elevatorName);
            if (elevator == null) {
                p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                return true;
            }

            elevatorManager.deleteElevator(controllerName, elevatorName);
            elevator.setElevatorName(toElevatorName);
            elevatorController.registerElevator(toElevatorName, elevator);
            p.sendMessage(Translate.chat("Old Name: " + elevatorName + " new Name: " + toElevatorName));
            return true;
        } else if (args[0].equalsIgnoreCase("settings")) {
            if (!p.hasPermission("world16elevators.settings")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&a&l&o[Elevator Settings Help]"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bticksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bdoorHolderTicksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &belevatorWaiterTicksPerSecond &3<Value>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bdoElevatorLeveling &3<Bool>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bonlyTwoFloors &3<Bool>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &barrivalSound &3<Sound> <Volume> <Pitch>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bpassingByFloorSound &3<Sound> <Volume> <Pitch"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bcallSystemType &3<Type>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bcallButtonSystem &3<Bool>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bsignFinderSystem &3<Bool>"));
                p.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bteleportElevatorOnEmpty &3<Bool>"));
            } else if (args.length > 2) {
                ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                Elevator elevator = eleArgs.getElevator();
                String setting = eleArgs.getOtherArgumentsAt(0);
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }
                if (elevator == null) {
                    p.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }
                if (setting == null) {
                    p.sendMessage(Translate.chat("Setting option cannot be null."));
                    return true;
                }

                if (setting.equalsIgnoreCase("ticksPerSecond")) {
                    long valueLong = Utils.asLongOrElse(eleArgs.getOtherArgumentsAt(1), ElevatorSettings.DEFAULT_TICKS_PER_SECOND);
                    elevator.getElevatorSettings().setTicksPerSecond(valueLong);
                    p.sendMessage(Translate.chat("The ticks per second has been updated to: " + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("doorHolderTicksPerSecond")) {
                    long valueLong = Utils.asLongOrElse(eleArgs.getOtherArgumentsAt(1), ElevatorSettings.DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND);
                    elevator.getElevatorSettings().setDoorHolderTicksPerSecond(valueLong);
                    p.sendMessage(Translate.chat("The door holder ticks per second has been updated to: " + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("elevatorWaiterTicksPerSecond")) {
                    long valueLong = Utils.asLongOrElse(eleArgs.getOtherArgumentsAt(1), ElevatorSettings.DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND);
                    elevator.getElevatorSettings().setElevatorWaiterTicksPerSecond(valueLong);
                    p.sendMessage(Translate.chat("The elevator waiter ticks per second has been updated to: " + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("doElevatorLeveling")) {
                    boolean bool = Utils.asBooleanOrElse(eleArgs.getOtherArgumentsAt(1), true);
                    elevator.getElevatorSettings().setDoElevatorLeveling(bool);
                    p.sendMessage(Translate.chat("The doLevelingSystem has been set to: " + bool));
                    return true;
                } else if (setting.equalsIgnoreCase("onlyTwoFloors")) {
                    boolean bool = Utils.asBooleanOrElse(eleArgs.getOtherArgumentsAt(1), false);
                    elevator.getElevatorSettings().setOnlyTwoFloors(bool);
                    p.sendMessage(Translate.chat("onlyTwoFloors has been set to: " + bool));
                    return true;
                } else if (setting.equalsIgnoreCase("arrivalSound") || setting.equalsIgnoreCase("passingByFloorSound")) {
                    if (args.length == 7) {
                        String fakeSound = eleArgs.getOtherArgumentsAt(1);
                        String fakeVolume = eleArgs.getOtherArgumentsAt(2);
                        String fakePitch = eleArgs.getOtherArgumentsAt(3);
                        if (fakeSound == null || fakeVolume == null || fakePitch == null) {
                            p.sendMessage(Translate.chat("sound is null, or volume is null, or pitch is null."));
                            return true;
                        }
                        Sound sound = Sound.valueOf(fakeSound);
                        float volume = Utils.asFloatOrElse(fakeVolume, 99.1F);
                        float pitch = Utils.asFloatOrElse(fakePitch, 99.1F);

                        if (volume == 91.1F || pitch == 91.1F) {
                            p.sendMessage(Translate.chat("Volume or pitch is messed up."));
                            return true;
                        }
                        ElevatorSound elevatorSound = new ElevatorSound(sound, volume, pitch);
                        if (setting.equalsIgnoreCase("arrivalSound")) {
                            elevator.getElevatorSettings().setArrivalSound(elevatorSound);
                            p.sendMessage("Sound was set to: " + elevatorSound);
                        } else if (setting.equalsIgnoreCase("passingByFloorSound")) {
                            elevator.getElevatorSettings().setPassingByFloorSound(elevatorSound);
                            p.sendMessage("Sound was set to: " + elevatorSound);
                        }

                        return true;
                    } else if (args.length == 4) {
                        if (setting.equalsIgnoreCase("arrivalSound")) {
                            elevator.getElevatorSettings().setArrivalSound(null);
                            p.sendMessage(Translate.chat("Removed arrival sound."));
                        } else {
                            elevator.getElevatorSettings().setPassingByFloorSound(null);
                            p.sendMessage(Translate.chat("Removed passing by floor sound."));
                        }
                        return true;
                    }
                    return true;
                } else if (setting.equalsIgnoreCase("floorSelectorType")) {
                    ElevatorFloorSelectorType elevatorFloorSelectorType;
                    try {
                        elevatorFloorSelectorType = ElevatorFloorSelectorType.valueOf(eleArgs.getOtherArgumentsAt(1));
                    } catch (Exception ignored) {
                        p.sendMessage(Translate.color("&cThat's not a valid ElevatorFloorSelectorType"));
                        return true;
                    }
                    elevator.getElevatorSettings().setFloorSelectorType(elevatorFloorSelectorType);
                    p.sendMessage(Translate.color("floorSelectorType has been set to " + elevatorFloorSelectorType.name()));
                    return true;
                } else if (setting.equalsIgnoreCase("callButtonType")) {
                    ElevatorCallButtonType elevatorCallButtonType;
                    try {
                        elevatorCallButtonType = ElevatorCallButtonType.valueOf(eleArgs.getOtherArgumentsAt(1));
                    } catch (Exception ignored) {
                        p.sendMessage(Translate.color("&cThat's not a valid ElevatorCallButtonType"));
                        return true;
                    }
                    elevator.getElevatorSettings().setCallButtonType(elevatorCallButtonType);
                    p.sendMessage(Translate.color("callButtonType has been set to " + elevatorCallButtonType.name()));
                    return true;
                } else if (setting.equalsIgnoreCase("signFinderSystem")) {
                    boolean bool = Utils.asBooleanOrElse(eleArgs.getOtherArgumentsAt(1), true);
                    elevator.getElevatorSettings().setSignFinderSystem(bool);
                    p.sendMessage(Translate.chat("The signFinderSystem has been set to: " + bool));
                    return true;
                } else if (setting.equalsIgnoreCase("teleportElevatorOnEmpty")) {
                    boolean bool = Utils.asBooleanOrElse(eleArgs.getOtherArgumentsAt(1), false);
                    elevator.getElevatorSettings().setTeleportElevatorOnEmpty(bool);
                    p.sendMessage(Translate.chat("The teleportElevatorOnEmpty has been set to: " + bool));
                    return true;
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("queue")) {
            if (!p.hasPermission("world16elevators.admin")) return true;
            if (args.length == 1) {
                p.sendMessage(Translate.chat("&6/elevator queue &e<Controller> floorQueueBuffer list/clear"));
                p.sendMessage(Translate.chat("&6/elevator queue &e<Controller> &9<Elevator> floorQueueBuffer list/clear"));
                return true;
            } else {
                ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }
                Elevator elevator = eleArgs.getElevator();
                String whatToRemove = eleArgs.getOtherArgumentsAt(0);
                if (whatToRemove == null) {
                    p.sendMessage("whatToBeRemoved cannot be null.");
                    return true;
                }
                String setting = eleArgs.getOtherArgumentsAt(1);
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
                        if (elevator == null) {
                            elevatorController.getElevatorsMap().forEach((eleName, eleObject) -> mainComponentBuilder.append(makeQueueChatComponent(eleObject).create()));
                        } else mainComponentBuilder.append(makeQueueChatComponent(elevator).create());
                        p.spigot().sendMessage(mainComponentBuilder.create());
                        return true;
                    } else if (setting.equalsIgnoreCase("clear")) {
                        if (elevator == null) {
                            elevatorController.getElevatorsMap().forEach((eleName, eleObject) -> eleObject.getFloorQueueBuffer().clear());
                            p.sendMessage(Translate.chat("FloorQueueBuffer has been cleared on all elevators on the controller."));
                        } else {
                            elevator.getFloorQueueBuffer().clear();
                            p.sendMessage(Translate.chat("FloorQueueBuffer has been cleared for " + elevator.getElevatorName() + " elevator."));
                        }
                        return true;
                    }
                    return true;
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("opendoor") && args.length == 4) {
            if (!p.hasPermission("world16elevators.opendoor")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            ElevatorArguments eleArgs = getElevatorArguments(args, 2);

            ElevatorController elevatorController = eleArgs.getElevatorController();
            if (elevatorController == null) {
                p.sendMessage(Translate.color("Elevator Controller wasn't found."));
                return true;
            }

            Elevator elevator = eleArgs.getElevator();
            if (elevator == null) {
                p.sendMessage(Translate.color("Elevator wasn't found on that controller."));
                return true;
            }

            String secondsString = eleArgs.getOtherArgumentsAt(0);
            if (secondsString == null) {
                p.sendMessage("SecondsString cannot be null");
                return true;
            }
            Integer seconds = Utils.asIntegerOrElse(secondsString, null);
            if (seconds == null) {
                p.sendMessage(Translate.color("Not a int."));
                return true;
            }

            Integer currentFloor = elevator.getElevatorMovement().getFloor();
            if (currentFloor == null) {
                p.sendMessage("It seems that the elevator isn't on a floor the elevator must not be running for this to work.");
                return true;
            }
            ElevatorFloor elevatorFloor = elevator.getFloor(currentFloor);
            elevatorFloor.doDoor(true, true);
            p.sendMessage("Opened all doors for that floor.");
            new BukkitRunnable() {
                @Override
                public void run() {
                    elevatorFloor.doDoor(false, true);
                }
            }.runTaskLater(plugin, 20L * seconds);
        } else if (args[0].equalsIgnoreCase("copysettingsfrom")) {
            if (!p.hasPermission("world16elevators.copysettingsfrom")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length == 5) {
                ElevatorArguments elevatorArguments = getElevatorArguments(args, 2);

                ElevatorController elevatorControllerFrom = elevatorArguments.getElevatorController();
                if (elevatorControllerFrom == null) {
                    p.sendMessage("from: That's not a valid elevator controller");
                    return true;
                }
                Elevator elevatorFrom = elevatorArguments.getElevator();
                if (elevatorFrom == null) {
                    p.sendMessage("from: That's not a valid elevator");
                    return true;
                }
                ElevatorController elevatorControllerTo = this.elevatorControllerMap.getOrDefault(elevatorArguments.getOtherArgumentsAt(0), null);
                if (elevatorControllerTo == null) {
                    p.sendMessage("to: That's not a valid elevator controller");
                    return true;
                }
                Elevator elevatorTo = elevatorControllerTo.getElevator(elevatorArguments.getOtherArgumentsAt(1));
                if (elevatorTo == null) {
                    p.sendMessage("to: That's not a valid elevator");
                    return true;
                }

                ElevatorSettings clone = elevatorFrom.getElevatorSettings().clone();
                elevatorTo.setElevatorSettings(clone);
                p.sendMessage("Successfully cloned to " + elevatorTo.getElevatorName());
                return true;
            } else {
                p.sendMessage(Translate.chat("&6/elevator copysettingsfrom &e<Controller> &9<Elevator> &e<Controller> &9<Elevator>"));
            }
        } else if (args[0].equalsIgnoreCase("teleport")) { // /elevator teleport <controller> <elevator>
            if (!p.hasPermission("world16elevators.teleport")) {
                p.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length == 3) {
                ElevatorArguments elevatorArguments = getElevatorArguments(args, 2);
                ElevatorController elevatorController = elevatorArguments.getElevatorController();
                if (elevatorController == null) {
                    p.sendMessage("Elevator controller was not found.");
                    return true;
                }
                Elevator elevator = elevatorArguments.getElevator();
                if (elevator == null) {
                    p.sendMessage("Elevator was not found.");
                    return true;
                }

                Location location = elevator.getElevatorMovement().getAtDoor();

                // Find a spot to teleport the player. 3x3 area.
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location newLocation = location.clone().add(x, 0, z);

                        Location newLocation1 = newLocation.clone().add(0, 1, 0);
                        Location newLocation2 = newLocation1.clone().add(0, 1, 0);

                        if (!newLocation.getBlock().getType().isAir()) {
                            if (newLocation1.getBlock().getType().isAir() && newLocation2.getBlock().getType().isAir()) {
                                location = newLocation1;
                                break;
                            }
                        } else {
                            break;
                        }

                    }
                }

                p.teleport(location);
                p.sendMessage(Translate.miniMessage("<gold>You have been teleported to the elevator."));
                return true;
            } else {
                p.sendMessage(Translate.chat("&6/elevator teleport &e<Controller> &9<Elevator>"));
            }
        }
        return true;
    }

    private ElevatorArguments getElevatorArguments(String[] args, int start) {
        ElevatorArguments elevatorArguments = new ElevatorArguments();
        String[] newStringArray = Arrays.copyOfRange(args, start - 1, args.length);
        ArrayList<String> otherArgs = new ArrayList<>();
        for (int i = 0; i < newStringArray.length; i++) {
            if (i == 0) {
                elevatorArguments.setElevatorController(this.elevatorControllerMap.get(newStringArray[0]));
            } else if (i == 1 && elevatorArguments.getElevatorController() != null && elevatorArguments.getElevatorController().getElevatorsMap().containsKey((newStringArray[1]))) {
                elevatorArguments.setElevator(elevatorArguments.getElevatorController().getElevatorsMap().get(newStringArray[1]));
            } else otherArgs.add(newStringArray[i]);
        }
        elevatorArguments.setOtherArgs(otherArgs);
        return elevatorArguments;
    }

    private ComponentBuilder makeQueueChatComponent(Elevator eleObject) {
        ComponentBuilder floorQueueObjectStringBuilder = new ComponentBuilder();
        floorQueueObjectStringBuilder.color(ChatColor.BLUE).bold(false);
        floorQueueObjectStringBuilder.append("Elevator: " + eleObject.getElevatorName()).append("\n").color(ChatColor.YELLOW).bold(false);
        for (FloorQueueObject floorQueueObject : eleObject.getFloorQueueBuffer()) {
            ComponentBuilder removeFloorFromFloorQueueBuffer = new ComponentBuilder().append("Floor: " + floorQueueObject.getFloorNumber()).append(" ").append("Status: " + floorQueueObject.getElevatorStatus().name()).append("\n");
            floorQueueObjectStringBuilder.append(removeFloorFromFloorQueueBuffer.create());
        }
        return floorQueueObjectStringBuilder;
    }
}

@NoArgsConstructor
class ElevatorArguments {
    @Getter
    @Setter
    private ElevatorController elevatorController = null;
    @Getter
    @Setter
    private Elevator elevator = null;
    @Setter
    private ArrayList<String> otherArgs = null;

    public String getOtherArgumentsAt(int index) {
        return Utils.getIndexFromStringList(otherArgs, index) != null ? otherArgs.get(index) : null;
    }
}