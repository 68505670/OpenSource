package scripts.core.grandexchange;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import scripts.core.banking.BankCache;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.inventory.InventoryEvent;
import scripts.core.utilities.Methods;
import scripts.core.utilities.Walker;
import scripts.dax_api.walker.utils.AccurateMouse;

import java.io.IOException;
import java.util.Objects;
import java.util.*;

public class GEEvent extends BotEvent {

    public HashMap<String, GrandExchangeItem> grandExchangeItemHashMap = new HashMap<>();
    public HashMap<String, GrandExchangeItem> grandExchangeSellHashMap = new HashMap<>();
    public int itemChild;
    BankEventV2 bankEvent;
    RSArea grandExchangeArea = new RSArea(
            new RSTile[]{
                    new RSTile(3157, 3494, 0),
                    new RSTile(3157, 3487, 0),
                    new RSTile(3162, 3483, 0),
                    new RSTile(3169, 3483, 0),
                    new RSTile(3173, 3487, 0),
                    new RSTile(3173, 3494, 0),
                    new RSTile(3168, 3498, 0),
                    new RSTile(3161, 3498, 0)
            }
    );
    ExchangeBoxes boxes;

    public GEEvent(Script script, BankEventV2 bankEvent) {
        super(script);
        this.bankEvent = bankEvent;
        bankEventV2= new BankEventV2(script).addReq("Coins", Integer.MAX_VALUE);

    }

    public GEEvent addReq(String itemName, int id, int qty, int price) {
        setGrandExchangeItemIDList(itemName, id, qty, price);
        return this;
    }

    public GEEvent addReq(String itemName, int qty, int price) {
        setGrandExchangeItemList(itemName, qty, price);
        return this;
    }

    public GEEvent sellReq(String itemName, int price) {
        setGrandExchangeSellList(itemName, price);
        General.println("Selling item: "+itemName +" pride: "+price);
        return this;
    }

    public void setGrandExchangeSellList(String itemName, int price) {
        grandExchangeSellHashMap.put(itemName, new GrandExchangeItem(itemName, price));
    }

    public boolean isPendingOperation() {
        for (Map.Entry<String, GrandExchangeItem> grandExchangeList : grandExchangeItemHashMap.entrySet()) {
            String itemName = grandExchangeList.getKey();
            if (!bankEvent.bankCacheHashMap.containsKey(itemName)) {
                General.println("Grand Exchange: We dont have: "+itemName);
                return true;
            }
        }
        return false;
    }

    public void setGrandExchangeItemIDList(String itemName, int id, int qty, int price) {
        grandExchangeItemHashMap.put(itemName, new GrandExchangeItem(itemName, id, qty, price));
    }

    public void setGrandExchangeItemList(String itemName, int qty, int price) {
        grandExchangeItemHashMap.put(itemName, new GrandExchangeItem(itemName, qty, price));
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (!isPendingOperation()) {
            if (!canOpenExchange()) {
                closeGrandExchange();
            } else if (!Banking.isBankScreenOpen()) {
                bankEventV2.openBank();
            } else {
                bankEventV2.depositAll();
                bankEventV2.updateCache();
                setComplete();
            }
        } else if (!grandExchangeArea.contains(me())) {
            Walker.walkToArea(grandExchangeArea);
            Timing.waitCondition( () -> grandExchangeArea.contains(me()), 10000);
        } else {
            if (needCoins()) {
                getCoins();
            } else if (Banking.isBankScreenOpen()) {
                Banking.close();
                Timing.waitCondition(() -> !Banking.isBankScreenOpen(), 4000);
            } else if (canOpenExchange()) {
                openGrandExchange();
            } else {
                General.println("grandExchangeSellHashMap: "+!grandExchangeSellHashMap.isEmpty());

                if(!grandExchangeSellHashMap.isEmpty()) {
                   for(Map.Entry<String, GrandExchangeItem> sellItemsMap : grandExchangeSellHashMap.entrySet()) {
                       String itemName = sellItemsMap.getKey();
                       int price = sellItemsMap.getValue().getPrice();
                       while (InventoryEvent.contains(itemName)) {
                           RSItem item = Methods.getInventoryItem(itemName);
                           if (GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW)) {
                               if (item.click("Offer")) {
                                   Timing.waitCondition(() -> GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.NEW_OFFER_WINDOW), 5000);
                               }
                           } else if (GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.NEW_OFFER_WINDOW)) {
                               if (!isItemPriceCorrect(price)) {
                                   setItemPrice(price);
                               } else {
                                   clickContinue();
                                   Timing.waitCondition(() -> (Arrays.stream(GrandExchange.getOffers()).anyMatch(i -> i.getStatus() == RSGEOffer.STATUS.COMPLETED) && GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW)), 10000);
                               }
                           }
                       }
                   }
                }
                for (Map.Entry<String, GrandExchangeItem> grandExchangeItems : grandExchangeItemHashMap.entrySet()) {
                    String itemName = grandExchangeItems.getKey();
                    General.println("Trying to buy: " + itemName);
                    int qty;
                    if (bankEventV2.bankCacheHashMap.get(itemName) != null) {
                        qty = grandExchangeItems.getValue().getQty() - bankEventV2.bankCacheHashMap.get(itemName).getQty();
                        if (qty <= 0) {
                            continue;
                        }
                    } else {
                        qty = grandExchangeItems.getValue().getQty();
                    }
                    if(canOpenExchange()) {
                        break;
                    }
                    int price = grandExchangeItems.getValue().getPrice();
                    RSGEOffer[] rsgeOffer2 = GrandExchange.getOffers();
                    if (Arrays.stream(rsgeOffer2).anyMatch(i -> i.getStatus() == RSGEOffer.STATUS.COMPLETED) && GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW)) {
                        General.println("Collecting items");
                        collectItemsInventory();
                        General.sleep(1000);
                        updateCache();
                    } else {

                        if (GrandExchange.getWindowState() != null) {
                            if (GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW)) {
                                if (alreadyHaveOffer(itemName)) {
                                    continue;
                                } else if (!alreadyHaveOffer(itemName)) {
                                    openFirstEmptyBox();
                                }
                            } else if (GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.NEW_OFFER_WINDOW)) {
                                while (!alreadyHaveOffer(itemName)) {
                                    if (needToChooseItem()) {
                                        if (isItemThere(itemName)) {
                                            clickItemChild();
                                            Timing.waitCondition(() -> !needToChooseItem(), 3000);
                                        } else if (!isItemThere(itemName)) {
                                            RSInterface buyTextBar = Interfaces.get(162, 42);
                                            Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(buyTextBar), 3000);
                                            new TypingEvent(script, itemName, false).execute();
                                            Timing.waitCondition(() -> isItemThere(itemName), 4000);
                                        }
                                    } else if (isItemCorrect(itemName)) {
                                        if (!isItemPriceCorrect(price)) {
                                            setItemPrice(price);
                                        } else if (!isItemQtyCorrect(qty)) {
                                            setItemQty(qty);
                                        } else {
                                            clickContinue();
                                            Timing.waitCondition(() -> GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW), 3000);
                                        }
                                    }
                                }
                            }
                            General.sleep(1000);
                        }
                    }
                }
            }
        }
    }

    public void collectItemsInventory() {
        RSInterface collectInterface = Interfaces.get(465, 6, 0);
        if (Interfaces.isInterfaceSubstantiated(collectInterface)) {
            if (collectInterface.click("Collect to inventory")) {
                General.sleep(1000);
            }
        }
    }

    public void updateItem(String itemName, int id, int amount) {
        int oldCache;
        if (bankEvent.bankCacheHashMap.containsKey(itemName)) {
            oldCache = bankEvent.bankCacheHashMap.get(itemName).getQty();
            bankEvent.bankCacheHashMap.replace(itemName, new BankCache(itemName, id, amount + oldCache));
        } else {
            bankEvent.bankCacheHashMap.put(itemName, new BankCache(itemName, id, amount));
        }
    }

    public void updateCache() {
        RSItem[] inventoryItems = Inventory.getAll();
        Arrays.stream(inventoryItems)
                .map(RSItem::getDefinition)
                .filter(Objects::nonNull)
                .forEach(rsItemDefinition -> {
                    final String name = rsItemDefinition.getName();
                    final int id = rsItemDefinition.getUnnotedItemID();

                    final int qty;

                    // find the qty by matching the current RSItem to the RSItem[] inventoryItems by ID
                    qty = Arrays.stream(inventoryItems)
                            .filter(rsItem -> rsItem.getDefinition().getUnnotedItemID() == rsItemDefinition.getID()) // find the current RSItem in stream using ID
                            .mapToInt(RSItem::getStack) // map the stack
                            .findFirst() // find it
                            .orElse(0); // return the stack or zero if not found

                    General.println("Updating cache: " + name + " ID: " + id + " qty: " + qty);

                    updateItem(name, id, qty);
                });
    }

    public void clickContinue() {
        RSInterface continueButton = Interfaces.get(465, 27, 0);
        if (Interfaces.isInterfaceSubstantiated(continueButton)) {
            continueButton.click();
            Timing.waitCondition(() -> GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.SELECTION_WINDOW), 3000);
        }
    }

    public boolean needToChooseItem() {
        RSInterface rsInterface = Interfaces.get(465, 24, 25);
        if (Interfaces.isInterfaceSubstantiated(rsInterface)) {
            return rsInterface.getText().equals("Choose an item...");
        }
        return false;
    }

    public boolean isItemCorrect(String itemName) {
        RSInterface itemNameInterface = Interfaces.get(465, 24, 25);
        if (Interfaces.isInterfaceSubstantiated(itemNameInterface)) {
            return itemNameInterface.getText().equals(itemName);
        }
        return false;
    }

    public boolean isItemPriceCorrect(int price) {
        RSInterface itemPriceText = Interfaces.get(465, 24, 39);
        if (Interfaces.isInterfaceSubstantiated(itemPriceText)) {
            String itemPrice = itemPriceText.getText().replace(" coins", "").replace(",", "");
            return Integer.parseInt(itemPrice) == price;
        }
        return false;
    }

    public void setItemPrice(int price) throws IOException, InterruptedException {
        RSInterface enterPriceButton = Interfaces.get(465, 24, 12);
        RSInterface enterPrice = Interfaces.get(162, 41);
        if (Interfaces.isInterfaceSubstantiated(enterPrice)) {
            String itemPrice = Integer.toString(price);
            new TypingEvent(script, itemPrice, true).execute();
            Timing.waitCondition(() -> isItemPriceCorrect(price), 4000);
        } else if (Interfaces.isInterfaceSubstantiated(enterPriceButton)) {
            if (enterPriceButton.click()) {
                Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(enterPrice), 2000);
            }
        }
    }

    public boolean isItemQtyCorrect(int qty) {
        RSInterface itemQtyText = Interfaces.get(465, 24, 32);
        if (Interfaces.isInterfaceSubstantiated(itemQtyText)) {
            return Integer.parseInt(itemQtyText.getText().replace(",", "")) == qty;
        }
        return false;
    }

    public void setItemQty(int qty) throws IOException, InterruptedException {
        RSInterface itemQtyButton = Interfaces.get(465, 24, 7);
        RSInterface enterQty = Interfaces.get(162, 41);
        if (Interfaces.isInterfaceSubstantiated(enterQty)) {
            String itemQty = Integer.toString(qty).replace(",", "");
            new TypingEvent(script, itemQty, true).execute();
        } else if (Interfaces.isInterfaceSubstantiated(itemQtyButton)) {
            if (itemQtyButton.click()) {
                Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(enterQty), 3000);
            }
        }
    }

    public void clickItemChild() {
        RSInterface itemList = ExchangeBoxes.itemList;
        if (Interfaces.isInterfaceSubstantiated(itemList)) {
            RSInterface itemChild = itemList.getChild(getItemChild());
            if (Interfaces.isInterfaceSubstantiated(itemChild)) {
                itemChild.click();
            } else {
                General.println("We need to scroll i guess");
                General.sleep(1000);
            }
        }
    }

    public boolean isItemThere(String itemName) {
        RSInterface itemList = ExchangeBoxes.itemList;
        if (Interfaces.isInterfaceSubstantiated(itemList)) {
            for (int i = 0; i < 26; i++) {
                if (Interfaces.isInterfaceSubstantiated(itemList.getChild(i))) {
                    String itemBoxName = itemList.getChild(i).getComponentName().replace("<col=ff9040>", "").replace("</col>", "");
                    if (itemBoxName.equals(itemName)) {
                        setItemChild(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getItemChild() {
        return itemChild;
    }

    public void setItemChild(int i) {
        itemChild = i;
    }

    public void openFirstEmptyBox() {
        General.println("Trying to open first empty box");
        RSInterface emptyBox = getFirstEmptyBox();
        if (emptyBox.getChild(ExchangeBoxes.buyChild).click()) {
            Timing.waitCondition(() -> GrandExchange.getWindowState().equals(GrandExchange.WINDOW_STATE.NEW_OFFER_WINDOW), 5000);
        }
    }

    public boolean alreadyHaveOffer(String itemName) {
        RSInterface[] allBoxes = new RSInterface[]{Interfaces.get(465, 7), Interfaces.get(465, 8), Interfaces.get(465, 9), Interfaces.get(465, 10), Interfaces.get(465, 11), Interfaces.get(465, 12), Interfaces.get(465, 13), Interfaces.get(465, 14)};
        return Arrays.stream(allBoxes).anyMatch(i -> i.getChild(19).getText().equals(itemName));
    }

    public boolean isBoxEmpty(RSInterface box) {
        return Interfaces.isInterfaceSubstantiated(box.getChild(ExchangeBoxes.buyChild));
    }

    public Optional<RSInterface> getEmptyBox() {
        RSInterface[] allBoxes = new RSInterface[]{Interfaces.get(465, 7), Interfaces.get(465, 8), Interfaces.get(465, 9), Interfaces.get(465, 10), Interfaces.get(465, 11), Interfaces.get(465, 12), Interfaces.get(465, 13), Interfaces.get(465, 14)};
        int buyChild = ExchangeBoxes.buyChild;
        return Arrays.stream(allBoxes).filter(Objects::nonNull).filter(i -> Interfaces.isInterfaceSubstantiated(i.getChild(buyChild))).findAny();
    }

    public RSInterface getFirstEmptyBox() {
        RSInterface[] allBoxes = new RSInterface[]{Interfaces.get(465, 7), Interfaces.get(465, 8), Interfaces.get(465, 9), Interfaces.get(465, 10), Interfaces.get(465, 11), Interfaces.get(465, 12), Interfaces.get(465, 13), Interfaces.get(465, 14)};
        int buyChild = ExchangeBoxes.buyChild;
        return Arrays.stream(allBoxes).filter(d -> Interfaces.isInterfaceSubstantiated(d.getChild(buyChild))).findFirst().get();
    }

    public boolean noEmptyBoxes() {
        RSInterface[] allBoxes = new RSInterface[]{Interfaces.get(465, 7), Interfaces.get(465, 8), Interfaces.get(465, 9), Interfaces.get(465, 10), Interfaces.get(465, 11), Interfaces.get(465, 12), Interfaces.get(465, 13), Interfaces.get(465, 14)};
        int buyChild = ExchangeBoxes.buyChild;
        return Arrays.stream(allBoxes).noneMatch(i -> Interfaces.isInterfaceSubstantiated(i.getChild(buyChild)));
    }


    public boolean needCoins() {
        return bankEventV2.addReq("Coins", Integer.MAX_VALUE).isPendingOperation();
    }
    BankEventV2 bankEventV2;

    public void getCoins() throws IOException, InterruptedException {
        if(grandExchangeSellHashMap.isEmpty()) {
            General.println("SellHashMap is empty");
           bankEventV2 = new BankEventV2(script).addReq("Coins", Integer.MAX_VALUE);
           bankEventV2.execute();
           bankEventV2.reset();
        } else {
            for(Map.Entry<String, GrandExchangeItem> grandSellItems :grandExchangeSellHashMap.entrySet()) {
                String itemName = grandSellItems.getValue().getItemName();
                General.println("Adding: "+itemName+" to bankevent");
                bankEventV2.addReq(itemName, Integer.MAX_VALUE, () -> bankEventV2.bankCacheHashMap.containsKey(itemName) || InventoryEvent.contains(itemName));
            }
            if(bankEventV2.isPendingOperation()) {
                bankEventV2.execute();
                bankEventV2 = new BankEventV2(script);
                bankEventV2.reset();
            }
        }
    }

    public boolean canOpenExchange() {
        return GrandExchange.getWindowState() == null;
    }

    public void openGrandExchange() {
        RSObject booth = Methods.getObjectWithAction("Exchange", "Grand Exchange booth", 20);
        if (booth != null) {
            if (!booth.isOnScreen() || !booth.isClickable()) {
                booth.adjustCameraTo();
            } else if (AccurateMouse.click(booth, "Exchange")) {
                Timing.waitCondition(() -> GrandExchange.getWindowState() != null, 5000);
            }
        }
    }

    public void closeGrandExchange() {
        if (!canOpenExchange()) {
            GrandExchange.close();
            Timing.waitCondition(() -> canOpenExchange(), 4000);
        }
    }

    public RSPlayer me() {
        return Player.getRSPlayer();
    }
}
