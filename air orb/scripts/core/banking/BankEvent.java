package scripts.core.banking;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.script.Script;
import scripts.core.botevent.BotEvent;
import scripts.core.inventory.InventoryEvent;
import scripts.core.requistion.RequisitionItem;
import scripts.dax_api.api_lib.DaxWalker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BankEvent extends BotEvent {

    public LinkedHashMap<String, RequisitionItem> withdrawList = new LinkedHashMap<>();
    BankEvent bankEvent;
    String finalItem;

    public BankEvent(Script script) {
        super(script);
    }

    public BankEvent(Script script, BankEvent bankEvent) {
        super(script);
        this.bankEvent = bankEvent;
    }

    public static boolean setNoted() {
        if (Game.getSetting(115) != 1) {
            Interfaces.get(12, 23).click("Note");
        }
        return Game.getSetting(115) == 1;
    }

    public static boolean setUnNoted() {
        if (Game.getSetting(115) == 1) {
            Interfaces.get(12, 21).click("Item");
        }
        return Game.getSetting(115) != 1;
    }

    public static boolean contains(String itemName) {
        return Banking.find(itemName).length > 0;
    }

    public static List<String> expandItemName(String name) {
        ArrayList<String> names = new ArrayList<>();
        Pattern pattern = Pattern.compile("^(.*?)([0-9]+)~([0-9]+)(.*?)$");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            String prepend = matcher.group(1), append = matcher.group(4);
            int start = Integer.parseInt(matcher.group(2)), finish = Integer.parseInt(matcher.group(3)),
                    dir = start > finish ? -1 : 1;
            for (int i = start; i * dir <= finish * dir; i += dir) {
                names.add(prepend + i + append);
            }
        } else {
            pattern = Pattern.compile("^(.*?)\\{(.*?)}(.*?)$");
            matcher = pattern.matcher(name);
            if (matcher.find()) {
                String prepend = matcher.group(1), append = matcher.group(3);
                String[] tings = matcher.group(2).split(";");
                for (String t : tings) {
                    names.add((prepend + t + append).trim());
                }
            } else {
                names.add(name);
            }
        }
        return names;
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (!Banking.isBankScreenOpen()) {
            RSObject[] bank = Objects.find(20, "Bank booth");
            RSNPC[] banker = NPCs.find("Banker");
            RSObject[] chest = Objects.find(20, "Bank chest");
            if (bank.length > 0 || banker.length > 0 || chest.length > 0) {
                if (Banking.openBank()) {
                    General.println("BankEvent opening bank");
                    Timing.waitCondition(Banking::isBankScreenOpen, 2000);
                }
            } else {
                General.println("BankEvent: Walking to closest bank");
                DaxWalker.walkToBank();
                Timing.waitCondition(() -> Banking.isInBank() || !Player.getRSPlayer().isMoving() || (bank.length > 0 || banker.length > 0 || chest.length > 0), 20000);
            }
        } else {
            if (Banking.isBankLoaded()) {
                if (!InventoryEvent.containsOnly(arryOfItemsToWithdraw())) {
                    General.println("Banking unreqired items");
                    Banking.depositAllExcept(arryOfItemsToWithdraw());
                }
                for (Map.Entry<String, RequisitionItem> withdrawList : withdrawList.entrySet()) {
                    RequisitionItem reqItem = withdrawList.getValue();
                    String itemName = reqItem.getName();
                    int amount = reqItem.getQty();
                    Supplier<Boolean> cond = reqItem.getCondition();
                    boolean noted = reqItem.getNoted();
                    finalItem = "";
                    if (cond.get()) {
                        if (itemName.contains("~")) {
                            List<String> expandedItem = expandItemName(itemName);
                            if (expandedItem.size() > 0 && finalItem.equals("")) {
                                for (String item : expandedItem) {
                                    if (contains(item) && finalItem.equals("")) {
                                        finalItem = item;
                                        break;
                                    }
                                }
                            }
                        } else {
                            finalItem = itemName;
                        }
                        RSItem wItem = InventoryEvent.getInventoryItem(finalItem);
                        General.println("Checking item: " + finalItem + " amount: " + amount + " noted: " + noted);
                        if (InventoryEvent.contains(wItem) && !wItem.getDefinition().isNoted() && noted) {
                            Banking.deposit(InventoryEvent.getCount(itemName), itemName);
                            Timing.waitCondition(() -> !InventoryEvent.contains(wItem), 2000);
                        } else if (!contains(finalItem) && InventoryEvent.getInventoryItem(finalItem) == null) {
                            General.println("Stopping we dont have item '" + finalItem + "' in bank");
                            script.stopScript();
                        } else if (!InventoryEvent.contains(finalItem)) {
                            if (noted) {
                                General.println("Withdrawing noted: " + finalItem);
                                if (setNoted()) {
                                    if (Banking.withdraw(amount, finalItem)) {
                                        Timing.waitCondition(() -> InventoryEvent.contains(finalItem), 2000);
                                    }
                                }
                            } else if (setUnNoted()) {
                                if (Banking.withdraw(amount, finalItem)) {
                                    Timing.waitCondition(() -> InventoryEvent.contains(finalItem), 2000);
                                }
                            }
                        } else if (InventoryEvent.contains(finalItem)) {
                            if (wItem != null) {
                                if (noted) {
                                    if (setNoted()) {
                                        if (wItem.getDefinition().isStackable()) {
                                            if (wItem.getStack() < amount) {
                                                if (Banking.withdraw(amount - wItem.getStack(), finalItem)) {
                                                    Timing.waitCondition(
                                                            () -> InventoryEvent.getStackedCount(finalItem) == amount,
                                                            2000);

                                                }
                                            } else if (wItem.getStack() > amount) {
                                                if (Banking.deposit(wItem.getStack() - amount, finalItem)) {
                                                    Timing.waitCondition(
                                                            () -> InventoryEvent.getStackedCount(finalItem) == amount,
                                                            2000);

                                                }
                                            }
                                        } else {
                                            if (InventoryEvent.getCount(finalItem) < amount) {
                                                if (Banking.withdraw(amount - InventoryEvent.getCount(finalItem),
                                                        finalItem)) {
                                                    Timing.waitCondition(() -> InventoryEvent.getCount(finalItem) == amount,
                                                            2000);

                                                }
                                            } else if (InventoryEvent.getCount(finalItem) > amount) {
                                                if (Banking.deposit(InventoryEvent.getCount(finalItem) - amount,
                                                        finalItem)) {
                                                    Timing.waitCondition(() -> InventoryEvent.getCount(finalItem) == amount,
                                                            2000);

                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (setUnNoted()) {
                                        if (wItem.getDefinition().isStackable()) {
                                            if (wItem.getStack() < amount) {
                                                if (Banking.withdraw(amount - wItem.getStack(), finalItem)) {
                                                    Timing.waitCondition(
                                                            () -> InventoryEvent.getStackedCount(finalItem) == amount,
                                                            2000);

                                                }
                                            } else if (wItem.getStack() > amount) {
                                                if (Banking.deposit(wItem.getStack() - amount, finalItem)) {
                                                    Timing.waitCondition(
                                                            () -> InventoryEvent.getStackedCount(finalItem) == amount,
                                                            2000);

                                                }
                                            }
                                        } else {
                                            if (InventoryEvent.getCount(finalItem) < amount) {
                                                if (Banking.withdraw(amount - InventoryEvent.getCount(finalItem),
                                                        finalItem)) {
                                                    Timing.waitCondition(() -> InventoryEvent.getCount(finalItem) == amount,
                                                            2000);

                                                }
                                            } else if (InventoryEvent.getCount(finalItem) > amount) {
                                                if (Banking.deposit(InventoryEvent.getCount(finalItem) - amount,
                                                        finalItem)) {
                                                    Timing.waitCondition(() -> InventoryEvent.getCount(finalItem) == amount,
                                                            2000);

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        General.println("BankEvent: Banking unrequired items part2");
                        General.println("Checking item: " + finalItem + " amount: " + amount + " noted: " + noted);
                        Banking.deposit(Integer.MAX_VALUE, itemName);
                    }
                }
                setComplete();
            }
        }
    }

    public void setWithdrawList(String itemName, int amount, boolean noted, Supplier<Boolean> condition) {
        if (withdrawList.containsKey(itemName)) {
            if (withdrawList.get(itemName).getQty() != amount) {
                withdrawList.replace(itemName, new RequisitionItem(itemName, amount, noted, condition));
            }
        } else {
            withdrawList.put(itemName, new RequisitionItem(itemName, amount, noted, condition));
        }
    }

    public BankEvent addReq(String itemName, int amount) {
        setWithdrawList(itemName, amount, false, () -> true);
        return this;
    }

    public BankEvent addReq(String itemName, int amount, Supplier<Boolean> condition) {
        setWithdrawList(itemName, amount, false, condition);
        return this;
    }

    public BankEvent addReq(String itemName, int amount, boolean noted) {
        setWithdrawList(itemName, amount, noted, () -> true);
        return this;
    }

    public BankEvent addReq(String itemName, int amount, boolean noted, Supplier<Boolean> condition) {
        setWithdrawList(itemName, amount, noted, condition);
        return this;
    }

    public String[] arryOfItemsToWithdraw() {
        General.println(withdrawList.keySet());
        return withdrawList.keySet().toArray(new String[0]);
    }

    public boolean isPendingOperation() {
        for (Map.Entry<String, RequisitionItem> withdrawList : withdrawList.entrySet()) {
            String itemName = withdrawList.getKey();
            finalItem = "";
            Supplier<Boolean> cond = withdrawList.getValue().getCondition();
            boolean noted = withdrawList.getValue().getNoted();
            if (cond.get()) {
                if (itemName.contains("~")) {
                    List<String> expandedItem = expandItemName(itemName);
                    if (expandedItem.size() > 0) {
                        for (String item : expandedItem) {
                            if (InventoryEvent.contains(item)) {
                                finalItem = item;
                                break;
                            } else {
                                finalItem = item;
                            }
                        }
                    }
                } else {
                    finalItem = itemName;
                }
                RSItem item = InventoryEvent.getInventoryItem(finalItem);
                if (noted) {
                    if (item != null) {
                        if (!InventoryEvent.contains(item)) {
                            General.println("We dont have item: " + finalItem);
                            return true;
                        } else if (!item.getDefinition().isNoted()) {
                            General.println("We need noted: " + finalItem);
                            return true;
                        }
                    } else {
                        return true;
                    }

                } else if (!InventoryEvent.contains(item)) {
                    General.println("We dont have: " + finalItem);
                    return true;
                }
            }
        }
        return false;
    }

    public RSItem getBankItem(String... itemNames) {
        RSItem[] items = Banking.find(itemNames);
        for (RSItem item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public int getCount(String... itemNames) {
        int amount = 0;
        if (getBankItem(itemNames) != null) {
            amount = getBankItem(itemNames).getStack();
        }
        return amount;
    }

    public void bankEquipment() {
        if (!Banking.isBankScreenOpen()) {
            if (!Banking.isInBank()) {
                DaxWalker.walkToBank();
            } else if (Banking.openBank()) {
                Timing.waitCondition(Banking::isBankScreenOpen, 2000);
            }
        } else {
            if (Banking.depositEquipment()) {
                Timing.waitCondition(() -> Equipment.getItems().length == 0, 2000);
            }
        }
    }
}
