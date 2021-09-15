package scripts.core.moneymaking.airorbs;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import scripts.core.banking.BankEventV2;
import scripts.core.botevent.BotEvent;
import scripts.core.equipment.LoadoutEvent;
import scripts.core.utilities.DrinkStaminaEvent;
import scripts.core.utilities.Walker;

import java.awt.event.KeyEvent;
import java.io.IOException;

public class WhipzAirOrberEvent extends BotEvent {

    public static RSArea bankArea = new RSArea(
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
    static int startXP;
    BankEventV2 bankEvent;
    BankEventV2 bankLoadout;
    LoadoutEvent loadoutEvent;
    EatBankEvent eatBankEvent;
    DrinkStaminaEvent drinkStaminaEvent;
    AntiPkEvent pkEvent;
    RSArea orbArea = new RSArea(
            new RSTile[]{
                    new RSTile(3082, 3574, 0),
                    new RSTile(3082, 3568, 0),
                    new RSTile(3086, 3565, 0),
                    new RSTile(3091, 3565, 0),
                    new RSTile(3094, 3568, 0),
                    new RSTile(3094, 3574, 0),
                    new RSTile(3091, 3577, 0),
                    new RSTile(3086, 3577, 0)
            }
    );
    RSTile tile = new RSTile(3088, 3570, 0);
    long last;


    public WhipzAirOrberEvent(Script script) {
        super(script);
        bankEvent = new BankEventV2(script).addReq("Cosmic rune", 81).addReq("Unpowered orb", 27);
        bankLoadout = new BankEventV2(script).addReq("Amulet of glory(6~1)", 1, () -> !LoadoutEvent.isWearing("Amulet of glory(6~1)")).addReq("Staff of air", 1, () -> !LoadoutEvent.isWearing("Staff of air"));
        loadoutEvent = new LoadoutEvent(script, bankLoadout);
        eatBankEvent = new EatBankEvent(script, WhipzAirOrberScript.eatFoodAt, WhipzAirOrberScript.foodFoodType);
        drinkStaminaEvent = new DrinkStaminaEvent(script);
        pkEvent = new AntiPkEvent(script);
    }

    @Override
    public void step() throws InterruptedException, IOException {
        Mouse.setSpeed(General.random(180, 250));
        if (DeathEvent.shouldHandleDeath()) {
            WhipzAirOrberScript.setStatus("Handling Death Event");
            new DeathEvent(script).execute();
        } else if (AntiPkEvent.shouldTeleport()) {
            WhipzAirOrberScript.setStatus("Handling PK Event");
            if (Magic.isSpellSelected()) {
                Mouse.randomRightClick();
            }
            pkEvent.execute();
            pkEvent.reset();
            if (eatBankEvent.needToEat()) {
                eatBankEvent.execute();
                eatBankEvent.reset();
            } else if (WhipzAirOrberScript.useStamina) {
                drinkStaminaEvent.execute();
                drinkStaminaEvent.reset();
            }
            bankEvent.execute();
            bankEvent.reset();
        } else if (loadoutEvent.isPendingOperation()) {
            loadoutEvent.execute();
            loadoutEvent.reset();
        } else if (bankEvent.isPendingOperation() && bankArea.contains(me())) {
            if (Magic.isSpellSelected()) {
                Mouse.randomRightClick();
            }
            if (eatBankEvent.needToEat()) {
                eatBankEvent.execute();
                eatBankEvent.reset();
            }
            if (WhipzAirOrberScript.useStamina) {
                drinkStaminaEvent.execute();
                drinkStaminaEvent.reset();
            }
            bankEvent.execute();
            bankEvent.reset();
        } else if (bankEvent.isPendingOperation() && !bankArea.contains(me())) {
            if (Magic.isSpellSelected() || Magic.getSelectedSpellName() != null) {
                WhipzAirOrberScript.setStatus("Spell is selected");
                Mouse.click(0);
            }
            WhipzAirOrberScript.setStatus("Walking to bank");
            Walker.walkToArea(bankArea);
            Timing.waitCondition(() -> bankArea.contains(me()), 5000);
        } else if (orbArea.contains(me())) {
            if (me().getAnimation() != -1) {
                last = System.currentTimeMillis();
            } else {
                if (System.currentTimeMillis() > (last + 4000)) {
                    RSInterface chargeInterface = Interfaces.get(270, 5);
                    if (Interfaces.isInterfaceSubstantiated(chargeInterface)) {
                        Keyboard.pressKeys(KeyEvent.VK_SPACE);
                        General.sleep(750, 1500);
                        if (Magic.isSpellSelected()) {
                            Mouse.click(1);
                        }
                        Timing.waitCondition(() -> bankEvent.isPendingOperation() || NPCChat.getClickContinueInterface() != null || AntiPkEvent.shouldTeleport() || System.currentTimeMillis() > (last + 4000), 120000);
                    } else if (Magic.isSpellSelected()) {
                        RSObject[] obelisk = Objects.findNearest(10, "Obelisk of Air");
                        if (obelisk.length > 0) {
                            WhipzAirOrberScript.setStatus("We are clicking on Obelisk");
                            if (DynamicClicking.clickRSObject(obelisk[0], 1)) {
                                Timing.waitCondition(() -> Interfaces.isInterfaceSubstantiated(chargeInterface), 1500);
                            }
                        }
                    } else {
                        WhipzAirOrberScript.setStatus("Trying to Select Spell");
                        if (Magic.selectSpell("Charge Air Orb")) {
                            Timing.waitCondition(Magic::isSpellSelected, 5000);
                        }
                    }
                }
            }
        } else if (!bankEvent.isPendingOperation() && !orbArea.contains(me())) {
            if (Magic.isSpellSelected()) {
                Mouse.randomRightClick();
            }
            Walker.walkToTile(tile);
        }
    }

    private RSPlayer me() {
        return Player.getRSPlayer();
    }
}
