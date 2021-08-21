package scripts.core.equipment;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Game;
import org.tribot.api2007.Magic;
import org.tribot.api2007.types.RSItem;
import org.tribot.script.Script;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.inventory.InventoryEvent;
import scripts.core.requistion.RequisitionItem;
import scripts.core.utilities.Methods;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;


public class LoadoutEvent extends BotEvent {
    BankEventV2 bankEventV2;
    String finalItem = null;

    public LoadoutEvent(Script script, BankEventV2 bankEvent) {
        super(script);
        this.bankEventV2 = bankEvent;
    }

    public static RSItem getEquipmentItem(String... name) {
        RSItem[] items = Equipment.find(name);
        if (items != null) {
            for (RSItem item : items) {
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    public static RSItem getEquipmentPartName(String partName) {
        RSItem[] items = Equipment.find(i -> i.getDefinition().getName().contains(partName));
        for (RSItem item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public static RSItem getEquipmentID(int id) {
        RSItem[] items = Equipment.find(i -> i.getDefinition().getID() == id);
        for (RSItem item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public static boolean isWearing(String... string) {
        return Equipment.isEquipped(string);
    }

    public static boolean isWearing(String string) {
        if (string.contains("~")) {
            List<String> expandedItem = BankEventV2.expandItemName(string);
            if (expandedItem.size() > 0) {
                for (String item : expandedItem) {
                    if (Equipment.isEquipped(item)) {
                        return true;
                    }
                }
            }
        } else {
            return Equipment.isEquipped(string);
        }
        return false;
    }

    public static boolean isWearingPartName(String partName) {
        RSItem[] items = Equipment.find(i -> i.getDefinition().getName().contains(partName));
        for (RSItem item : items) {
            if (item != null) {
                return item != null;
            }
        }
        return false;
    }

    public boolean isPendingOperation() {
        for (Map.Entry<String, RequisitionItem> withdrawList : bankEventV2.withdrawList.entrySet()) {
            String itemName = withdrawList.getKey();
            finalItem = "";
            Supplier<Boolean> cond = withdrawList.getValue().getCondition();
            boolean noted = withdrawList.getValue().getNoted();
            if (cond.get()) {
                if (itemName.contains("~")) {
                    List<String> expandedItem = BankEventV2.expandItemName(itemName);
                    if (expandedItem.size() > 0) {
                        for (String item : expandedItem) {
                            if (Equipment.isEquipped(item)) {
                                finalItem = itemName;
                                return false;
                            }
                        }
                    }
                } else {
                    finalItem = itemName;
                }
                RSItem item = LoadoutEvent.getEquipmentItem(finalItem);
                if (finalItem == "") {
                    General.println("Skipping final Item is: empty");
                } else {
                    return !LoadoutEvent.isWearing(finalItem);
                }
            }
        }
        return false;
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (!isPendingOperation()) {
            General.println("LoadoutEvent is complete");
            setComplete();
        } else {
            for (Entry<String, RequisitionItem> withdrawList : bankEventV2.withdrawList.entrySet()) {
                String itemName = withdrawList.getKey();
                Supplier<Boolean> cond = withdrawList.getValue().getCondition();
                finalItem = "";
                if (cond.get()) {
                    if (itemName.contains("~")) {
                        List<String> expandedItem = BankEventV2.expandItemName(itemName);
                        if (expandedItem.size() > 0) {
                            for (String item : expandedItem) {
                                if (InventoryEvent.contains(item)) {
                                    finalItem = item;
                                    break;
                                }
                            }
                        }
                    } else {
                        finalItem = itemName;
                    }
                    if(isWearing(finalItem)) {
                    	continue;
					}else
                    if (Magic.isSpellSelected()) {
                        Mouse.click(1);
                    } else if (InventoryEvent.contains(finalItem)) {
                        General.println("LoadoutEvent: Inventory contains: " + finalItem);
                        if (Banking.isBankScreenOpen()) {
                            Banking.close();
                            Timing.waitCondition(() -> !Banking.isBankScreenOpen(), 5000);
                        } else {
                            RSItem item = Methods.getInventoryItem(finalItem);
                            if (InventoryEvent.contains("Vial")) {
                                General.println("Dropping vial");
                                RSItem vial = Methods.getInventoryItem("Vial");
                                if (Game.getItemSelectionState() == 0) {
                                    if (vial.click("Drop")) {
                                        Timing.waitCondition(() -> !InventoryEvent.contains("Vial"), 2000);
                                    }
                                } else {
                                    General.println("Deselecting vial LoadoutEvent");
                                    Clicking.click(vial);
                                    Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 1000);
                                }
                            } else if (Game.getItemSelectionState() == 0) {
                                General.println("ItemSelection State: 0");
                                if (item != null) {
                                    General.println("Loadout: Equiping item.. " + item.getDefinition().getName());
                                    if (item.click("Wear", "Wield", "Weild", "Equip")) {
                                        Timing.waitCondition(() -> Equipment.isEquipped(item.getDefinition().getName()),
                                                500);
                                    }
                                }
                            } else {
                                General.println("Trying to deselect item LoadoutEvent");
                                Clicking.click(item);
                                Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 1000);
                            }
                        }
                    } else if (bankEventV2.isPendingOperation()) {
                        bankEventV2.execute();
                        bankEventV2.reset();
                    }
                }
            }
        }
    }

}
