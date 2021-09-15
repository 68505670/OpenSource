package scripts.core.moneymaking.airorbs;

import org.tribot.api.DynamicClicking;
import org.tribot.api.Timing;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import scripts.core.banking.BankEvent;
import scripts.core.botevent.BotEvent;
import scripts.core.equipment.LoadoutEvent;
import scripts.core.utilities.Methods;

import java.io.IOException;

public class DeathEvent extends BotEvent {

    public String[] chat = new String[]{"Tell me about gravestones again.", "How do I pay a gravestone fee?",
            "How long do I have to return to my gravestone?", "How do I know what will happen to my items when I die?",
            "I think I'm done here.", "Can I collect the items from that gravestone now?",
            "Bring my items here now; I'll pay your fee.", "Pay Death's fee."};
    BankEvent bankEvent;
    RSArea dArea = new RSArea(new RSTile(3125, 3632, 0), new RSTile(3131, 3628, 0));
    RSArea area = new RSArea(new RSTile[]{new RSTile(3148, 3641, 0), new RSTile(3154, 3641, 0),
            new RSTile(3154, 3628, 0), new RSTile(3144, 3628, 0), new RSTile(3142, 3628, 0), new RSTile(3142, 3619, 0),
            new RSTile(3126, 3619, 0), new RSTile(3126, 3633, 0), new RSTile(3126, 3635, 0),
            new RSTile(3148, 3635, 0)});

    public DeathEvent(Script script) {
        super(script);
        bankEvent = new BankEvent(script).addReq("Ring of dueling(8~1)", 1,
                () -> !LoadoutEvent.isWearingPartName("Ring of dueling"));
    }

    public static boolean shouldHandleDeath() {
        RSNPC[] death = NPCs.find("Death");
        return death.length > 0;
    }

    @Override
    public void step() throws InterruptedException, IOException { // TEMP - prevent super fast loop
        RSNPC[] death = NPCs.find("Death");
        if (death.length > 0) {
            if (Timing.waitCondition(() -> isConversationWindowUp(), 10000)) {
                handleChatV2(chat);
            } else {
                RSObject portal = Methods.getObject(15, "Portal");
                if (portal != null) {
                    if (portal.isOnScreen() && portal.isClickable()) {
                        if (DynamicClicking.clickRSObject(portal, "Exit")) {
                            Timing.waitCondition(() -> death.length == 0, 5000);
                        }
                    }
                }
            }
        }

    }

}