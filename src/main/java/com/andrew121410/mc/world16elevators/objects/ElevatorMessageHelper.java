package com.andrew121410.mc.world16elevators.objects;

import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16utils.chat.ChatResponseManager;
import com.andrew121410.mc.world16utils.chat.Translate;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ElevatorMessageHelper {

    private World16Elevators plugin;
    private ElevatorObject elevatorObject;

    private boolean isRunning;
    private List<UUID> players;
    private int counter;

    public ElevatorMessageHelper(World16Elevators plugin, ElevatorObject elevatorObject) {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.elevatorObject = elevatorObject;
    }

    private void messageSetup() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning) {
                    this.cancel();
                    return;
                }
                counter++;

                //When the player isn't in the elevator remove them from the list.
                Iterator<UUID> iterator = players.iterator();
                while (iterator.hasNext()) {
                    UUID uuid = iterator.next();
                    ArrayList<UUID> uuidArrayList = (ArrayList<UUID>) elevatorObject.getPlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());
                    if (!uuidArrayList.contains(uuid)) iterator.remove();
                }

                //After 20 seconds check if players are in the elevator if not then stop the message helper.
                if (counter >= 20) {
                    if (players.isEmpty() && !elevatorObject.isGoing() && !elevatorObject.isIdling()) {
                        stop();
                        return;
                    }
                }

                for (Player player : elevatorObject.getPlayers()) {
                    //The player already got the message.
                    if (players.contains(player.getUniqueId())) {
                        return;
                    }

                    FloorQueueObject floorQueueObject = getNextFloor(elevatorObject.getElevatorMovement().getFloor());
                    if (elevatorObject.getElevatorSettings().isOnlyTwoFloors() && floorQueueObject != null) {
                        FloorObject floorObject = elevatorObject.getFloor(floorQueueObject.getFloorNumber());
                        elevatorObject.goToFloor(floorQueueObject.getFloorNumber(), floorQueueObject.getElevatorStatus(), ElevatorWho.MESSAGE_HELPER);
                        player.sendMessage(Translate.color("&6ElevatorMessageHelper: &9Going to floor: " + floorObject.getName()));
                        players.add(player.getUniqueId());
                        return;
                    }
                    switch (elevatorObject.getElevatorSettings().getFloorSelectorType()) {
                        case CLICK_CHAT -> {
                            if (World16Elevators.getInstance().getOtherPlugins().hasFloodgate()) {
                                if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                    handleFloodgatePlayer(player, elevatorObject);
                                    break;
                                }
                            }
                            elevatorObject.clickMessageGoto(player);
                        }
                        case GUI -> {
                            if (World16Elevators.getInstance().getOtherPlugins().hasFloodgate()) {
                                if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                    handleFloodgatePlayer(player, elevatorObject);
                                    break;
                                }
                            }
                            GUIElevatorFloors guiElevatorFloors = new GUIElevatorFloors(plugin, elevatorObject);
                            guiElevatorFloors.open(player);
                        }
                        case CHAT_RESPONSE -> {
                            ChatResponseManager chatResponseManager = plugin.getOtherPlugins().getWorld16Utils().getChatResponseManager();
                            player.sendMessage("Please type in the chat your response");
                            elevatorObject.elevatorFloorsMessage(player);
                            chatResponseManager.create(player, Translate.color("&bWhat floor?"), "", (thePlayer, response) -> {
                                plugin.getServer().dispatchCommand(player, "elevator call " + elevatorObject.getElevatorControllerName() + " " + elevatorObject.getElevatorName() + " " + response);
                            });
                        }
                    }
                    players.add(player.getUniqueId());
                }
            }
        }.runTaskTimer(plugin, 1L, 20L);
    }

    private void handleFloodgatePlayer(Player player, ElevatorObject elevatorObject) {
        SimpleForm.Builder simpleForm = SimpleForm.builder().title("Floors").content("List of floors!");
        elevatorObject.getFloorsMap().forEach((floorNumber, floorObject) -> {
            String realName = floorObject.getName();
            simpleForm.button(realName);
        });
        simpleForm.responseHandler((form, data) -> {
            SimpleFormResponse simpleFormResponse = form.parseResponse(data);
            if (!simpleFormResponse.isCorrect()) return;
            plugin.getServer().dispatchCommand(player, "elevator call " + elevatorObject.getElevatorControllerName() + " " + elevatorObject.getElevatorName() + " " + simpleFormResponse.getClickedButton().getText());
        });
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), simpleForm.build());
    }

    public void start() {
        if (isRunning) return;
        this.counter = 0;
        this.players = new ArrayList<>();
        this.isRunning = true;
        messageSetup();
    }

    public void stop() {
        this.isRunning = false;
    }

    public FloorQueueObject getNextFloor(int floorNumber) {
        FloorObject floorObject = null;
        ElevatorStatus elevatorStatus = null;
        if (floorNumber == 1) {
            floorObject = elevatorObject.getFloor(2);
            if (floorObject == null) {
                floorObject = elevatorObject.getFloor(-1);
                elevatorStatus = ElevatorStatus.UP;
            } else {
                elevatorStatus = ElevatorStatus.DOWN;
            }
        } else if (floorNumber == 2) {
            floorObject = elevatorObject.getFloor(1);
            elevatorStatus = ElevatorStatus.UP;
        } else if (floorNumber == -1) {
            floorObject = elevatorObject.getFloor(1);
            elevatorStatus = ElevatorStatus.DOWN;
        }
        if (floorObject == null) return null;
        return new FloorQueueObject(floorObject.getFloor(), elevatorStatus);
    }

    public boolean isRunning() {
        return isRunning;
    }
}
