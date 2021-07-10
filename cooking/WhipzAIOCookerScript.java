package scripts.core.skills.cooking;

import org.tribot.api.General;
import org.tribot.api2007.Skills;
import org.tribot.api2007.types.RSArea;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Ending;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.Starting;
import scripts.core.gui.GUI;
import scripts.core.paint.PaintInfo;
import scripts.core.paint.Painter;
import scripts.dax_api.api_lib.WebWalkerServerApi;
import scripts.dax_api.api_lib.models.DaxCredentials;
import scripts.dax_api.api_lib.models.DaxCredentialsProvider;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;


@ScriptManifest(category = "Cooking", authors = {"Whipz"}, name = "Whipz AIO Cooker")
public class WhipzAIOCookerScript extends Script implements PaintInfo, Painting, Starting, Ending, MessageListening07 {

    WhipzCookerGUIController guiController;

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    public static String choosenFood;
    public static String rawNameFood;
    public static boolean makeWines;
    public static boolean makeBread;
    public static boolean makePlainPizza;
    public static boolean makePotatoButter;
    public static boolean makePotatoCheese;
    public static boolean makeMeatPizza;
    public static boolean makePineapplePizza;
    public static boolean makeTunaPotato;
    static RSArea area;
    static long startTime;
    private static String status;

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    final Painter corePaint = new Painter(this, Painter.PaintLocations.INVENTORY_AREA,
            new Color[]{new Color(255, 251, 255)}, "Trebuchet MS", new Color[]{new Color(93, 156, 236, 127)},
            new Color[]{new Color(39, 95, 175)}, 1, false, 5, 3, 0);

    // static RsItemPriceService priceService;
    private URL fxml;
    private GUI gui;

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public static String format(long value) {
        // Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE)
            return format(Long.MIN_VALUE + 1);
        if (value < 0)
            return "-" + format(-value);
        if (value < 1000)
            return Long.toString(value); // deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); // the number part of the
        // output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public String getStatus() {
        return status;
    }

    public static void setStatus(String message) {
        status = message;
        General.println(message);
    }


    @Override
    public void run() {

        try {
            fxml = new URL("https://raw.githubusercontent.com/Whipz/guis/main/WhipzAIOCooker.fxml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (fxml != null) {
            General.println("Trying to open gui");
            gui = new GUI(fxml);
            gui.show();
        } else {
            General.println("fxml is null");
        }
        while (gui.isOpen()) {
            sleep(500);
        }

        startTime = System.currentTimeMillis();
        startXP = Skills.getXP(Skills.SKILLS.COOKING);
        WebWalkerServerApi.getInstance().setDaxCredentialsProvider(new DaxCredentialsProvider() {
            @Override
            public DaxCredentials getDaxCredentials() {
                return new DaxCredentials("sub_DPjXXzL5DeSiPf", "PUBLIC-KEY");
            }
        });

        while (true) {
            try {
                if (makeWines) {
                    new WhipzAIOCookerWineEvent(this).execute();
                } else if (makeBread) {
                    new WhipzAIOCookerBreadEvent(this).execute();
                } else if (makePlainPizza) {
                    new WhipzAIOCookerPlainPizzaEvent(this).execute();
                } else if (makePotatoButter) {
                    new WhipzAIOCookerPotatoButterEvent(this).execute();
                } else if (makePotatoCheese) {
                    new WhipzAIOCookerPotatoCheeseEvent(this).execute();
                } else if (makeMeatPizza) {
                    new WhipzAIOCookerMeatPizzaEvent(this).execute();
                } else if (makePineapplePizza) {
                    new WhipzAIOCookerPineapplePizzaEvent(this).execute();
                } else if (makeTunaPotato) {
                    new WhipzAIOCookerTunaPotatoEvent(this).execute();
                } else {
                    new WhipzCookerEvent(this, choosenFood, area).execute();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static int cooked;
    public int startXP;
    public int levelGained;
    public int burnt;

    @Override
    public void serverMessageReceived(String m) {
        if(m.contains("successfully")) {
            cooked++;
        } else if(m.contains("Congratulations")) {
            levelGained++;
        } else if(m.contains("burn")) {
            burnt++;
        }
    }

    @Override
    public void onEnd() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPaint(Graphics g) {
        corePaint.paint(g);
    }

    @Override
    public String[] getPaintInfo() {
        return new String[]{"Whipz AIO Cooker", "Time Running: " + corePaint.getRuntimeString(), "Cooking: " + choosenFood, "Level: "+ Skills.SKILLS.COOKING.getActualLevel(), "Items cooked: "+WhipzAIOCookerScript.cooked, "Levels Gained: "+levelGained, "XP Gained: " +(Skills.SKILLS.COOKING.getXP() - startXP), "Burnt items: "+burnt};
    }
}
