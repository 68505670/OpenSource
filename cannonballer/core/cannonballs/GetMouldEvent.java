package scripts.core.cannonballs;

import org.tribot.api.Timing;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.inventory.InventoryEvent;
import scripts.core.utilities.Methods;
import scripts.core.utilities.Walker;

import java.io.IOException;

public class GetMouldEvent extends BotEvent {

    RSArea area = new RSArea(
            new RSTile[]{
                    new RSTile(3015, 3452, 0),
                    new RSTile(3015, 3454, 0),
                    new RSTile(3015, 3455, 0),
                    new RSTile(3008, 3455, 0),
                    new RSTile(3008, 3449, 0),
                    new RSTile(3011, 3449, 0)
            }
    );

    BankEventV2 bankEvent;

    public GetMouldEvent(Script script) {
        super(script);
        bankEvent = new BankEventV2(script).addReq("Coins", 5000);
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (bankEvent.isPendingOperation()) {
            bankEvent.execute();
            bankEvent.reset();
        } else if (!area.contains(Player.getRSPlayer())) {
            Walker.walkToArea(area);
            Timing.waitCondition(() -> area.contains(Player.getRSPlayer()), 10000);
        } else {
            RSNPC nulodion = Methods.getNPC("Nulodion");
            RSInterface shopInterface = Interfaces.get(300, 0);
            RSInterface mouldInterface = Interfaces.get(300, 16, 6);
            if (Interfaces.isInterfaceSubstantiated(shopInterface)) {
                if (Interfaces.isInterfaceSubstantiated(mouldInterface)) {
                    if (mouldInterface.click("Buy 1")) {
                        Timing.waitCondition(() -> InventoryEvent.contains("Ammo mould"), 6000);
                    }
                }
            } else {
                if (nulodion != null) {
                    if (nulodion.isOnScreen()) {
                        if (nulodion.click("Trade")) {
                            Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(shopInterface), 5000);
                        }
                    }
                }
            }
        }
    }
}
