package scripts.core.utilities;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Game;
import org.tribot.api2007.Options;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;

import scripts.core.botevent.BotEvent;
import scripts.core.equipment.LoadoutEvent;
import scripts.core.inventory.InventoryEvent;
import scripts.dax_api.api_lib.DaxWalker;
import scripts.dax_api.walker_engine.WalkingCondition.State;

import java.io.IOException;

public class Walker extends BotEvent {

	public Walker(Script script) {
		super(script);
		invent = new InventoryEvent(script);
		healEvent = new HealEvent(script).foodType("Trout").healthPercent(0.4);
	}

	
	Methods methods;
	InventoryEvent invent;
	static HealEvent healEvent;

	public static void walkToArea(RSArea area) {
		if (area != null && !area.contains(Player.getRSPlayer().getPosition())) {
			General.println("Walking to area");
			if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
				General.println("Activating run");
				Options.setRunEnabled(true);
				Timing.waitCondition(() -> Game.isRunOn(), 3000);
			} else {
				DaxWalker.walkTo(area.getRandomTile(), () -> {
					if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
						General.println("Activating run");
						Options.setRunEnabled(true);
						Timing.waitCondition(() -> Game.isRunOn(), 3000);
						return State.CONTINUE_WALKER;
					} else if (area.contains(Player.getRSPlayer().getPosition())) {
						return State.EXIT_OUT_WALKER_SUCCESS;
					} else if(Game.getDestination().getPosition() != null && area.contains(Game.getDestination().getPosition())){
						Timing.waitCondition( () -> area.contains(Player.getRSPlayer()), 5000);
						return State.EXIT_OUT_WALKER_SUCCESS;
					} else {
						return State.CONTINUE_WALKER;
					}
				});
			}
		}
	}

	public static void walkToAreaHeal(RSArea area, HealEvent healEvent) {
		if (area != null && !area.contains(Player.getRSPlayer().getPosition())) {
			General.println("Walking to area");
			if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
				General.println("Activating run");
				Options.setRunEnabled(true);
				Timing.waitCondition(() -> Game.isRunOn(), 3000);
			} else {
				DaxWalker.walkTo(area.getRandomTile(), () -> {
					if (healEvent.canEat()) {
						healEvent.eat();
						return State.CONTINUE_WALKER;
					} else if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
						General.println("Activating run");
						Options.setRunEnabled(true);
						Timing.waitCondition(() -> Game.isRunOn(), 3000);
						return State.CONTINUE_WALKER;
					}
					return State.CONTINUE_WALKER;
				});
			}
		}
	}

	public static void walkToTile(RSTile tile) {
		if (tile != null && tile.distanceTo(Player.getRSPlayer().getPosition()) >= 2) {
			if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
				General.println("Activating run");
				Options.setRunEnabled(true);
				Timing.waitCondition(() -> Game.isRunOn(), 3000);
			} else {
				DaxWalker.walkTo(tile, () -> {
					if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
						General.println("Activating run");
						Options.setRunEnabled(true);
						Timing.waitCondition(() -> Game.isRunOn(), 3000);
						return State.CONTINUE_WALKER;
					}
					return State.CONTINUE_WALKER;
				});
			}
		}
	}

	public static void walkToTileConditionItem(RSTile tile, String itemName) {
		if (tile != null && tile.distanceTo(Player.getRSPlayer().getPosition()) >= 2) {
			if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
				General.println("Activating run");
				Options.setRunEnabled(true);
				Timing.waitCondition(() -> Game.isRunOn(), 3000);
			} else {
				DaxWalker.walkTo(tile, () -> {
					if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
						General.println("Activating run");
						Options.setRunEnabled(true);
						Timing.waitCondition(() -> Game.isRunOn(), 3000);
						return State.CONTINUE_WALKER;
					} else if (InventoryEvent.containsPartName(itemName)) {
						return State.EXIT_OUT_WALKER_SUCCESS;
					} else if (LoadoutEvent.isWearingPartName(itemName)) {
						return State.EXIT_OUT_WALKER_SUCCESS;
					}
					return State.CONTINUE_WALKER;
				});
			}
		}
	}

	public static void walkToTileHeal(RSTile tile, HealEvent healEvent) {
		if (tile != null && tile.distanceTo(Player.getRSPlayer().getPosition()) > 1) {
			if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
				General.println("Activating run");
				Options.setRunEnabled(true);
				Timing.waitCondition(() -> Game.isRunOn(), 3000);
			} else {
				DaxWalker.walkTo(tile, () -> {
					if (healEvent.canEat()) {
						healEvent.eat();
						return State.CONTINUE_WALKER;
					} else if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
						General.println("Activating run");
						Options.setRunEnabled(true);
						Timing.waitCondition(() -> Game.isRunOn(), 3000);
						return State.CONTINUE_WALKER;
					} else {
						return State.CONTINUE_WALKER;
					}
				});
			}
		}
	}

	public void walkToAndPickup(RSArea area, String name) {
		if (area != null && !area.contains(Player.getRSPlayer().getPosition())) {
			General.println("Walking to area to pickup item: " + name);
			walkToArea(area);
		} else {
			RSGroundItem item = Methods.getGroundItem(name);
			if (item != null) {
				if (area.contains(item.getPosition())) {
					if (item.isOnScreen()) {
						if (item.click("Take")) {
							Timing.waitCondition(() -> InventoryEvent.contains(name), 3000);
						}
					} else {
						General.println("Turning camera to item: " + name);
						item.adjustCameraTo();
					}
				}
			}

		}
	}

	public static void walkToObjectWithAction(RSArea area, String name, String action) {
		if (area != null & !area.contains(Player.getRSPlayer().getPosition())) {
			General.println("Walking to area with Object: " + name + " with action: " + action);
			walkToArea(area);
		} else {
			RSObject object = Methods.getObjectWithAction(action, name, 5);
			if (object != null) {
				if (area.contains(object.getPosition())) {
					if (object.isOnScreen()) {
						if (object.click(action)) {
							Timing.waitCondition(() -> object == null, 3000);
						}
					} else {
						General.println("Rotating camera to object: " + name);
						object.adjustCameraTo();
					}
				}
			}
		}
	}

	public static void blindWalkToAreaHeal(RSArea area, HealEvent healEvent) {
		if (area != null && !area.contains(Player.getRSPlayer().getPosition())) {
			if (healEvent.canEat()) {
				General.println("Eating....");
				healEvent.eat();
			} else if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
				General.println("Activating run...");
				Options.setRunEnabled(true);
				Timing.waitCondition(() -> Game.isRunOn(), 3000);
			} else {
				General.println("Blind walking and healing to area");
				Walking.blindWalkTo(area.getRandomTile(), () -> healEvent.canEat(), 100);
			}
		}
	}

	public static void blindWalkToTileHeal(RSTile tile) {
		if (tile != null && tile.distanceTo(Player.getRSPlayer()) >= 3) {
			if (healEvent.canEat()) {
				General.println("Eating....");
				healEvent.eat();
			} else if (Game.getRunEnergy() > 20 && !Game.isRunOn()) {
				General.println("Activating run...");
				Options.setRunEnabled(true);
				Timing.waitCondition(() -> Game.isRunOn(), 3000);
			} else {
				General.println("Blind walking and healing to area");
				Walking.blindWalkTo(tile);
			}
		}
	}

	public static void triWalkToTile(RSTile tile) {
		if (tile != null && tile.distanceTo(Player.getRSPlayer().getPosition()) > 1) {
			General.println("Not close enough to tile");
			Walking.walkTo(tile);
		}
	}

	public static void blindWalkToTile(RSTile tile, RSArea area) {
		RSPlayer me = Player.getRSPlayer();
		if (tile != null && tile.distanceTo(Player.getRSPlayer().getPosition()) >= 2) {
			General.println("Walking Event: BlindWalkToTile: Not close enough to tile");
			Walking.blindWalkTo(tile, () -> tile.distanceTo(me) < 2 || area.contains(me), 1000);
		}
	}

	public static void blindWalkToTile(RSTile tile) {
		RSPlayer me = Player.getRSPlayer();
		if (tile != null && tile.distanceTo(me) >= 2) {
			General.println("Walking Event: BlindWalkToTile: Not close enough to tile");
			Walking.blindWalkTo(tile);
		}
	}

	public static boolean blindWalkToTileCondition(RSTile tile, int withinDistance, int checkDelay) {
		if (tile != null && tile.distanceTo(Player.getRSPlayer().getPosition()) > withinDistance) {
			General.println("Not close enough to tile");
			return Walking.blindWalkTo(tile, () -> Player.getPosition().distanceTo(tile) <= withinDistance, checkDelay);
		}
		return false;
	}

	@Override
	public void step() throws InterruptedException, IOException {
		// TODO Auto-generated method stub

	}

}
