package scripts.core.banking;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Objects;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.script.Script;
import scripts.core.botevent.BotEvent;
import scripts.core.inventory.InventoryEvent;
import scripts.core.requistion.RequisitionItem;
import scripts.dax_api.api_lib.DaxWalker;

import java.io.IOException;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.tribot.api2007.Inventory.getAll;

public class BankEventV2 extends BotEvent {

    public  HashMap<String, BankCache> bankCacheHashMap = new HashMap<>();

    public LinkedHashMap<String, RequisitionItem> withdrawList = new LinkedHashMap<>();

    public BankEventV2(Script script) {
        super(script);
    }

    String finalItem;

    @Override
    public void step() throws InterruptedException, IOException {
        if(GrandExchange.getWindowState() != null) {
            GrandExchange.close();
            Timing.waitCondition( () -> GrandExchange.getWindowState() == null, 4000);
        } else
        if (!Banking.isBankScreenOpen()) {
            RSObject[] bank = Objects.find(20, "Bank booth");
            RSNPC[] banker = NPCs.find("Banker");
            RSObject[] chest = Objects.find(20, "Bank chest", "Open chest");
            if (bank.length < 1 && banker.length < 1 && chest.length < 1) {
                General.println("BankEvent: Walking to closest bank");
                DaxWalker.walkToBank();
                Timing.waitCondition( () -> Banking.isInBank() || !Player.getRSPlayer().isMoving(), 20000);
            } else if (Banking.openBank()) {
                General.println("BankEvent opening bank");
                Timing.waitCondition(Banking::isBankLoaded, 2000);
                updateCache();
            }
            return;
        }

        if (!Banking.isBankLoaded()) {
            return;
        }

        if (!InventoryEvent.containsOnly(arryOfItemsToWithdraw())) {
            General.println("Banking unreqired items");
            Banking.depositAllExcept(arryOfItemsToWithdraw());
            Timing.waitCondition( () -> InventoryEvent.containsOnly(arryOfItemsToWithdraw()), 6000);
        }
        for (Map.Entry<String, RequisitionItem> withdrawList : withdrawList.entrySet()) {
            RequisitionItem reqItem = withdrawList.getValue();
            String itemName = reqItem.getName();
            int amount = reqItem.getQty();
            Supplier<Boolean> itemCondition = reqItem.getCondition();
            boolean noted = reqItem.getNoted();
            finalItem = "";

            if (itemName.contains("~")) {
                if (finalItem.equals("")) {
                    List<String> expandedItem = expandItemName(itemName);
                    for (String item : expandedItem) {
                        if (contains(item)) {
                            finalItem = item;
                            break;
                        }
                    }
                }
            } else {
                finalItem = itemName;
            }
            if(finalItem.equals("")) {
                continue;
            }
            /*if (!itemCondition.get()) {
                General.println("BankEvent: Banking unrequired items part2");
                General.println("Checking item: " + itemName + " amount: " + amount + " noted: " + noted); //You're not checking finalItem we're checking the itemName
                Banking.deposit(Integer.MAX_VALUE, finalItem);
                continue;
            }*/
            RSItem finalRsItem = InventoryEvent.getInventoryItem(finalItem);
            RSItemDefinition finalItemDefinition = null;
            if(finalRsItem != null) {
                finalItemDefinition = finalRsItem.getDefinition();
            }
            General.println("Checking item: " + finalItem + " amount: " + amount + " noted: " + noted);
            if(!itemCondition.get()) {
                if(InventoryEvent.contains(finalItem)) {
                    Banking.deposit(Integer.MAX_VALUE, finalItem);
                    Timing.waitCondition( () -> !InventoryEvent.contains(finalItem), 3000);
                }
                continue;
            }

            if (InventoryEvent.contains(finalRsItem) && finalItemDefinition != null && !finalItemDefinition.isNoted() && noted) {
                General.println("Depositing item: "+finalItem+" we need noted");
                Banking.deposit(InventoryEvent.getCount(itemName), itemName);
                Timing.waitCondition(() -> !InventoryEvent.contains(finalRsItem), 2000);
            } else if (!bankCacheHashMap.containsKey(finalItem) || !contains(finalItem)) {
                General.println("Stopping we dont have item '"+finalItem+"' in bank");
                updateCache();
                setComplete();
            } else if (!InventoryEvent.contains(finalItem)) {
                if(!itemCondition.get()) {
                    General.println("Skipping do to not needing item");
                    continue;
                }
                boolean setStatus = setWithdrawNoted(noted);
                if (!setStatus) {
                    continue;
                } else if (noted) {
                    General.println("Withdrawing noted: " + finalItem);
                }
                if (Banking.withdraw(amount, finalItem)) {
                    Timing.waitCondition(() -> InventoryEvent.contains(finalItem), 2000);
                }
            } else if (InventoryEvent.contains(finalItem) && finalRsItem != null) {
                boolean isStackable = finalItemDefinition != null && finalItemDefinition.isStackable();
                int itemCount = isStackable ? finalRsItem.getStack() : InventoryEvent.getCount(finalItem);
                boolean shouldWithdraw = itemCount < amount;
                boolean setWithdrawStatus = setWithdrawNoted(noted);
                BooleanSupplier bankWaitCondition = isStackable ? () -> InventoryEvent.getStackedCount(finalItem) == amount : () -> InventoryEvent.getCount(finalItem) == amount;

                if (shouldWithdraw && Banking.withdraw(amount - itemCount, finalItem)) {
                    Timing.waitCondition(bankWaitCondition, 2000);
                }
            }
        }
        General.println("Trying to update cache");
        updateCache();
        Banking.close();
        setComplete();
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

    public BankEventV2 addReq(String itemName, int amount) {
        setWithdrawList(itemName, amount, false, () -> true);
        return this;
    }

    public BankEventV2 addReq(String itemName, int amount, Supplier<Boolean> condition) {
        setWithdrawList(itemName, amount, false, condition);
        return this;
    }

    public BankEventV2 addReq(String itemName, int amount, boolean noted) {
        setWithdrawList(itemName, amount, noted, () -> true);
        return this;
    }

    public BankEventV2 addReq(String itemName, int amount, boolean noted, Supplier<Boolean> condition) {
        setWithdrawList(itemName, amount, noted, condition);
        return this;
    }

    public static boolean setWithdrawNoted(boolean enable) {
        return enable ? setNoted() : setUnNoted();
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
                            }
                        }
                    }
                } else {
                    finalItem = itemName;
                }
                if(finalItem.equals("")) {
                    continue;
                }
                RSItem item = InventoryEvent.getInventoryItem(finalItem);
                if (noted) {
                    if (item != null) {
                        if (!item.getDefinition().isNoted()) {
                            General.println("We need noted: " + finalItem);
                            return true;
                        }
                    } else {
                        return true;
                    }
                } else if (!InventoryEvent.contains(finalItem)) {
                    General.println("BankEvent is Pending Operation: We dont have: " + finalItem);
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

    public boolean contains(String itemName) {
        return Banking.find(itemName).length >=1;
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

    public void openBank() {
        if (!Banking.isBankScreenOpen()) {
            RSObject[] bank = Objects.find(20, "Bank booth");
            RSNPC[] banker = NPCs.find("Banker");
            RSObject[] chest = Objects.find(20, "Bank chest", "Open chest");
            if (bank.length < 1 && banker.length < 1 && chest.length < 1) {
                General.println("BankEvent: Walking to closest bank");
                DaxWalker.walkToBank();
                Timing.waitCondition(() -> Banking.isInBank() || !Player.getRSPlayer().isMoving(), 20000);
            } else if (Banking.openBank()) {
                General.println("BankEvent opening bank");
                Timing.waitCondition(Banking::isBankScreenOpen, 2000);
            }
        } else {
            depositAll();
            General.sleep(1000);
            updateCache();
        }
    }

    public void depositAll() {
        if(Banking.isBankScreenOpen()) {
            if(getAll().length > 0) {
                Banking.depositAll();
            }
        }
    }

    public boolean needCache() {
        return bankCacheHashMap.isEmpty();
    }

    public void updateItem(String itemName, int id, int amount) {
        if(bankCacheHashMap.containsKey(itemName)) {
            bankCacheHashMap.replace(itemName, new BankCache(itemName, id, amount));
        } else {
            bankCacheHashMap.put(itemName, new BankCache(itemName, id, amount));
        }
    }

    public void updateCache() {
        if(!Banking.isBankScreenOpen() && Banking.isBankLoaded()) {
            General.println("Bank is not open cannot continue");
            return;
        } else {
            bankCacheHashMap = new HashMap<>();
            RSItem[] bankCache = Banking.getAll();
            for(int i = 0; bankCache.length > i;i++) {
                General.println("Updating cache: "+bankCache[i].getDefinition().getName() +" ID: "+ bankCache[i].getDefinition().getID()+ " qty: "+bankCache[i].getStack());
                updateItem(bankCache[i].getDefinition().getName(), bankCache[i].getDefinition().getID(), bankCache[i].getStack());
            }
            RSItem[] equipment = Equipment.getItems();
            for(int i=0; equipment.length> i;i++) {
                General.println("Updating cache: "+equipment[i].getDefinition().getName() +" ID: "+ equipment[i].getDefinition().getID()+ " qty: "+equipment[i].getStack());
                updateItem(equipment[i].getDefinition().getName(), equipment[i].getDefinition().getID(), equipment[i].getStack());
            }
        }
    }
}
