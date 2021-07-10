package scripts.core.skills.cooking;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.utilities.Methods;
import scripts.core.utilities.Walker;

import java.io.IOException;

public class WhipzCookerEvent extends BotEvent {

    String rawFood = "";
    BankEventV2 bankEvent;
    RSArea area;

    long last;


    public WhipzCookerEvent(Script script, String rawFood, RSArea area) {
        super(script);
        this.rawFood = rawFood;
        this.area = area;
        bankEvent = new BankEventV2(script).addReq(rawFood, 28);
    }

    public RSInterface getCookInterface() {
        return Interfaces.get(270);
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if(bankEvent.isPendingOperation()) {
            bankEvent.execute();
            bankEvent.reset();
        }  else if(area.contains(me())) {
            RSObject[] range = Objects.findNearest(20, i->i.getDefinition().getName().toString().equals("Stove") || i.getDefinition().getName().toString().equals("Cooking range") || i.getDefinition().getName().toString().equals("Fire") || i.getDefinition().getName().toString().equals("Range"));
            RSItem[] foodToCook = Inventory.find(i->i.getDefinition().getName().toString().equals(rawFood));
            RSInterface cook = Interfaces.get(270);
            if(Banking.isBankScreenOpen()) {
                Banking.close();
                Timing.waitCondition( () -> !Banking.isBankScreenOpen(), 2000);
            }else
            if (me().getAnimation() != -1) {
                last = System.currentTimeMillis();
            } else {
                if (Interfaces.isInterfaceSubstantiated(getCookInterface())) {
                    WhipzAIOCookerScript.setStatus("Pressing space bar");
                    Keyboard.typeString("1");
                    General.sleep(4000, 5000);
                    Timing.waitCondition(() -> bankEvent.isPendingOperation() || NPCChat.getClickContinueInterface() != null || System.currentTimeMillis() > (last + 4000), 120000);
                } else
                if (System.currentTimeMillis() > (last + 4000) || Interfaces.isInterfaceSubstantiated(cook)) {
                    if (range.length > 0 && foodToCook.length > 0) {
                        Methods.useItemOnObject(foodToCook[0], range[0]);
                        Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(getCookInterface()), 4000);
                    }
                }
            }
        } else {
            Walker.walkToArea(area);
        }
    }

    RSPlayer me() {
        return Player.getRSPlayer();
    }
}
