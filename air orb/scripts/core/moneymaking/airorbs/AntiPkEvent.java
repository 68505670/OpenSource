package scripts.core.moneymaking.airorbs;

import org.tribot.api2007.Player;
import org.tribot.api2007.Players;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import scripts.core.botevent.BotEvent;
import scripts.core.utilities.Walker;

import java.io.IOException;

public class AntiPkEvent extends BotEvent {

    public static RSPlayer me = Player.getRSPlayer();
    RSArea bankArea = new RSArea(
            new RSTile[]{
                    new RSTile(3099, 3500, 0),
                    new RSTile(3099, 3488, 0),
                    new RSTile(3091, 3488, 0),
                    new RSTile(3091, 3493, 0),
                    new RSTile(3090, 3494, 0),
                    new RSTile(3090, 3497, 0),
                    new RSTile(3091, 3498, 0),
                    new RSTile(3091, 3500, 0)
            }
    );

    public AntiPkEvent(Script script) {
        super(script);
    }

    public static boolean shouldTeleport() {
        RSPlayer[] pker = Players.find(i -> i.isInteractingWithMe());
        return pker.length > 0 && me.isInCombat();
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (bankArea.contains(me)) {
            setComplete();
        } else if (!bankArea.contains(me)) {
            WhipzAirOrberScript.setStatus("Teleporting due to Pker");
            Walker.walkToArea(bankArea);
        }
    }
}
