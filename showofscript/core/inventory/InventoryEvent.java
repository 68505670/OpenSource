package scripts.core.inventory;

import org.tribot.api.General;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;
import org.tribot.script.Script;
import scripts.core.botevent.BotEvent;

import java.io.IOException;
import java.util.Arrays;

public class InventoryEvent extends BotEvent {

	public InventoryEvent(Script script) {
		super(script);
	}

	public static RSItem getInventoryItem(String name) {
		RSItem[] items = Inventory.find(name);
		for (RSItem item : items) {
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	public static int getItemCount(String name) {
		RSItem[] items = Inventory.find(name);
		int count = 0;
		for (RSItem item : items) {
			if (item != null) {
				if (item.getDefinition().getUnnotedItemID() == item.getID()) {
					count++;
				}
			}
		}
		return count;
	}

	public static RSItem getInventoryItem(int id) {
		RSItem[] items = Inventory.find(id);
		for (RSItem item : items) {
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	public static boolean contains(String name) {
		return Inventory.find(name).length > 0;
	}

	public static boolean containsPartName(String partName) {
		return Inventory.find(i -> i.getDefinition().getName().toLowerCase().contains(partName)).length > 0;
	}

	public static boolean containsOnly(String... names) {
		return Arrays.stream(Inventory.getAll())
				.allMatch(i -> Arrays.asList(names).contains(i.getDefinition().getName()));
	}

	public static int getStackedCount(String name) {
		int count = 0;
		RSItem items = getInventoryItem(name);
		if (contains(name)) {
			assert items != null;
			count = items.getStack();
			General.println(name + " " + count);
		}
		return count;
	}

	public static int getCount(String name) {
		RSItem[] items = Inventory.find(name);
		int count = 0;
		if (contains(name)) {
			count = items.length;
		}
		return count;
	}

	@Override
	public void step() throws InterruptedException, IOException {
		// TODO Auto-generated method stub

	}

	public static boolean contains(RSItem item) {
		if (item != null) {
			return Inventory.find(item.getDefinition().getName()).length > 0;
		}
		return false;
	}

	public static boolean contains(int id) {
		RSItem[] items = Inventory.find(id);
		for (RSItem item : items) {
			return item != null;
		}
		return false;
	}

	public static int getStackedCount(RSItem wItem) {
		int count = 0;
		if (contains(wItem)) {
			count = wItem.getStack();
			General.println(wItem.getDefinition().getName() + " " + count);
		}
		return count;
	}

	public static int getCount(RSItem wItem) {
		RSItem[] items = Inventory.find(wItem.getID());
		int count = 0;
		if (contains(wItem.getID())) {
			count = items.length;
		}
		return count;
	}

}
