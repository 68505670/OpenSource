package scripts.core.cannonballs;

import org.tribot.api2007.Skills;
import org.tribot.script.Script;
import scripts.core.botevent.BotEvent;
import scripts.core.quests.QuestList;

import java.io.IOException;

public class WhipzCannonBallerEvent extends BotEvent {

    public WhipzCannonBallerEvent(Script script) {
        super(script);
    }

    @Override
    public void step() throws InterruptedException, IOException {
        if (WhipzCannonBaller.doricQuest && !QuestList.DORICS_QUEST.isComplete()) {
            new DoricsQuestEvent(script, QuestList.DORICS_QUEST).setInterruptCondition(() -> QuestList.DORICS_QUEST.isComplete()).execute();
        } else if (WhipzCannonBaller.dwarfCannonQuest && !QuestList.DWARF_CANNON.isComplete()) {
            new DwarfCannonEvent(script, QuestList.DWARF_CANNON).setInterruptCondition(() -> QuestList.DWARF_CANNON.isComplete()).execute();
        } else if (WhipzCannonBaller.knightSwordQuest && !QuestList.THE_KNIGHTS_SWORD.isComplete()) {
            new KnightsSwordEvent(script, QuestList.THE_KNIGHTS_SWORD).setInterruptCondition(() -> QuestList.THE_KNIGHTS_SWORD.isComplete()).execute();
        } else if (Skills.getActualLevel(Skills.SKILLS.SMITHING) < 35 && WhipzCannonBaller.levelTask) {
            new GetRemainingLevelsEvent(script).setInterruptCondition(() -> Skills.getActualLevel(Skills.SKILLS.SMITHING) >= 35).execute();
        } else {
            new MakeCannonBallEvent(script).execute();
        }
    }
}
