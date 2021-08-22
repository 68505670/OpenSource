package scripts.core.cannonballs;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.NPCChat;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.grandexchange.GEEvent;
import scripts.core.inventory.InventoryEvent;
import scripts.core.utilities.Methods;
import scripts.core.utilities.Walker;

import java.io.IOException;

public class MakeCannonBallEvent extends BotEvent {

    BankEventV2 bankEvent;
    GEEvent geEvent;

    RSArea area = new RSArea(new RSTile(3105, 3501, 0), new RSTile(3110, 3496, 0));

    RSArea bankArea = new RSArea(
            new RSTile[]{
                    new RSTile(3099, 3488, 0),
                    new RSTile(3099, 3500, 0),
                    new RSTile(3091, 3500, 0),
                    new RSTile(3091, 3498, 0),
                    new RSTile(3090, 3497, 0),
                    new RSTile(3090, 3494, 0),
                    new RSTile(3091, 3493, 0),
                    new RSTile(3091, 3488, 0)
            }
    );

    public MakeCannonBallEvent(Script script) {
        super(script);
        bankEvent = new BankEventV2(script).addReq("Ammo mould", 1).addReq("Steel bar", 27);
        geEvent = new GEEvent(script, bankEvent).sellReq("Cannonball", (int) (WhipzCannonBaller.prices.getPrice(2).get() * WhipzCannonBaller.sellPrice)).addReq("Steel bar", WhipzCannonBaller.restockAmount, (int) (WhipzCannonBaller.prices.getPrice(2353).get() * WhipzCannonBaller.buyPrice));
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (!bankEvent.needCache()) {
            General.println("geEvent is Pending Operation: " + geEvent.isPendingOperation());
            if (geEvent.isPendingOperation()) {
                geEvent.execute();
                geEvent.reset();
            } else if (bankEvent.bankCacheHashMap.containsKey("Ammo mould") || InventoryEvent.contains("Ammo mould")) {
                if (bankEvent.isPendingOperation() && !geEvent.isPendingOperation()) {
                    bankEvent.execute();
                    bankEvent.reset();
                } else if (!area.contains(Player.getRSPlayer())) {
                    if (bankArea.contains(Player.getRSPlayer())) {
                        Walker.blindWalkToTile(area.getRandomTile());
                        Timing.waitCondition(() -> area.contains(Player.getRSPlayer()), 10000);
                    } else {
                        Walker.walkToArea(area);
                        Timing.waitCondition(() -> area.contains(Player.getRSPlayer()), 10000);
                    }
                } else {
                    RSObject furnace = Methods.getObject(5, "Furnace");
                    RSInterface smeltInterface = Interfaces.get(270, 14);
                    if (Interfaces.isInterfaceSubstantiated(smeltInterface)) {
                        Keyboard.typeString(" ");
                        Timing.waitCondition(() -> bankEvent.isPendingOperation() || NPCChat.getClickContinueInterface() != null, 180000);
                    } else if (furnace != null) {
                        if (furnace.isOnScreen()) {
                            if (furnace.click("Smelt")) {
                                Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(smeltInterface), 3000);
                            }
                        } else furnace.adjustCameraTo();
                    }
                }
            } else {
                new GetMouldEvent(script).setInterruptCondition(() -> InventoryEvent.contains("Ammo mould")).execute();
            }
        } else {
            bankEvent.openBank();
        }
    }
}
