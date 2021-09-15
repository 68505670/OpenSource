package scripts.core.botevent;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.input.Keyboard;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Login;
import org.tribot.api2007.Login.STATE;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import scripts.core.utilities.Methods;
import scripts.core.utilities.Walker;
import scripts.dax_api.shared.helpers.InterfaceHelper;
import scripts.dax_api.walker_engine.WaitFor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class BotEvent {

    private final static int ITEM_ACTION_INTERFACE_WINDOW = 193;
    private final static int NPC_TALKING_INTERFACE_WINDOW = 231;
    private final static int PLAYER_TALKING_INTERFACE_WINDOW = 217;
    private static final int SELECT_AN_OPTION_INTERFACE_WINDOW = 219;
    private final static int SINGLE_OPTION_DIALOGUE_WINDOW = 229;
    private final static int[] ALL_WINDOWS = new int[]{ITEM_ACTION_INTERFACE_WINDOW, NPC_TALKING_INTERFACE_WINDOW,
            PLAYER_TALKING_INTERFACE_WINDOW, SELECT_AN_OPTION_INTERFACE_WINDOW, SINGLE_OPTION_DIALOGUE_WINDOW};
    private final ArrayList<Supplier<Boolean>> conditionList = new ArrayList<>();
    protected Supplier<Boolean> interruptCondition;
    protected Script script;
    private boolean complete = false;
    private boolean failed = true;

    public BotEvent(Script script) {
        super();
        this.script = script;
    }

    public BotEvent setInterruptCondition(Supplier<Boolean> interuptCondition) {
        this.interruptCondition = interuptCondition;
        return this;
    }

    public BotEvent reset() {
        this.complete = false;
        this.failed = true;
        return this;
    }

    public abstract void step() throws InterruptedException, IOException;

    public void setComplete() {
        this.complete = true;
        this.failed = false;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isFailed() {
        return failed;
    }

    public void execute() throws InterruptedException, IOException {
        executed();
    }

    public boolean executed() throws InterruptedException, IOException {
        while (!isComplete() && script.isActive() && !script.isPaused()) {
            if (Login.getLoginState().equals(STATE.INGAME)) {
                for (Supplier<Boolean> condition : conditionList) {
                    if (!condition.get()) {
                        setComplete();
                        return true;
                    }
                }
                if (interruptCondition != null && interruptCondition.get()) {
                    setComplete();
                } else if (!script.isPaused()) {
                    step();
                }
            }
        }
        return isComplete() && !isFailed();
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

    private void waitForNextOption() {
        List<String> interfaces = getAllInterfaces().stream().map(RSInterface::getText).collect(Collectors.toList());
        WaitFor.condition(2500, () -> {
            if (!interfaces
                    .equals(getAllInterfaces().stream().map(RSInterface::getText).collect(Collectors.toList()))) {
                return WaitFor.Return.SUCCESS;
            }
            return WaitFor.Return.IGNORE;
        });
    }

    public void waitForConversationWindow() {
        RSPlayer player = Player.getRSPlayer();
        RSCharacter rsCharacter = null;
        if (player != null) {
            rsCharacter = player.getInteractingCharacter();
        }
        WaitFor.condition(rsCharacter != null ? WaitFor.getMovementRandomSleep(rsCharacter) : 5000, () -> {
            if (isConversationWindowUp()) {
                return WaitFor.Return.SUCCESS;
            }
            return WaitFor.Return.IGNORE;
        });
    }

    protected void clickHereToContinue() {
        General.println("Clicking continue.");
        Keyboard.typeKeys(' ');
        waitForNextOption();
    }

    private List<RSInterface> getConversationDetails() {
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

    public boolean talkToV2(String name, RSArea area) {
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

    public boolean isConversationWindowUp() {
        return Arrays.stream(ALL_WINDOWS).anyMatch(Interfaces::isInterfaceValid);
    }

    private List<RSInterface> getAllInterfaces() {
        ArrayList<RSInterface> interfaces = new ArrayList<>();
        for (int window : ALL_WINDOWS) {
            interfaces.addAll(InterfaceHelper.getAllInterfaces(window));
        }
        return interfaces;
    }

}
