package scripts.core.utilities;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Game;
import org.tribot.api2007.types.RSItem;
import org.tribot.script.Script;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.inventory.InventoryEvent;

import java.io.IOException;
import java.util.List;

public class DrinkStaminaEvent extends BotEvent {

    BankEventV2 bankEvent;

    String finalItem = "";

    public DrinkStaminaEvent(Script script) {
        super(script);
        bankEvent = new BankEventV2(script).addReq("Stamina potion(4~1)", 1);
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (bankEvent.isPendingOperation()) {
            bankEvent.execute();
            bankEvent.reset();
        } else if (Banking.isBankScreenOpen()) {
            if (Banking.close()) {
                Timing.waitCondition(() -> !Banking.isBankScreenOpen(), 5000);
            }
        } else {
            General.println("Drinking Stamina");
            List<String> expandedItem = BankEventV2.expandItemName("Stamina potion(1~4)");
            if (expandedItem.size() > 0 && finalItem.equals("")) {
                for (String item : expandedItem) {
                    if (InventoryEvent.contains(item) && finalItem.equals("")) {
                        finalItem = item;
                        General.println("FinalItem: " + finalItem);
                        break;
                    }
                }
            } else {
                RSItem stamina = InventoryEvent.getInventoryItem(finalItem);
                if (Game.getItemSelectionState() == 0) {
                    if (stamina != null) {
                        General.println("Clicking " + stamina.getDefinition().getName());
                        if (Clicking.click(stamina)) {
                            General.sleep(777, 1500);
                            finalItem = "";
                            setComplete();
                        }
                    }
                }
            }
        }

    }

}
