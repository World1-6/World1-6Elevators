package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16elevators.enums.ElevatorWho;
import com.andrew121410.mc.world16utils.chat.ChatResponseManager;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.gui.GUIWindow;
import com.andrew121410.mc.world16utils.gui.buttons.AbstractGUIButton;
import com.andrew121410.mc.world16utils.gui.buttons.defaults.ClickEventButton;
import com.andrew121410.mc.world16utils.utils.InventoryUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;

public class ElevatorFloorSelectorManager {

    private final World16Elevators plugin;
    private final ElevatorObject elevatorObject;

    private boolean isRunning;
    private List<UUID> players;
    private int counter;

    public ElevatorFloorSelectorManager(World16Elevators plugin, ElevatorObject elevatorObject) {
        this.plugin = plugin;
        this.elevatorObject = elevatorObject;
        this.players = new ArrayList<>();
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

                // When the player isn't in the elevator remove them from the list.
                Iterator<UUID> iterator = players.iterator();
                while (iterator.hasNext()) {
                    UUID uuid = iterator.next();
                    ArrayList<UUID> uuidArrayList = (ArrayList<UUID>) elevatorObject.getPlayersUUIDs();
                    if (!uuidArrayList.contains(uuid)) iterator.remove();
                }

                // After 20 seconds, check if players are in the elevator if not then stop the message helper.
                if (counter >= 20) {
                    if (players.isEmpty() && !elevatorObject.isGoing() && !elevatorObject.isIdling()) {
                        stop();
                        return;
                    }
                }

                for (Player player : elevatorObject.getPlayers()) {
                    // The player already got the message.
                    if (players.contains(player.getUniqueId())) {
                        return;
                    }

                    // Handle only two floors
                    FloorQueueObject floorQueueObject = getNextFloor(elevatorObject.getElevatorMovement().getFloor());
                    if (elevatorObject.getElevatorSettings().isOnlyTwoFloors() && floorQueueObject != null) {
                        elevatorObject.goToFloor(player, floorQueueObject.getFloorNumber(), floorQueueObject.getElevatorStatus(), ElevatorWho.FLOOR_SELECTOR_MANAGER);
                        players.add(player.getUniqueId());
                        return;
                    }

                    // Handle floor selector types
                    switch (elevatorObject.getElevatorSettings().getFloorSelectorType()) {
                        case CLICK_CHAT -> {
                            if (World16Elevators.getInstance().getOtherPlugins().hasFloodgate()) {
                                if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                    handleFloodgatePlayer(player);
                                    break;
                                }
                            }
                            handleClickChat(player);
                        }
                        case GUI -> {
                            if (World16Elevators.getInstance().getOtherPlugins().hasFloodgate()) {
                                if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                    handleFloodgatePlayer(player);
                                    break;
                                }
                            }
                            handleGUI(player);
                        }
                        case CHAT_RESPONSE -> {
                            ChatResponseManager chatResponseManager = plugin.getOtherPlugins().getWorld16Utils().getChatResponseManager();
                            player.sendMessage("Please type in the chat your response");
                            sendElevatorFloorsMessage(player);
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

    private void handleFloodgatePlayer(Player player) {
        SimpleForm.Builder simpleForm = SimpleForm.builder().title("Floors").content("List of floors!");

        for (Map.Entry<Integer, FloorObject> integerFloorObjectEntry : elevatorObject.getFloorsMap().entrySet()) {
            FloorObject floorObject = integerFloorObjectEntry.getValue();

            // Don't show the floor to people who don't have permission for that floor
            if (floorObject.getPermission() != null && !floorObject.getPermission().isEmpty()) {
                if (!player.hasPermission(floorObject.getPermission())) continue;
            }

            simpleForm.button(floorObject.getName());
        }

        simpleForm.validResultHandler((form, simpleFormResponse) -> {
            elevatorObject.goToFloor(player, simpleFormResponse.clickedButton().text(), ElevatorStatus.DONT_KNOW, ElevatorWho.FLOOR_SELECTOR_MANAGER);
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), simpleForm.build());
    }

    private void handleClickChat(Player player) {
        TextComponent.Builder componentBuilder = Component.text()
                .append(Translate.miniMessage("[<gold>BexarSystems <red>- <blue>Please click a floor in the chat to go to that floor.<reset>] "));

        int i = 0;
        for (Map.Entry<Integer, FloorObject> floorObjectEntry : this.elevatorObject.getFloorsMap().entrySet()) {
            FloorObject floorObject = floorObjectEntry.getValue();

            // Don't show the floor to people who don't have permission for that floor
            if (floorObject.getPermission() != null && !floorObject.getPermission().isEmpty()) {
                if (!player.hasPermission(floorObject.getPermission())) continue;
            }

            componentBuilder.resetStyle()
                    .append(Component.text(i == 0 ? floorObject.getName() : ", " + floorObject.getName())
                            .color(NamedTextColor.GOLD)
                            .decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.runCommand("/elevator call " + elevatorObject.getElevatorControllerName() + " " + elevatorObject.getElevatorName() + " " + floorObject.getName()))
                            .hoverEvent(HoverEvent.showText(Translate.miniMessage("<green> Click me to go to floor <#E10600>" + floorObject.getName() + "!"))));

            i++;
        }
        player.sendMessage(componentBuilder.build());
    }

    private void handleGUI(Player player) {
        new GUIWindow() {
            @Override
            public void onCreate(Player player) {
                List<AbstractGUIButton> guiButtons = new ArrayList<>();
                int guiSlots = elevatorObject.getFloorsMap().size() + (9 - (elevatorObject.getFloorsMap().size() % 9));

                int slot = 0;
                for (Map.Entry<Integer, FloorObject> entry : elevatorObject.getFloorsMap().entrySet()) {
                    FloorObject floorObject = entry.getValue();

                    // Don't show the floor to people who don't have permission for that floor
                    if (floorObject.getPermission() != null && !floorObject.getPermission().isEmpty()) {
                        if (!player.hasPermission(floorObject.getPermission())) continue;
                    }

                    guiButtons.add(new ClickEventButton(slot, InventoryUtils.createItem(Material.GREEN_CONCRETE, 1, floorObject.getName(), "Click to go to floor."), (guiClickEvent -> {
                        elevatorObject.goToFloor(player, floorObject.getFloor(), ElevatorStatus.DONT_KNOW, ElevatorWho.FLOOR_SELECTOR_MANAGER);
                        player.closeInventory();
                    })));
                    slot++;
                }
                this.update(guiButtons, "Elevator Floors!", guiSlots);
            }

            @Override
            public void onClose(InventoryCloseEvent inventoryCloseEvent) {

            }
        }.open(player);
    }

    private void sendElevatorFloorsMessage(Player player) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&2[Elevator Floors]&r");
        for (Map.Entry<Integer, FloorObject> entry : this.elevatorObject.getFloorsMap().entrySet()) {
            FloorObject floorObject = entry.getValue();

            // Don't show the floor to people who don't have permission for that floor
            if (floorObject.getPermission() != null && !floorObject.getPermission().isEmpty()) {
                if (!player.hasPermission(floorObject.getPermission())) continue;
            }

            stringBuilder.append("&e, &a").append(floorObject.getName());
        }
        player.sendMessage(Translate.color(stringBuilder.toString()));
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
