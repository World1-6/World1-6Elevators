package com.andrew121410.mc.world16elevators.commands;

import com.andrew121410.mc.world16elevators.*;
import com.andrew121410.mc.world16elevators.commands.tabcomplete.ElevatorTab;
import com.andrew121410.mc.world16elevators.enums.ElevatorCallButtonType;
import com.andrew121410.mc.world16elevators.enums.ElevatorFloorSelectorType;
import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16elevators.enums.ElevatorWho;
import com.andrew121410.mc.world16elevators.storage.ElevatorManager;
import com.andrew121410.mc.world16utils.chat.ChatClickCallbackManager;
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
import org.bukkit.Material;
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

        Player player = (Player) sender;
        if (!player.hasPermission("world16elevators.elevator")) {
            player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Translate.color("&6/elevator create &e<Controller> &9<Elevator> &a<FloorName>"));
            player.sendMessage(Translate.color("&6/elevator delete &e<Controller> &9<Elevator>"));
            player.sendMessage(Translate.color("&6/elevator stop &e<Controller> &9<Elevator>"));
            player.sendMessage(Translate.color("&6/elevator click &e<Controller> &9<Elevator>"));
            player.sendMessage(Translate.color("&6/elevator rename &e<Controller> &9<Elevator> &a<TOElevatorName>"));
            player.sendMessage(Translate.color("&6/elevator opendoor &e<Controller> &9<Elevator> <SecondsUntilDoorCloses>"));
            player.sendMessage(Translate.chat("&6/elevator copysettingsfrom &e<Controller> &9<Elevator> &e<Controller> &9<Elevator>"));
            player.sendMessage("");
            player.sendMessage(Translate.color("&a&lMORE HELP COMMANDS..."));
            player.sendMessage("");
            player.sendMessage(Translate.color("&6/elevator controller &d<Shows help for creation of a controller.>"));
            player.sendMessage(Translate.color("&6/elevator floor &d<Shows help for the floor."));
            player.sendMessage(Translate.color("&6/elevator call &d<Shows help to call the elevator.>"));
            player.sendMessage(Translate.color("&6/elevator settings &d<Shows help to change the settings.>"));
            return true;
        } else if (args[0].equalsIgnoreCase("controller")) {
            if (args.length == 1) {
                player.sendMessage(Translate.chat("&6/elevator controller create &e<Controller>"));
                player.sendMessage(Translate.chat("&6/elevator controller delete &e<Controller>"));
                return true;
            } else if (args.length == 3 && args[1].equalsIgnoreCase("create")) { // /elevator controller create <Controller>
                if (!player.hasPermission("world16elevators.controller.create")) {
                    player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                    return true;
                }
                String controllerName = args[2].toLowerCase();

                if (elevatorControllerMap.get(controllerName) != null) {
                    player.sendMessage(Translate.chat("Looks like that's already a controller name."));
                    return true;
                }

                this.elevatorControllerMap.putIfAbsent(controllerName, new ElevatorController(plugin, controllerName));
                player.sendMessage(Translate.miniMessage("<green>ElevatorController has been registered with the name of <white>" + controllerName));
                return true;
            } else if (args.length == 3 && args[1].equalsIgnoreCase("delete")) { // /elevator controller delete <Controller>
                if (!player.hasPermission("world16elevators.controller.delete")) {
                    player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                    return true;
                }

                String controllerName = args[2].toLowerCase();

                if (elevatorControllerMap.get(controllerName) == null) {
                    player.sendMessage(Translate.chat("Looks like that's not a valid controller."));
                    return true;
                }

                this.elevatorManager.deleteElevatorController(controllerName);
                player.sendMessage(Translate.miniMessage("<red>ElevatorController has been deleted with the name of <white>" + controllerName));
                return true;
            }
            return true;
        } else if (args[0].equalsIgnoreCase("create")) { // /elevator create <Controller> <ElevatorName> <FloorName>
            if (!player.hasPermission("world16elevators.create")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }

            if (args.length == 4) {
                Block blockPlayerIsLookingAt = PlayerUtils.getBlockPlayerIsLookingAt(player);
                ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                String floorName = eleArgs.getOtherArgumentsAt(1);
                BoundingBox region = this.plugin.getOtherPlugins().getWorld16Utils().getClassWrappers().getWorldEdit().getRegion(player);

                if (region == null) {
                    player.sendMessage(Translate.miniMessage("<red>You need to select a region with WorldEdit."));
                    return true;
                }

                ElevatorController elevatorController = eleArgs.getElevatorController();
                if (elevatorController == null) {
                    player.sendMessage(Translate.miniMessage("<red>Elevator controller was not found."));
                    return true;
                }

                if (eleArgs.getElevator() != null) {
                    player.sendMessage(Translate.miniMessage("<red>That elevator already exists in the controller."));
                    return true;
                }
                String elevatorName = eleArgs.getOtherArgumentsAt(0);

                ElevatorMovement elevatorMovement = new ElevatorMovement(1, blockPlayerIsLookingAt.getLocation().clone(), region);
                Elevator elevator = new Elevator(this.plugin, elevatorName, player.getWorld().getName(), elevatorMovement);
                ElevatorFloor elevatorFloor = new ElevatorFloor(1, floorName, blockPlayerIsLookingAt.getLocation().clone());
                elevator.addFloor(elevatorFloor);

                elevatorController.registerElevator(elevatorName, elevator);
                player.sendMessage(Translate.miniMessage("<green>The elevator: <white>" + elevatorName + " <green>has been registered to <white>" + elevatorController.getControllerName()));
                return true;
            } else {
                player.sendMessage(Translate.chat("&6/elevator create &e<Controller> &9<ElevatorName> &a<FloorName>"));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("floor")) {
            if (args.length >= 5) {
                ElevatorArguments elevatorArguments = getElevatorArguments(args, 3);
                ElevatorController elevatorController = elevatorArguments.getElevatorController();
                Elevator elevator = elevatorArguments.getElevator();
                String floorName = elevatorArguments.getOtherArgumentsAt(0);

                if (elevatorController == null) {
                    player.sendMessage("Elevator controller was not found.");
                    return true;
                }
                if (elevator == null) {
                    player.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }

                if (args[1].equalsIgnoreCase("create")) {
                    if (!player.hasPermission("world16elevators.floor.create")) {
                        player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    elevator.addFloor(new ElevatorFloor(floorName, PlayerUtils.getBlockPlayerIsLookingAt(player).getLocation()));
                    player.sendMessage(Translate.color("&e[&9Elevator&e] &6Floor:" + floorName + " has been added to elevator: " + elevator.getElevatorName()));
                    return true;
                } else if (args[1].equalsIgnoreCase("delete")) {
                    if (!player.hasPermission("world16elevators.floor.delete")) {
                        player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        player.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    elevator.deleteFloor(floorName);
                    player.sendMessage(Translate.chat("The floor: " + floorName + " has been removed from the elevator: " + elevator.getElevatorName()));
                    return true;
                } else if (args[1].equalsIgnoreCase("setname") && args.length == 6) {
                    if (!player.hasPermission("world16elevators.floor.setname")) {
                        player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        player.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    String toFloorName = elevatorArguments.getOtherArgumentsAt(1);
                    elevatorFloor.setName(toFloorName);
                    player.sendMessage(Translate.color("&6Elevator floor name has been set to: " + toFloorName));
                    return true;
                } else if (args[1].equalsIgnoreCase("sign")) {
                    if (!player.hasPermission("world16elevators.floor.sign")) {
                        player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        player.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    elevatorFloor.getSignList().add(new ElevatorSign(PlayerUtils.getBlockPlayerIsLookingAt(player).getLocation()));
                    player.sendMessage(Translate.color("&e[&9Elevator&e] &6Floor: " + elevatorFloor.getName() + " has been set."));
                    return true;
                } else if (args[1].equalsIgnoreCase("door") && args.length == 6) {
                    if (!player.hasPermission("world16elevators.floor.door")) {
                        player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }
                    Location location = ElevatorFloor.ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(PlayerUtils.getBlockPlayerIsLookingAt(player).getLocation()).getLocation();
                    String addOrRemove = elevatorArguments.getOtherArgumentsAt(1);

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        player.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    if (addOrRemove.equalsIgnoreCase("add")) {
                        elevatorFloor.getDoorList().add(location);
                        player.sendMessage(Translate.chat("The door for the floor: " + elevatorFloor.getFloor() + " has been added to the elevator: " + elevator.getElevatorName()));
                    } else if (addOrRemove.equalsIgnoreCase("remove") || addOrRemove.equalsIgnoreCase("delete")) {
                        elevatorFloor.getDoorList().remove(location);
                        player.sendMessage(Translate.chat("The door for the floor: " + elevatorFloor.getFloor() + " has been deleted for the elevator: " + elevator.getElevatorName()));
                    }
                    return true;
                } else if (args[1].equalsIgnoreCase("permission") && args.length == 6) {
                    if (!player.hasPermission("world16elevators.floor.permission")) {
                        player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }
                    String permission = elevatorArguments.getOtherArgumentsAt(1);

                    if (permission.equalsIgnoreCase("null")) permission = null;

                    ElevatorFloor elevatorFloor = elevator.getFloor(floorName);
                    if (elevatorFloor == null) {
                        player.sendMessage(Translate.chat("This floor doesn't exist."));
                        return true;
                    }

                    elevatorFloor.setPermission(permission);
                    if (permission != null) {
                        player.sendMessage("Changed permission for floor " + elevatorFloor.getName() + " to " + permission);
                    } else {
                        player.sendMessage("Removed permission on floor " + elevatorFloor.getName());
                    }
                    return true;
                } else if (args[1].equalsIgnoreCase("smartCreateFloors") && args.length == 6) {
                    if (!player.hasPermission("world16elevators.floor.smartcreatefloors")) {
                        player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                        return true;
                    }

                    ElevatorFloor elevatorFloor = elevator.getFloor(elevatorArguments.getOtherArgumentsAt(0));
                    if (elevatorFloor == null) {
                        player.sendMessage(Translate.color("&cCouldn't find floor"));
                        return true;
                    }

                    Boolean bool = Utils.asBooleanOrElse(elevatorArguments.getOtherArgumentsAt(1), null);
                    if (bool == null) {
                        player.sendMessage(Translate.color("Not a true/false"));
                        return true;
                    }

                    elevator.smartCreateFloors(elevatorFloor, bool);
                    player.sendMessage(Translate.color("&esmartCreateFloors has started."));
                    return true;
                }
            } else {
                player.sendMessage(Translate.color("&a&l&o[Elevator Floor Help]"));
                player.sendMessage(Translate.color("&6/elevator floor create &e<Controller> &9<Elevator> &a<Floor>"));
                player.sendMessage(Translate.color("&6/elevator floor delete &e<Controller> &9<Elevator> &a<Floor>"));
                player.sendMessage(Translate.color("&6/elevator floor setName &e<Controller> &9<Elevator> &a<FloorInt> &a<ToName>"));
                player.sendMessage(Translate.color("&6/elevator floor sign &e<Controller> &9<Elevator> &a<Floor>"));
                player.sendMessage(Translate.color("&6/elevator floor door &e<Controller> &9<Elevator> &a<Floor> &b<ADD OR DELETE>"));
                player.sendMessage(Translate.color("&6/elevator floor permission &e<Controller> &9<Elevator> &a<Floor> <Permission>"));
                player.sendMessage(Translate.color("&6/elevator floor smartCreateFloors &e<Controller> &9<Elevator> &a<FromFloor> &c<GoUp>"));
            }
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("delete")) {
            if (!player.hasPermission("world16elevators.delete")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            ElevatorArguments elevatorArguments = getElevatorArguments(args, 2);

            if (elevatorArguments.getElevatorController() == null) {
                player.sendMessage("Elevator controller was not found.");
                return true;
            }
            if (elevatorArguments.getElevator() == null) {
                player.sendMessage(Translate.chat("That elevator doesn't exist."));
                return true;
            }
            String controllerName = elevatorArguments.getElevatorController().getControllerName();
            String elevatorName = elevatorArguments.getElevator().getElevatorName();

            this.plugin.getElevatorManager().deleteElevator(controllerName, elevatorName);
            player.sendMessage(Translate.chat("Elevator: " + elevatorName + " has been deleted from controller: " + controllerName));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
            if (!player.hasPermission("world16elevators.admin")) return true;
            this.plugin.getElevatorManager().saveAllElevators();
            player.sendMessage(Translate.chat("All elevators have been saved."));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("load")) {
            if (!player.hasPermission("world16elevators.admin")) return true;
            this.plugin.getElevatorManager().loadAllElevatorControllers();
            player.sendMessage(Translate.chat("All elevators have been loaded in memory."));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            if (!player.hasPermission("world16elevators.admin")) return true;
            elevatorControllerMap.clear();
            player.sendMessage(Translate.chat("All elevators have been cleared in memory."));
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("toString")) {
            if (!player.hasPermission("world16elevators.admin")) return true;
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2];

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                player.sendMessage("Elevator controller was not found.");
                return true;
            }

            if (elevatorController.getElevatorsMap().get(elevatorName) == null) {
                player.sendMessage(Translate.chat("That elevator doesn't exist."));
                return true;
            }

            this.plugin.getServer().getConsoleSender().sendMessage(elevatorController.getElevatorsMap().get(elevatorName).toString());
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("stop")) {
            if (!player.hasPermission("world16elevators.stop")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }

            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2];

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                player.sendMessage("Elevator controller was not found.");
                return true;
            }

            Elevator elevator = elevatorController.getElevator(elevatorName);
            if (elevator == null) {
                player.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                return true;
            }

            elevator.emergencyStop();
            player.sendMessage(Translate.chat("emergency stop has been activated."));
            return true;
        } else if (args[0].equalsIgnoreCase("call")) {
            if (!player.hasPermission("world16elevators.call")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }

            if (args.length == 1) {
                player.sendMessage(Translate.chat("&a&l&o[Elevator Call Help]"));
                player.sendMessage(Translate.chat("&6/elevator call &e<Controller> &9<ElevatorName> &b<Floor>"));
                player.sendMessage(Translate.chat("&6/elevator call &e<Controller> &a<Floor>"));
                return true;
            } else {
                ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                Elevator elevator = eleArgs.getElevator();
                String floorName = eleArgs.getOtherArgumentsAt(0);
                Boolean goUp = eleArgs.getOtherArgumentsAt(1) != null ? Boolean.parseBoolean(eleArgs.getOtherArgumentsAt(1)) : null;
                if (elevatorController == null) {
                    player.sendMessage(Translate.chat("elevatorController cannot be null."));
                    return true;
                }
                if (floorName == null) {
                    player.sendMessage(Translate.chat("floorName cannot be null."));
                    return true;
                }

                if (elevator != null) {
                    elevator.goToFloor(player, floorName, goUp != null ? ElevatorStatus.upOrDown(goUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.PLAYER_COMMAND);
                    return true;
                } else {
                    elevatorController.callElevatorClosest(floorName, goUp != null ? ElevatorStatus.upOrDown(goUp) : ElevatorStatus.DONT_KNOW, ElevatorWho.PLAYER_COMMAND);
                    player.sendMessage(Translate.chat("Called for the nearest elevator to go to floor: " + floorName + " on controller: " + elevatorController.getControllerName()));
                }
                return true;
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("rename")) {
            if (!player.hasPermission("world16elevators.rename")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            String controllerName = args[1].toLowerCase();
            String elevatorName = args[2];
            String toElevatorName = args[3];

            ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
            if (elevatorController == null) {
                player.sendMessage("Elevator controller was not found.");
                return true;
            }

            Elevator elevator = elevatorController.getElevator(elevatorName);
            if (elevator == null) {
                player.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                return true;
            }

            elevatorManager.deleteElevator(controllerName, elevatorName);
            elevator.setElevatorName(toElevatorName);
            elevatorController.registerElevator(toElevatorName, elevator);
            player.sendMessage(Translate.chat("Old Name: " + elevatorName + " new Name: " + toElevatorName));
            return true;
        } else if (args[0].equalsIgnoreCase("settings")) {
            if (!player.hasPermission("world16elevators.settings")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length == 1) {
                player.sendMessage(Translate.chat("&a&l&o[Elevator Settings Help]"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bticksPerSecond &3<Value>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bdoorHolderTicksPerSecond &3<Value>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &belevatorWaiterTicksPerSecond &3<Value>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bdoElevatorLeveling &3<Bool>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bonlyTwoFloors &3<Bool>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &barrivalSound &3<Sound> <Volume> <Pitch>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bpassingByFloorSound &3<Sound> <Volume> <Pitch"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bcallSystemType &3<Type>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bcallButtonSystem &3<Bool>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bsignFinderSystem &3<Bool>"));
                player.sendMessage(Translate.chat("&6/elevator settings &e<Controller> &9<Elevator> &bteleportElevatorOnEmpty &3<Bool>"));
            } else if (args.length > 2) {
                ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                Elevator elevator = eleArgs.getElevator();
                String setting = eleArgs.getOtherArgumentsAt(0);
                if (elevatorController == null) {
                    player.sendMessage("Elevator controller was not found.");
                    return true;
                }
                if (elevator == null) {
                    player.sendMessage(Translate.chat("That elevator doesn't exist in the controller."));
                    return true;
                }
                if (setting == null) {
                    player.sendMessage(Translate.chat("Setting option cannot be null."));
                    return true;
                }

                String value = eleArgs.getOtherArgumentsAt(1);
                if (setting.equalsIgnoreCase("ticksPerSecond")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default ticks per second is: <white>" + ElevatorSettings.DEFAULT_TICKS_PER_SECOND));
                    Long valueLong = Utils.asLongOrElse(value, null);
                    if (valueLong == null) {
                        player.sendMessage(Translate.miniMessage("<gold>The current ticks per second is: <white>" + elevator.getElevatorSettings().getTicksPerSecond()));
                        return true;
                    }
                    elevator.getElevatorSettings().setTicksPerSecond(valueLong);
                    player.sendMessage(Translate.miniMessage("<green>The ticks per second has been updated to: <white>" + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("doorHolderTicksPerSecond")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default door holder ticks per second is: <white>" + ElevatorSettings.DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND));
                    Long valueLong = Utils.asLongOrElse(value, null);
                    if (valueLong == null) {
                        player.sendMessage(Translate.miniMessage("<gold>The current door holder ticks per second is: <white>" + elevator.getElevatorSettings().getDoorHolderTicksPerSecond()));
                        return true;
                    }
                    elevator.getElevatorSettings().setDoorHolderTicksPerSecond(valueLong);
                    player.sendMessage(Translate.miniMessage("<green>The door holder ticks per second has been updated to: <white>" + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("elevatorWaiterTicksPerSecond")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default elevator waiter ticks per second is: <white>" + ElevatorSettings.DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND));
                    Long valueLong = Utils.asLongOrElse(value, null);
                    if (valueLong == null) {
                        player.sendMessage(Translate.miniMessage("<gold>The current elevator waiter ticks per second is: <white>" + elevator.getElevatorSettings().getElevatorWaiterTicksPerSecond()));
                        return true;
                    }
                    elevator.getElevatorSettings().setElevatorWaiterTicksPerSecond(valueLong);
                    player.sendMessage(Translate.miniMessage("<green>The elevator waiter ticks per second has been updated to: <white>" + valueLong));
                    return true;
                } else if (setting.equalsIgnoreCase("doElevatorLeveling")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default do elevator leveling is: <white>true"));
                    Boolean bool = Utils.asBooleanOrElse(value, null);
                    if (bool == null) {
                        player.sendMessage(Translate.miniMessage("<gold>The current do elevator leveling is: <white>" + elevator.getElevatorSettings().isDoElevatorLeveling()));
                        return true;
                    }
                    elevator.getElevatorSettings().setDoElevatorLeveling(bool);
                    player.sendMessage(Translate.miniMessage("<green>The do elevator leveling has been updated to: <white>" + bool));
                    return true;
                } else if (setting.equalsIgnoreCase("onlyTwoFloors")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default only two floors is: <white>false"));
                    Boolean bool = Utils.asBooleanOrElse(value, null);
                    if (bool == null) {
                        player.sendMessage(Translate.miniMessage("<gold>The current only two floors is: <white>" + elevator.getElevatorSettings().isOnlyTwoFloors()));
                        return true;
                    }
                    elevator.getElevatorSettings().setOnlyTwoFloors(bool);
                    player.sendMessage(Translate.miniMessage("<green>The only two floors has been updated to: <white>" + bool));
                    return true;
                } else if (setting.equalsIgnoreCase("arrivalSound") || setting.equalsIgnoreCase("passingByFloorSound")) {
                    if (args.length == 7) { // /elevator settings <Controller> <Elevator> <Setting> <Sound> <Volume> <Pitch>
                        String fakeSound = eleArgs.getOtherArgumentsAt(1);
                        String fakeVolume = eleArgs.getOtherArgumentsAt(2);
                        String fakePitch = eleArgs.getOtherArgumentsAt(3);

                        Sound sound;
                        try {
                            sound = Sound.valueOf(fakeSound);
                        } catch (Exception e) {
                            player.sendMessage(Translate.miniMessage("<red>Invalid sound."));
                            return true;
                        }

                        Float volume = Utils.asFloatOrElse(fakeVolume, null);
                        if (volume == null) {
                            player.sendMessage(Translate.miniMessage("<red>Invalid volume."));
                            return true;
                        }

                        Float pitch = Utils.asFloatOrElse(fakePitch, null);
                        if (pitch == null) {
                            player.sendMessage(Translate.miniMessage("<red>Invalid pitch."));
                            return true;
                        }

                        ElevatorSound elevatorSound = new ElevatorSound(sound, volume, pitch);
                        if (setting.equalsIgnoreCase("arrivalSound")) {
                            elevator.getElevatorSettings().setArrivalSound(elevatorSound);
                            player.sendMessage(Translate.miniMessage("<green>The arrival sound has been updated."));
                        } else {
                            elevator.getElevatorSettings().setPassingByFloorSound(elevatorSound);
                            player.sendMessage(Translate.miniMessage("<green>The passing by floor sound has been updated."));
                        }
                        return true;
                    } else if (args.length == 4) { // /elevator settings <Controller> <Elevator> <Setting>
                        // Display the current sound
                        if (setting.equalsIgnoreCase("arrivalSound")) {
                            player.sendMessage(Translate.miniMessage("<gold>The current arrival sound is: <white>" + elevator.getElevatorSettings().getArrivalSound()));
                        } else {
                            player.sendMessage(Translate.miniMessage("<gold>The current passing by floor sound is: <white>" + elevator.getElevatorSettings().getPassingByFloorSound()));
                        }
                        player.sendMessage(Translate.miniMessage("<gray>Btw... Use null keyword for the sound to have no sound, also don't provide the volume and pitch when using null."));

                        // Usage: /elevator settings <Controller> <Elevator> <Setting> <Sound> <Volume> <Pitch>
                        player.sendMessage(Translate.miniMessage("<yellow>Usage: /elevator settings <Controller> <Elevator> <Setting> <Sound> <Volume> <Pitch>"));
                        return true;
                    } else if (args.length == 5) { // /elevator settings <Controller> <Elevator> <Setting> <null>
                        if (value.equalsIgnoreCase("null")) {
                            if (setting.equalsIgnoreCase("arrivalSound")) {
                                elevator.getElevatorSettings().setArrivalSound(null);
                                player.sendMessage(Translate.miniMessage("<green>Removed the arrival sound."));
                            } else {
                                elevator.getElevatorSettings().setPassingByFloorSound(null);
                                player.sendMessage(Translate.miniMessage("<green>Removed the passing by floor sound."));
                            }
                        }
                    }
                } else if (setting.equalsIgnoreCase("floorSelectorType")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default floor selector type is: <white>" + ElevatorFloorSelectorType.CLICK_CHAT));
                    ElevatorFloorSelectorType elevatorFloorSelectorType;
                    try {
                        elevatorFloorSelectorType = ElevatorFloorSelectorType.valueOf(value);
                    } catch (Exception ignored) {
                        player.sendMessage(Translate.miniMessage("<red>That's not a valid ElevatorFloorSelectorType."));
                        return true;
                    }
                    elevator.getElevatorSettings().setFloorSelectorType(elevatorFloorSelectorType);
                    player.sendMessage(Translate.miniMessage("<green>The floor selector type has been updated to: <white>" + elevatorFloorSelectorType.name()));
                    return true;
                } else if (setting.equalsIgnoreCase("callButtonType")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default call button type is: <white>" + ElevatorCallButtonType.CALL_THE_ELEVATOR));
                    ElevatorCallButtonType elevatorCallButtonType;
                    try {
                        elevatorCallButtonType = ElevatorCallButtonType.valueOf(value);
                    } catch (Exception ignored) {
                        player.sendMessage(Translate.miniMessage("<red>That's not a valid ElevatorCallButtonType."));
                        return true;
                    }
                    elevator.getElevatorSettings().setCallButtonType(elevatorCallButtonType);
                    player.sendMessage(Translate.miniMessage("<green>The call button type has been updated to: <white>" + elevatorCallButtonType.name()));
                    return true;
                } else if (setting.equalsIgnoreCase("signFinderSystem")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default sign finder system is: <white>true"));
                    Boolean bool = Utils.asBooleanOrElse(value, null);
                    if (bool == null) {
                        player.sendMessage(Translate.miniMessage("<gold>The current sign finder system is: <white>" + elevator.getElevatorSettings().isSignFinderSystem()));
                        return true;
                    }
                    elevator.getElevatorSettings().setSignFinderSystem(bool);
                    player.sendMessage(Translate.miniMessage("<green>The sign finder system has been updated to: <white>" + bool));
                    return true;
                } else if (setting.equalsIgnoreCase("teleportElevatorOnEmpty")) {
                    player.sendMessage(Translate.miniMessage("<gray>The default teleport elevator on empty is: <white>false"));
                    Boolean bool = Utils.asBooleanOrElse(value, null);
                    if (bool == null) {
                        player.sendMessage(Translate.miniMessage("<gold>The current teleport elevator on empty is: <white>" + elevator.getElevatorSettings().isTeleportElevatorOnEmpty()));
                        return true;
                    }
                    elevator.getElevatorSettings().setTeleportElevatorOnEmpty(bool);
                    player.sendMessage(Translate.miniMessage("<green>The teleport elevator on empty has been updated to: <white>" + bool));
                    return true;
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("queue")) {
            if (!player.hasPermission("world16elevators.admin")) return true;
            if (args.length == 1) {
                player.sendMessage(Translate.chat("&6/elevator queue &e<Controller> floorQueueBuffer list/clear"));
                player.sendMessage(Translate.chat("&6/elevator queue &e<Controller> &9<Elevator> floorQueueBuffer list/clear"));
                return true;
            } else {
                ElevatorArguments eleArgs = getElevatorArguments(args, 2);
                ElevatorController elevatorController = eleArgs.getElevatorController();
                if (elevatorController == null) {
                    player.sendMessage("Elevator controller was not found.");
                    return true;
                }
                Elevator elevator = eleArgs.getElevator();
                String whatToRemove = eleArgs.getOtherArgumentsAt(0);
                if (whatToRemove == null) {
                    player.sendMessage("whatToBeRemoved cannot be null.");
                    return true;
                }
                String setting = eleArgs.getOtherArgumentsAt(1);
                if (setting == null) {
                    player.sendMessage(Translate.chat("setting cannot be null."));
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
                        player.spigot().sendMessage(mainComponentBuilder.create());
                        return true;
                    } else if (setting.equalsIgnoreCase("clear")) {
                        if (elevator == null) {
                            elevatorController.getElevatorsMap().forEach((eleName, eleObject) -> eleObject.getFloorQueueBuffer().clear());
                            player.sendMessage(Translate.chat("FloorQueueBuffer has been cleared on all elevators on the controller."));
                        } else {
                            elevator.getFloorQueueBuffer().clear();
                            player.sendMessage(Translate.chat("FloorQueueBuffer has been cleared for " + elevator.getElevatorName() + " elevator."));
                        }
                        return true;
                    }
                    return true;
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("opendoor") && args.length == 4) {
            if (!player.hasPermission("world16elevators.opendoor")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            ElevatorArguments eleArgs = getElevatorArguments(args, 2);

            ElevatorController elevatorController = eleArgs.getElevatorController();
            if (elevatorController == null) {
                player.sendMessage(Translate.color("Elevator Controller wasn't found."));
                return true;
            }

            Elevator elevator = eleArgs.getElevator();
            if (elevator == null) {
                player.sendMessage(Translate.color("Elevator wasn't found on that controller."));
                return true;
            }

            String secondsString = eleArgs.getOtherArgumentsAt(0);
            if (secondsString == null) {
                player.sendMessage("SecondsString cannot be null");
                return true;
            }
            Integer seconds = Utils.asIntegerOrElse(secondsString, null);
            if (seconds == null) {
                player.sendMessage(Translate.color("Not a int."));
                return true;
            }

            Integer currentFloor = elevator.getElevatorMovement().getFloor();
            if (currentFloor == null) {
                player.sendMessage("It seems that the elevator isn't on a floor the elevator must not be running for this to work.");
                return true;
            }
            ElevatorFloor elevatorFloor = elevator.getFloor(currentFloor);
            elevatorFloor.doDoor(true, true);
            player.sendMessage("Opened all doors for that floor.");
            new BukkitRunnable() {
                @Override
                public void run() {
                    elevatorFloor.doDoor(false, true);
                }
            }.runTaskLater(plugin, 20L * seconds);
        } else if (args[0].equalsIgnoreCase("copysettingsfrom")) {
            if (!player.hasPermission("world16elevators.copysettingsfrom")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length == 5) {
                ElevatorArguments elevatorArguments = getElevatorArguments(args, 2);

                ElevatorController elevatorControllerFrom = elevatorArguments.getElevatorController();
                if (elevatorControllerFrom == null) {
                    player.sendMessage("from: That's not a valid elevator controller");
                    return true;
                }
                Elevator elevatorFrom = elevatorArguments.getElevator();
                if (elevatorFrom == null) {
                    player.sendMessage("from: That's not a valid elevator");
                    return true;
                }
                ElevatorController elevatorControllerTo = this.elevatorControllerMap.getOrDefault(elevatorArguments.getOtherArgumentsAt(0), null);
                if (elevatorControllerTo == null) {
                    player.sendMessage("to: That's not a valid elevator controller");
                    return true;
                }
                Elevator elevatorTo = elevatorControllerTo.getElevator(elevatorArguments.getOtherArgumentsAt(1));
                if (elevatorTo == null) {
                    player.sendMessage("to: That's not a valid elevator");
                    return true;
                }

                ElevatorSettings clone = elevatorFrom.getElevatorSettings().clone();
                elevatorTo.setElevatorSettings(clone);
                player.sendMessage("Successfully cloned to " + elevatorTo.getElevatorName());
                return true;
            } else {
                player.sendMessage(Translate.chat("&6/elevator copysettingsfrom &e<Controller> &9<Elevator> &e<Controller> &9<Elevator>"));
            }
        } else if (args[0].equalsIgnoreCase("teleport")) { // /elevator teleport <controller> <elevator>
            if (!player.hasPermission("world16elevators.teleport")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length == 3) {
                ElevatorArguments elevatorArguments = getElevatorArguments(args, 2);
                ElevatorController elevatorController = elevatorArguments.getElevatorController();
                if (elevatorController == null) {
                    player.sendMessage("Elevator controller was not found.");
                    return true;
                }
                Elevator elevator = elevatorArguments.getElevator();
                if (elevator == null) {
                    player.sendMessage("Elevator was not found.");
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

                player.teleport(location);
                player.sendMessage(Translate.miniMessage("<gold>You have been teleported to the elevator."));
                return true;
            } else {
                player.sendMessage(Translate.chat("&6/elevator teleport &e<Controller> &9<Elevator>"));
            }
        } else if (args[0].equalsIgnoreCase("realign")) { // /elevator realign <controller> <elevator>
            if (!player.hasPermission("world16elevators.realign")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length == 3) {
                ElevatorArguments elevatorArguments = getElevatorArguments(args, 2);
                ElevatorController elevatorController = elevatorArguments.getElevatorController();
                if (elevatorController == null) {
                    player.sendMessage("Elevator controller was not found.");
                    return true;
                }
                Elevator elevator = elevatorArguments.getElevator();
                if (elevator == null) {
                    player.sendMessage("Elevator was not found.");
                    return true;
                }

                ChatClickCallbackManager chatClickCallbackManager = this.plugin.getOtherPlugins().getWorld16Utils().getChatClickCallbackManager();
                Map<Location, Material> blockMap = elevator.fixUnalignedElevator(player, false);

                player.sendMessage(Translate.miniMessage("<rainbow>PLEASE go check to see if the elevator is aligned correctly. Look for diamond blocks."));
                player.sendMessage(Translate.miniMessage("<yellow><u>Click here to undo the diamond block changes.").clickEvent(chatClickCallbackManager.create(player, p1 -> {
                    blockMap.forEach((location, material) -> location.getBlock().setType(material));
                    p1.sendMessage(Translate.miniMessage("<green>The original blocks have been restored."));
                })));
                player.sendMessage("");
                player.sendMessage("");
                player.sendMessage(Translate.miniMessage("<red><bold><u>CLICK HERE TO CONFIRM THE CHANGES.").clickEvent(chatClickCallbackManager.create(player, p1 -> {
                    elevator.fixUnalignedElevator(player, true);
                    blockMap.forEach((location, material) -> location.getBlock().setType(material));
                    p1.sendMessage(Translate.miniMessage("<green>The changes have been confirmed."));
                })));
                return true;
            } else {
                player.sendMessage(Translate.chat("&6/elevator realign &e<Controller> &9<Elevator>"));
            }
        } else if (args[0].equalsIgnoreCase("boundingbox")) { // elevator boundingbox <controller> <elevator> <show/shift> <y>
            if (!player.hasPermission("world16elevators.boundingbox")) {
                player.sendMessage(Translate.color("&bYou don't have permission to use this command."));
                return true;
            }
            if (args.length >= 4) {
                ElevatorArguments elevatorArguments = getElevatorArguments(args, 2);
                ElevatorController elevatorController = elevatorArguments.getElevatorController();
                if (elevatorController == null) {
                    player.sendMessage("Elevator controller was not found.");
                    return true;
                }
                Elevator elevator = elevatorArguments.getElevator();
                if (elevator == null) {
                    player.sendMessage("Elevator was not found.");
                    return true;
                }

                String setting = elevatorArguments.getOtherArgumentsAt(0);
                if (setting == null) {
                    player.sendMessage(Translate.miniMessage("<red>Setting cannot be null."));
                    return true;
                }

                ChatClickCallbackManager chatClickCallbackManager = this.plugin.getOtherPlugins().getWorld16Utils().getChatClickCallbackManager();
                if (setting.equalsIgnoreCase("show")) {
                    Map<Location, Material> blockMap = elevator.showLocationOfElevator(null, null, null, null);

                    player.sendMessage(Translate.miniMessage("<yellow><u>Click here to undo the block changes").clickEvent(chatClickCallbackManager.create(player, p1 -> {
                        elevator.showLocationOfElevator(blockMap, null, null, null); // Undo the changes.
                        p1.sendMessage(Translate.miniMessage("<green>The original blocks have been restored."));
                    })));

                    player.sendMessage(Translate.miniMessage("<green>The normal bounding box is the diamond blocks, and the expanded bounding box is the redstone blocks."));
                } else if (setting.equalsIgnoreCase("shift")) {
                    Integer y = Utils.asIntegerOrElse(elevatorArguments.getOtherArgumentsAt(1), null);
                    if (y == null) {
                        player.sendMessage("Y cannot be null.");
                        return true;
                    }

                    player.sendMessage(Translate.miniMessage("<gray>You are about to shift the elevator by " + y + " blocks."));

                    // Copy the stuff
                    BoundingBox copyBoundingBox = elevator.getElevatorMovement().getBoundingBox().clone();
                    BoundingBox copyExpandedBoundingBox = elevator.getBoundingBoxExpanded().clone();
                    Location copyAtDoor = elevator.getElevatorMovement().getAtDoor().clone();

                    // Shift the stuff
                    copyBoundingBox.shift(0, y, 0);
                    copyExpandedBoundingBox.shift(0, y, 0);
                    copyAtDoor.add(0, y, 0);

                    // Show the blocks.
                    Map<Location, Material> blockMap = elevator.showLocationOfElevator(null, copyAtDoor, copyBoundingBox, copyExpandedBoundingBox);

                    // Click here to confirm the changes.
                    player.sendMessage(Translate.miniMessage("<red><bold><u>CLICK HERE TO CONFIRM THE CHANGES.").clickEvent(chatClickCallbackManager.create(player, p1 -> {
                        // Remove the blocks.
                        elevator.showLocationOfElevator(blockMap, copyAtDoor, copyBoundingBox, copyExpandedBoundingBox);

                        // Set the new values.
                        elevator.getElevatorMovement().setBoundingBox(copyBoundingBox);
                        elevator.setBoundingBoxExpanded(copyExpandedBoundingBox);
                        elevator.getElevatorMovement().setAtDoor(copyAtDoor);

                        p1.sendMessage(Translate.miniMessage("<green>The changes have been confirmed."));
                    })));

                    // Click here to undo the block changes if you don't want to confirm the changes.
                    player.sendMessage(Translate.miniMessage("<yellow><u>Click here to undo the block changes").clickEvent(chatClickCallbackManager.create(player, p1 -> {
                        // Undo the changes.
                        elevator.showLocationOfElevator(blockMap, copyAtDoor, copyBoundingBox, copyExpandedBoundingBox);
                        p1.sendMessage(Translate.miniMessage("<green>The original blocks have been restored."));
                    })));
                }
                return true;
            } else {
                player.sendMessage(Translate.color("&6/elevator boundingbox &e<Controller> &9<Elevator> &eshow/shift &3<y>"));
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