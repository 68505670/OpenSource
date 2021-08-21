package scripts.core;

import org.tribot.script.Script;
import org.tribot.script.sdk.tasks.BankTask;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.equipment.LoadoutEvent;
import scripts.core.grandexchange.GEEvent;

import java.io.IOException;

public class ShowOffScriptEvent extends BotEvent {

    BankEventV2 bankEvent;
    LoadoutEvent loadoutEvent;
    BankEventV2 bankLoadout;
    GEEvent geEvent;
    BankTask bankTask;

    public ShowOffScriptEvent(Script script) {
        super(script);
        bankLoadout = new BankEventV2(script).addReq("Staff of earth",1);
        loadoutEvent = new LoadoutEvent(script, bankLoadout);
        bankEvent = new BankEventV2(script).addReq("Potato", 10);
        geEvent = new GEEvent(script, bankEvent).addReq("Potato", 10, 300).addReq("Vial", 10, 100).addReq("Bronze axe", 2, 1000);

    }

    @Override
    public void step() throws InterruptedException, IOException {
        if(loadoutEvent.isPendingOperation()) {
            loadoutEvent.execute();
            loadoutEvent.reset();
        }
    }
}
