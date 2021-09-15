package scripts.core.moneymaking.airorbs;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Game;
import org.tribot.api2007.Skills;
import org.tribot.api2007.types.RSItem;
import org.tribot.script.Script;
import scripts.core.banking.BankEvent;
import scripts.core.botevent.BotEvent;
import scripts.core.inventory.InventoryEvent;

import java.io.IOException;

public class EatBankEvent extends BotEvent {

    BankEvent bankEvent;
    int eatBelow;
    String foodType;

    public EatBankEvent(Script script, int eatBelow, String food) {
        super(script);
        this.eatBelow = eatBelow;
        this.foodType = food;
        bankEvent = new BankEvent(script).addReq(foodType, 5);
    }

    public boolean needToEat() {
        return Skills.SKILLS.HITPOINTS.getCurrentLevel() < eatBelow;
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (!needToEat()) {
            General.println("Completed eating for next run");
            setComplete();
        } else if (bankEvent.isPendingOperation()) {
            WhipzAirOrberScript.setStatus("Banking for food");
            bankEvent.execute();
            bankEvent.reset();
        } else {
            if (!Banking.isBankScreenOpen()) {
                RSItem food = InventoryEvent.getInventoryItem(foodType);
                if (Game.getItemSelectionState() == 0) {
                    if (Clicking.click(food)) {
                        General.sleep(300, 750);
                    }
                } else {
                    General.println("Deselecting item");
                    Clicking.click(food);
                }
            } else {
                WhipzAirOrberScript.setStatus("Closing bank");
                Banking.close();
            }
        }
    }
}
