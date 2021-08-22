package scripts.core.cannonballs;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.grandexchange.GEEvent;
import scripts.core.utilities.Methods;
import scripts.core.utilities.Walker;

import java.io.IOException;

public class GetRemainingLevelsEvent extends BotEvent {

    BankEventV2 bankEvent;
    GEEvent geEvent;

    RSArea area = new RSArea(new RSTile(3185, 3427, 0), new RSTile(3190, 3420, 0));

    public GetRemainingLevelsEvent(Script script) {
        super(script);
        bankEvent = new BankEventV2(script).addReq("Hammer", 1)
                .addReq("Iron bar", 27);
        geEvent = new GEEvent(script, bankEvent).addReq("Hammer", 1, (int) (WhipzCannonBaller.prices.getPrice(2347).get() * 2.10))
                .addReq("Iron bar", 417, (int) (WhipzCannonBaller.prices.getPrice(2351).get() * 2.10));
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (bankEvent.needCache() && bankEvent.isPendingOperation()) {
            bankEvent.openBank();
        } else if (!bankEvent.needCache() && geEvent.isPendingOperation() && bankEvent.isPendingOperation()) {
            geEvent.execute();
            geEvent.reset();
            General.sleep(1000);
        } else if (bankEvent.isPendingOperation()) {
            bankEvent.execute();
            bankEvent.reset();
        } else if (!area.contains(Player.getRSPlayer())) {
            Walker.walkToArea(area);
            Timing.waitCondition(() -> area.contains(Player.getRSPlayer()), 10000);
        } else {
            RSObject anvil = Methods.getObject("Anvil", 10);
            RSInterface mainWindow = Interfaces.get(312, 0);
            RSInterface iron2H = Interfaces.get(312, 13);
            if (Interfaces.isInterfaceSubstantiated(mainWindow)) {
                if (Interfaces.isInterfaceSubstantiated(iron2H)) {
                    if (iron2H.click("")) {
                        Timing.waitCondition(() -> bankEvent.isPendingOperation() || isConversationWindowUp(), 60000);
                    }
                }
            } else if (anvil != null) {
                if (anvil.isOnScreen()) {
                    if (anvil.click("Smith")) {
                        Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(mainWindow), 4000);
                    }
                }
            }
        }
    }
}
