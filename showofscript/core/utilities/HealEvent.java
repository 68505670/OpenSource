package scripts.core.utilities;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.RSItem;
import org.tribot.script.Script;
import scripts.core.botevent.BotEvent;
import scripts.core.inventory.InventoryEvent;

import java.io.IOException;

public class HealEvent extends BotEvent {

	InventoryEvent invent;

	String food;
	double health;
	int eatAt;
	HealEvent healEvent;

	public HealEvent(Script script) {
		super(script);
		invent = new InventoryEvent(script);
	}

	public HealEvent(Script script, HealEvent healEvent) {
		super(script);
		this.healEvent = healEvent;
		return;
	}

	public HealEvent foodType(String foodName) {
		food = foodName;
		return this;
	}

	public HealEvent healthPercent(double healthPercent) {
		health = healthPercent;
		return this;
	}

	public boolean canEat() {
		return InventoryEvent.contains(food) && (((SKILLS.HITPOINTS.getActualLevel()
				- SKILLS.HITPOINTS.getCurrentLevel()) >= (SKILLS.HITPOINTS.getActualLevel() * (40.0f / 100.0f))));
	}

	public void eat() {
		if (canEat()) {
			RSItem eat = InventoryEvent.getInventoryItem(food);
			General.println("Heal Event: Eating: " + eat.getDefinition().getName());
			if (eat.click("Eat")) {
				Timing.waitCondition(() -> !canEat(), 500);
				setComplete();
			}
		}
	}

	@Override
	public void step() throws InterruptedException, IOException {
		if (canEat()) {
			eat();
		}
	}

	public HealEvent below(int below) {
		eatAt = below;
		return this;
	}

}
