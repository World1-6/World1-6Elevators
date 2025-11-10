package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16elevators.enums.ElevatorWho;
import com.andrew121410.mc.world16utils.chat.ChatResponseManager;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.gui.GUIWindow;
import com.andrew121410.mc.world16utils.gui.buttons.AbstractGUIButton;
import com.andrew121410.mc.world16utils.gui.buttons.defaults.ClickEventButton;
import com.andrew121410.mc.world16utils.utils.InventoryUtils;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
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
    private final Elevator elevator;

    private boolean isRunning;
    private List<UUID> players;
    private int counter;

    public ElevatorFloorSelectorManager(World16Elevators plugin, Elevator elevator) {
        this.plugin = plugin;
        this.elevator = elevator;
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
                    ArrayList<UUID> uuidArrayList = (ArrayList<UUID>) elevator.getPlayersUUIDs();
                    if (!uuidArrayList.contains(uuid)) iterator.remove();
                }

                // After 20 seconds, check if players are in the elevator if not then stop the message helper.
                if (counter >= 20) {
                    if (players.isEmpty() && !elevator.isGoing() && !elevator.isIdling()) {
                        stop();
                        return;
                    }
                }

                for (Player player : elevator.getPlayers()) {
                    // The player already got the message.
                    if (players.contains(player.getUniqueId())) {
                        return;
                    }

                    // Handle only two floors
                    FloorQueueObject floorQueueObject = getNextFloor(elevator.getElevatorMovement().getFloor());
                    if (elevator.getElevatorSettings().isOnlyTwoFloors() && floorQueueObject != null) {
                        elevator.goToFloor(player, floorQueueObject.getFloorNumber(), floorQueueObject.getElevatorStatus(), ElevatorWho.FLOOR_SELECTOR_MANAGER);
                        players.add(player.getUniqueId());
                        return;
                    }

                    // Mark player as having received the message
                    players.add(player.getUniqueId());

                    // Wait ticks before showing floor selector
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Check if player is still in the elevator
                            if (!elevator.getPlayersUUIDs().contains(player.getUniqueId())) {
                                // Player left, remove from tracking list
                                players.remove(player.getUniqueId());
                                return;
                            }

                            // Handle floor selector types
                            switch (elevator.getElevatorSettings().getFloorSelectorType()) {
                                case CLICK_CHAT -> {
                                    // We have to handle bedrock players differently
                                    if (World16Elevators.getInstance().getOtherPlugins().hasFloodgate()) {
                                        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                            handleFloodgatePlayer(player);
                                            break;
                                        }
                                    }
                                    handleClickChat(player);
                                }
                                case GUI -> {
                                    // We have to handle bedrock players differently
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
                                        plugin.getServer().dispatchCommand(player, "elevator call " + elevator.getElevatorControllerName() + " " + elevator.getElevatorName() + " " + response);
                                    });
                                }

                                case DIALOGUE -> {
                                    // We have to handle bedrock players differently
                                    if (World16Elevators.getInstance().getOtherPlugins().hasFloodgate()) {
                                        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                            handleFloodgatePlayer(player);
                                            break;
                                        }
                                    }

                                    // Create Action Buttons for each floor
                                    List<ActionButton> actionButtons = new ArrayList<>();
                                    List<ElevatorFloor> accessibleFloors = elevator.getFloors(player);
                                    for (ElevatorFloor elevatorFloor : accessibleFloors) {
                                        Component label = Translate.miniMessage(elevatorFloor.getName());
                                        int width = 100;
                                        // ActionButton.create expects a DialogAction, but lambda is not allowed. Use null for now.
                                        DialogAction dialogAction = DialogAction.customClick((response, audience) -> {
                                            elevator.goToFloor(player, elevatorFloor.getFloor(), ElevatorStatus.DONT_KNOW, ElevatorWho.FLOOR_SELECTOR_MANAGER);
                                        }, ClickCallback.Options.builder()
                                                .uses(1)
                                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                                .build());

                                        actionButtons.add(ActionButton.create(label, null, width, dialogAction));
                                    }

                                    Dialog dialog = Dialog.create(builder -> builder.empty()
                                            .base(DialogBase.builder(Component.text("Please Select Floor:")).build())
                                            .type(DialogType.multiAction(actionButtons).build())
                                    );
                                    player.showDialog(dialog);
                                }
                            }
                        }
                    }.runTaskLater(plugin, 10L);
                }
            }
        }.runTaskTimer(plugin, 1L, 20L);
    }

    private void handleFloodgatePlayer(Player player) {
        SimpleForm.Builder simpleForm = SimpleForm.builder().title("Floors").content("List of floors!");

        for (Map.Entry<Integer, ElevatorFloor> integerFloorObjectEntry : elevator.getFloorsMap().entrySet()) {
            ElevatorFloor elevatorFloor = integerFloorObjectEntry.getValue();

            // Don't show the floor to people who don't have permission for that floor
            if (elevatorFloor.getPermission() != null && !elevatorFloor.getPermission().isEmpty()) {
                if (!player.hasPermission(elevatorFloor.getPermission())) continue;
            }

            simpleForm.button(elevatorFloor.getName());
        }

        simpleForm.validResultHandler((form, simpleFormResponse) -> {
            elevator.goToFloor(player, simpleFormResponse.clickedButton().text(), ElevatorStatus.DONT_KNOW, ElevatorWho.FLOOR_SELECTOR_MANAGER);
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), simpleForm.build());
    }

    private void handleClickChat(Player player) {
        TextComponent.Builder componentBuilder = Component.text()
                .append(Translate.miniMessage("[<gold>BexarSystems <red>- <blue>Please click a floor in the chat to go to that floor.<reset>] "));

        int i = 0;
        for (Map.Entry<Integer, ElevatorFloor> floorObjectEntry : this.elevator.getFloorsMap().entrySet()) {
            ElevatorFloor elevatorFloor = floorObjectEntry.getValue();

            // Don't show the floor to people who don't have permission for that floor
            if (elevatorFloor.getPermission() != null && !elevatorFloor.getPermission().isEmpty()) {
                if (!player.hasPermission(elevatorFloor.getPermission())) continue;
            }

            componentBuilder.resetStyle()
                    .append(Component.text(i == 0 ? elevatorFloor.getName() : ", " + elevatorFloor.getName())
                            .color(NamedTextColor.GOLD)
                            .decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.runCommand("/elevator call " + elevator.getElevatorControllerName() + " " + elevator.getElevatorName() + " " + elevatorFloor.getName()))
                            .hoverEvent(HoverEvent.showText(Translate.miniMessage("<green> Click me to go to floor <#E10600>" + elevatorFloor.getName() + "!"))));

            i++;
        }
        player.sendMessage(componentBuilder.build());
    }

    private void handleGUI(Player player) {
        new GUIWindow() {
            @Override
            public void onCreate(Player player) {
                List<AbstractGUIButton> guiButtons = new ArrayList<>();
                int guiSlots = elevator.getFloorsMap().size() + (9 - (elevator.getFloorsMap().size() % 9));

                int slot = 0;
                for (Map.Entry<Integer, ElevatorFloor> entry : elevator.getFloorsMap().entrySet()) {
                    ElevatorFloor elevatorFloor = entry.getValue();

                    // Don't show the floor to people who don't have permission for that floor
                    if (elevatorFloor.getPermission() != null && !elevatorFloor.getPermission().isEmpty()) {
                        if (!player.hasPermission(elevatorFloor.getPermission())) continue;
                    }

                    guiButtons.add(new ClickEventButton(slot, InventoryUtils.createItem(Material.GREEN_CONCRETE, 1, elevatorFloor.getName(), "Click to go to floor."), (guiClickEvent -> {
                        elevator.goToFloor(player, elevatorFloor.getFloor(), ElevatorStatus.DONT_KNOW, ElevatorWho.FLOOR_SELECTOR_MANAGER);
                        player.closeInventory();
                    })));
                    slot++;
                }
                this.update(guiButtons, Component.text("Elevator Floors!"), guiSlots);
            }

            @Override
            public void onClose(InventoryCloseEvent inventoryCloseEvent) {

            }
        }.open(player);
    }

    private void sendElevatorFloorsMessage(Player player) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&2[Elevator Floors]&r");
        for (Map.Entry<Integer, ElevatorFloor> entry : this.elevator.getFloorsMap().entrySet()) {
            ElevatorFloor elevatorFloor = entry.getValue();

            // Don't show the floor to people who don't have permission for that floor
            if (elevatorFloor.getPermission() != null && !elevatorFloor.getPermission().isEmpty()) {
                if (!player.hasPermission(elevatorFloor.getPermission())) continue;
            }

            stringBuilder.append("&e, &a").append(elevatorFloor.getName());
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

    public FloorQueueObject getNextFloor(Integer floorNumber) {
        if (floorNumber == null) return null;

        ElevatorFloor elevatorFloor = null;
        ElevatorStatus elevatorStatus = null;
        if (floorNumber == 1) {
            elevatorFloor = elevator.getFloor(2);
            if (elevatorFloor == null) {
                elevatorFloor = elevator.getFloor(-1);
                elevatorStatus = ElevatorStatus.UP;
            } else {
                elevatorStatus = ElevatorStatus.DOWN;
            }
        } else if (floorNumber == 2) {
            elevatorFloor = elevator.getFloor(1);
            elevatorStatus = ElevatorStatus.UP;
        } else if (floorNumber == -1) {
            elevatorFloor = elevator.getFloor(1);
            elevatorStatus = ElevatorStatus.DOWN;
        }
        if (elevatorFloor == null) return null;
        return new FloorQueueObject(elevatorFloor.getFloor(), elevatorStatus);
    }

    public boolean isRunning() {
        return isRunning;
    }
}
