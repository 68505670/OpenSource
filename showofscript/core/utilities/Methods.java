package scripts.core.utilities;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import scripts.core.inventory.InventoryEvent;
import scripts.dax_api.shared.helpers.InterfaceHelper;
import scripts.dax_api.walker_engine.WaitFor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Methods {

	public static RSNPC getNPC(String... name) {
		// "Looking for: " + name);
		RSNPC[] npcs = NPCs.findNearest(name);
		for (RSNPC npc : npcs) {
			if (npc != null) {
				return npc;
			}
		}
		return null;
	}

	public static RSNPC getNPC(Predicate<RSNPC> filter) {
		RSNPC[] npcs = NPCs.find(filter);
		for (RSNPC npc : npcs) {
			if (npc != null) {
				return npc;
			}
		}
		return null;
	}

	public static RSNPC[] getNPCs(Predicate<RSNPC> filter) {
		return NPCs.find(filter);
	}

	public static RSGroundItem getGroundItem(String name) {
		RSGroundItem[] items = GroundItems.find(name);
		for (RSGroundItem item : items) {
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	public static boolean pickupGroundItem(String name) {
		RSGroundItem items = Methods.getGroundItem(name);
		if (items != null) {
			if (items.isOnScreen()) {
				if (items.click("Take")) {
					Timing.waitCondition(() -> InventoryEvent.contains(name), 5000);
					return true;
				}
			} else {
				items.adjustCameraTo();
			}
		}
		return false;
	}

	public static RSObject getObject(int distance, String... name) {
		RSObject[] objects = Objects.findNearest(distance, name);
		for (RSObject object : objects) {
			if (object != null) {
				return object;
			}
		}
		return null;
	}

	public static RSObject getObjectAtTile(RSTile tile1, RSTile tile2, String name) {
		RSObject[] objects = Objects.getAllIn(tile1, tile2, i -> i.getDefinition().getName().contains(name));
		for (RSObject object : objects) {
			if (object != null) {
				return object;
			}
		}
		return null;
	}

	public static RSObject getObjectAtTileWithAction(RSTile tile1, RSTile tile2, String name, String action) {
		RSObject[] objects = Objects.getAllIn(tile1, tile2, i -> i.getDefinition().getName().contains(name));
		return Arrays.stream(objects).filter(i-> Arrays.stream(i.getDefinition().getActions()).filter(java.util.Objects::nonNull).anyMatch(d->d.contains(action))).findAny().get();
	}

	public static RSObject getObjectWithAction(String action, String name, int distance) {
		RSObject[] objects = Objects.find(distance, i -> i.getDefinition().getName().contains(name));
		return Arrays.stream(objects).filter(i-> Arrays.stream(i.getDefinition().getActions()).anyMatch(d->d.contains(action))).findFirst().get();
	}

	public static RSItem getInventoryItem(String name) {
		RSItem[] items = Inventory.find(name);
		return Arrays.stream(items).filter(java.util.Objects::nonNull).findFirst().get();
	}

	public static RSItem getInventoryPartName(String partName) {
		RSItem[] items = Inventory.find(i -> i.getDefinition().getName().contains(partName));
		return Arrays.stream(items).filter(java.util.Objects::nonNull).findFirst().get();
	}

	public static RSItem getEquipmentPartName(String partName) {
		RSItem[] items = Equipment.find(i -> i.getDefinition().getName().contains(partName));
		return Arrays.stream(items).filter(java.util.Objects::nonNull).findAny().get();
	}

	public static boolean useItemOnObject(String itemName, String objectName, int distance) {
		General.println("Using: " + itemName + " on " + objectName);
		RSItem item = getInventoryItem(itemName);
		RSObject object = getObject(objectName, distance);
		if (item != null && object != null) {
			 Camera.setCamera(270, 100);
			if (Game.getItemSelectionState() != 0 && !Game.getSelectedItemName().equals(itemName)) {
				General.println("Deselecting item");
				Mouse.click(1);
				Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 5000);
			} else if (item.click("Use")) {
				Timing.waitCondition(
						() -> Game.getSelectedItemName() != null && Game.getSelectedItemName().equals(itemName), 5000);
			}
		}
		return Game.getSelectedItemName().equals(itemName)
				&& DynamicClicking.clickRSObject(object,
						"Use " + itemName + " -> " + objectName);
	}

	public static boolean useItemOnObject(RSItem item, RSObject object) {
		General.println("Using: " + item.getDefinition().getName() + " on " + object.getDefinition().getName());
		if (object.isOnScreen()) {
			if (Game.getItemSelectionState() != 0
					&& !Game.getSelectedItemName().equals(item.getDefinition().getName())) {
				General.println("Deselecting item");
				Mouse.click(0);
				Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 5000);
			} else if (java.util.Objects.requireNonNull(getInventoryItem(item.getDefinition().getName())).click("Use")) {
				Timing.waitCondition(() -> Game.getSelectedItemName() != null
						&& Game.getSelectedItemName().equals(item.getDefinition().getName()), 5000);
			}
		}
		return Game.getSelectedItemName().equals(item.getDefinition().getName()) && DynamicClicking.clickRSObject(
				object, "Use " + item.getDefinition().getName() + " -> " + object.getDefinition().getName());
	}

	public static void useItemOnNPC(String itemName, String npcName) {
		General.println("Using: " + itemName + " on " + npcName);
		RSItem item = getInventoryItem(itemName);
		RSNPC npc = getNPC(npcName);
		if (item != null && npc != null) {
			if(!npc.isOnScreen()) {
				npc.adjustCameraTo();
			}else
			if (Game.getItemSelectionState() != 0 && !Game.getSelectedItemName().equals(itemName)) {
				General.println("Deselecting item");
				Mouse.click(1);
				Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 5000);
			} else if (item.click("Use")) {
				Timing.waitCondition(
						() -> Game.getSelectedItemName() != null && Game.getSelectedItemName().equals(itemName), 5000);
			} else if (Game.getSelectedItemName().equals(itemName)) {
				npc.click("Use " + itemName + " -> " + npcName);
			}
		}
	}

	public static boolean useItemOnItem(String itemName1, String itemName2) {
		General.println("Using: " + itemName1 + " on " + itemName2);
		if (getInventoryItem(itemName1) != null && getInventoryItem(itemName2) != null) {
			if (Game.getItemSelectionState() != 0 && !Game.getSelectedItemName().equals(itemName1)) {
				General.println("Deselecting item");
				Mouse.click(3);
				Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 5000);
			} else if (java.util.Objects.requireNonNull(getInventoryItem(itemName1)).click("Use")) {
				Timing.waitCondition(
						() -> Game.getSelectedItemName() != null && Game.getSelectedItemName().equals(itemName1), 5000);
			}
		}
		return Game.getSelectedItemName().equals(itemName1)
				&& java.util.Objects.requireNonNull(getInventoryItem(itemName2)).click("Use " + itemName1 + " -> " + itemName2);
	}

	public static boolean useItemWithAction(String itemName, String action) {
		RSItem item = getInventoryItem(itemName);
		if (item == null) {
			return false;
		}
		return item.click(action);
	}

	public static boolean useItemOnObject(int itemID, RSObject object) {
		General.println("Using: " + itemID + " on " + object.getDefinition().getName());
		if (getInventoryItem(itemID) != null && object.isOnScreen()) {
			if (Game.getItemSelectionState() != 0
					&& !Game.getSelectedItemName().equals(java.util.Objects.requireNonNull(getInventoryItem(itemID)).getDefinition().getName())) {
				General.println("Deselecting item");
				Mouse.click(3);
				Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 5000);
			} else if (java.util.Objects.requireNonNull(getInventoryItem(itemID)).click("Use")) {
				Timing.waitCondition(() -> Game.getSelectedItemName() != null
						&& Game.getSelectedItemName().equals(java.util.Objects.requireNonNull(getInventoryItem(itemID)).getDefinition().getName()), 5000);
			}
		}
		return Game.getSelectedItemName().equals(java.util.Objects.requireNonNull(getInventoryItem(itemID)).getDefinition().getName())
				&& DynamicClicking.clickRSObject(object, "Use " + java.util.Objects.requireNonNull(getInventoryItem(itemID)).getDefinition().getName()
						+ " -> " + object.getDefinition().getName());
	}

	private static RSItem getInventoryItem(int itemID) {
		RSItem[] items = Inventory.find(itemID);
		for (RSItem item : items) {
			if (item != null) {

				return item;
			}
		}
		return null;
	}

	public static boolean useItemOnNPC(int itemID, String npcName) {
		General.println("Using: " + itemID + " on " + npcName);
		if (getInventoryItem(itemID) != null && getNPC(npcName) != null) {
			if (Game.getItemSelectionState() != 0
					&& !Game.getSelectedItemName().equals(java.util.Objects.requireNonNull(getInventoryItem(itemID)).getDefinition().getName())) {
				General.println("Deselecting item");
				Mouse.click(3);
				Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 5000);
			} else if (java.util.Objects.requireNonNull(getInventoryItem(itemID)).click("Use")) {
				Timing.waitCondition(() -> Game.getSelectedItemName() != null
						&& Game.getSelectedItemName().equals(java.util.Objects.requireNonNull(getInventoryItem(itemID)).getDefinition().getName()), 5000);
			}
		}
		return getInventoryItem(itemID) != null
				&& Game.getSelectedItemName().equals(java.util.Objects.requireNonNull(getInventoryItem(itemID)).getDefinition().getName())
				&& java.util.Objects.requireNonNull(getNPC(npcName))
						.click("Use " + java.util.Objects.requireNonNull(getInventoryItem(itemID)).getDefinition().getName() + " -> " + npcName);
	}

	public static boolean useItemOnObject(String itemName, RSObject object) {
		General.println("Using: " + itemName + " on " + object.getDefinition().getName());
		if(!object.isOnScreen()) {
			object.adjustCameraTo();
		}else
		if (getInventoryItem(itemName) != null && object.isOnScreen()) {
			if (Game.getItemSelectionState() != 0 && !Game.getSelectedItemName().equals(itemName)) {
				General.println("Deselecting item");
				Mouse.click(3);
				Timing.waitCondition(() -> Game.getItemSelectionState() == 0, 5000);
			} else if (java.util.Objects.requireNonNull(getInventoryItem(itemName)).click("Use")) {
				Timing.waitCondition(
						() -> Game.getSelectedItemName() != null && Game.getSelectedItemName().equals(itemName), 5000);
			}
		}
		return Game.getSelectedItemName().equals(itemName)
				&& DynamicClicking.clickRSObject(object, "Use " + itemName + " -> " + object.getDefinition().getName());
	}

	public static RSObject getObject(String name, int distance) {
		RSObject[] objects = Objects.findNearest(distance, name);
		for (RSObject object : objects) {
			if (object != null) {
				return object;
			}
		}
		return null;
	}

	public static boolean hasAction(RSObject object, String action) {
		if (object != null) {
			String[] actions = object.getDefinition().getActions();
			for (String string : actions) {
				return java.util.Objects.equals(action, string);
			}
		}
		return false;
	}

	public static boolean isPoisoned() {
		return Game.getSetting(102) > 0;
	}

	public static void useItemWithAction(RSItem item, String action) {
		if (item == null) {
			return;
		}
		item.click(action);
	}

	public static RSObject getObject(int id, int distance) {
		RSObject[] objects = Objects.find(distance, id);
		for (RSObject object : objects) {
			if (object != null) {
				return object;
			}
		}
		return null;
	}

	protected RSInterface getClickHereToContinue() {
		List<RSInterface> list = getConversationDetails();
		if (list == null) {
			return null;
		}
		Optional<RSInterface> optional = list.stream()
				.filter(rsInterface -> rsInterface.getText().equals("Click here to continue")).findAny();
		return optional.orElse(null);
	}

	private static void waitForNextOption() {
		List<String> interfaces = getAllInterfaces().stream().map(RSInterface::getText).collect(Collectors.toList());
		WaitFor.condition(5000, () -> {
			if (!interfaces
					.equals(getAllInterfaces().stream().map(RSInterface::getText).collect(Collectors.toList()))) {
				return WaitFor.Return.SUCCESS;
			}
			return WaitFor.Return.IGNORE;
		});
	}

	public static void waitForConversationWindow() {
		RSPlayer player = Player.getRSPlayer();
		RSCharacter rsCharacter = null;
		if (player != null) {
			rsCharacter = player.getInteractingCharacter();
		}
		WaitFor.condition(rsCharacter != null ? WaitFor.getMovementRandomSleep(rsCharacter) : 10000, () -> {
			if (isConversationWindowUp()) {
				return WaitFor.Return.SUCCESS;
			}
			return WaitFor.Return.IGNORE;
		});
	}

	protected static void clickHereToContinue() {
		General.println("Clicking continue.");
		Keyboard.typeKeys(' ');
		waitForNextOption();
	}

	private static List<RSInterface> getConversationDetails() {
		for (int window : ALL_WINDOWS) {
			List<RSInterface> details = InterfaceHelper.getAllInterfaces(window).stream().filter(rsInterfaceChild -> {
				if (rsInterfaceChild.getTextureID() != -1) {
					return false;
				}
				String text = rsInterfaceChild.getText();
				return text != null && text.length() > 0;
			}).collect(Collectors.toList());
			if (details.size() > 0) {
				General.println("Conversation Options: ["
						+ details.stream().map(RSInterface::getText).collect(Collectors.joining(", ")) + "]");
				return details;
			}
		}
		return null;
	}

	private List<RSInterface> getAllOptions(String... options) {
		final List<String> optionList = Arrays.stream(options).map(String::toLowerCase).collect(Collectors.toList());
		List<RSInterface> list = getConversationDetails();
		return list != null
				? list.stream().filter(rsInterface -> optionList.contains(rsInterface.getText().trim().toLowerCase()))
						.collect(Collectors.toList())
				: null;
	}

	public void handleChatV2(String... options) {
		General.println("Handling... " + Arrays.asList(options));
		List<String> blackList = new ArrayList<>();
		int limit = 0;
		while (limit++ < 50) {
			if (WaitFor.condition(General.random(650, 800), () -> isConversationWindowUp() ? WaitFor.Return.SUCCESS
					: WaitFor.Return.IGNORE) != WaitFor.Return.SUCCESS) {
				General.println("Conversation window not up.");
				break;
			}

			if (getClickHereToContinue() != null) {
				clickHereToContinue();
				limit = 0;
				continue;
			}

			List<RSInterface> selectableOptions = getAllOptions(options);
			if (selectableOptions == null || selectableOptions.size() == 0) {
				WaitFor.milliseconds(150);
				continue;
			}

			for (RSInterface selected : selectableOptions) {
				if (blackList.contains(selected.getText())) {
					continue;
				}
				General.sleep(General.randomSD(350, 2250, 775, 350));
				General.println("Replying with option: " + selected.getText());
				blackList.add(selected.getText());
				Keyboard.typeString(selected.getIndex() + "");
				waitForNextOption();
				limit = 0;
				break;
			}
			General.sleep(10, 20);
		}
		if (limit > 50) {
			General.println("Reached conversation limit.");
		}
	}

	private final static int ITEM_ACTION_INTERFACE_WINDOW = 193;
	private final static int NPC_TALKING_INTERFACE_WINDOW = 231;
	private final static int PLAYER_TALKING_INTERFACE_WINDOW = 217;
	private static final int SELECT_AN_OPTION_INTERFACE_WINDOW = 219;
	private final static int SINGLE_OPTION_DIALOGUE_WINDOW = 229;

	private final static int[] ALL_WINDOWS = new int[] { ITEM_ACTION_INTERFACE_WINDOW, NPC_TALKING_INTERFACE_WINDOW,
			PLAYER_TALKING_INTERFACE_WINDOW, SELECT_AN_OPTION_INTERFACE_WINDOW, SINGLE_OPTION_DIALOGUE_WINDOW };

	public static boolean talkToV2(String name, RSArea area) {
		if (area != null && !area.contains(Player.getRSPlayer().getPosition())) {
			General.println("Walking to NPC: " + name);
			Walker.walkToArea(area);
		} else {
			RSNPC npc = Methods.getNPC(name);
			if (npc != null) {
				if (npc.isOnScreen()) {
					if (DynamicClicking.clickRSNPC(npc, "Talk-to")) {
						waitForConversationWindow();
					}
				} else {
					General.println("Rotating Camera to npc: " + npc);
					npc.adjustCameraTo();
				}
			}
		}
		return isConversationWindowUp();
	}

	public static boolean isConversationWindowUp() {
		return Arrays.stream(ALL_WINDOWS).anyMatch(Interfaces::isInterfaceValid);
	}

	private static List<RSInterface> getAllInterfaces() {
		ArrayList<RSInterface> interfaces = new ArrayList<>();
		for (int window : ALL_WINDOWS) {
			interfaces.addAll(InterfaceHelper.getAllInterfaces(window));
		}
		return interfaces;
	}

}
