package scripts.core.grandexchange;

import org.tribot.api.General;
import org.tribot.api.input.Keyboard;
import org.tribot.script.Script;
import scripts.core.botevent.BotEvent;

import java.io.IOException;

public class TypingEvent extends BotEvent {

    String string;
    boolean enter;

    public TypingEvent(Script script, String string, boolean enter) {
        super(script);
        this.string = string;
        this.enter = enter;
    }

    /*  else if(Interfaces.isInterfaceSubstantiated(itemList)) {
                for (int i = 0; i> 9;i++) {
                    if(itemList.getChild(i).getComponentName().contains(itemName)) {
                        setComplete();
                    }
                }*/

    @Override
    public void step() throws InterruptedException, IOException {
        if (enter) {
            Keyboard.typeSend(string);
        } else {
            Keyboard.typeString(string);
        }
        General.sleep(1000);
        setComplete();
    }
}
