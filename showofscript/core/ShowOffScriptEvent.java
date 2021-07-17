package scripts.core;

import org.tribot.api.General;
import org.tribot.script.Script;
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

    public ShowOffScriptEvent(Script script) {
        super(script);
        bankLoadout = new BankEventV2(script).addReq("Rune scimitar", 1).addReq("Rune arrows", 500).addReq("Dragon platelegs", 1).addReq("Ava's accumulator", 1);
        loadoutEvent = new LoadoutEvent(script, bankLoadout);
        bankEvent = new BankEventV2(script).addReq("Potato", 10);
        geEvent = new GEEvent(script, bankEvent).addReq("Potato", 10, 300).addReq("Vial", 10, 100).addReq("Bronze axe", 2, 1000);
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (bankEvent.needCache()) {
            bankEvent.openBank();
        } else if (geEvent.isPendingOperation() && bankEvent.isPendingOperation()) {
            geEvent.execute();
            General.println("Completed GE");
            General.sleep(1000);
            geEvent.reset();
        } else if (bankEvent.isPendingOperation()) {
            bankEvent.execute();
            bankEvent.reset();
        }
        General.sleep(1000);
    }
}
