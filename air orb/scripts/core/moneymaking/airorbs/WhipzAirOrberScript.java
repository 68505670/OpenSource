package scripts.core.moneymaking.airorbs;

import org.tribot.api.General;
import org.tribot.api2007.Skills;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Ending;
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
import java.util.Optional;
import java.util.TreeMap;

@ScriptManifest(category = "Money Making", authors = {"Whipz"}, name = "Whipz Air Orber")
public class WhipzAirOrberScript extends Script implements PaintInfo, Painting, Starting, Ending {

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    public static boolean useStamina;
    public static boolean useGlory;
    public static int eatFoodAt;
    public static String foodFoodType;
    public static Optional<Integer> airOrb, unpowered, cosmic;
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

    // static RsItemPriceService priceService;

    final Painter corePaint = new Painter(this, Painter.PaintLocations.INVENTORY_AREA,
            new Color[]{new Color(255, 251, 255)}, "Trebuchet MS", new Color[]{new Color(93, 156, 236, 127)},
            new Color[]{new Color(39, 95, 175)}, 1, false, 5, 3, 0);
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
            fxml = new URL("https://raw.githubusercontent.com/Whipz/guis/main/WhipzAirOrbGui.fxml");
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
        WhipzAirOrberEvent.startXP = Skills.getXP(Skills.SKILLS.MAGIC);

        WebWalkerServerApi.getInstance().setDaxCredentialsProvider(new DaxCredentialsProvider() {
            @Override
            public DaxCredentials getDaxCredentials() {
                return new DaxCredentials("sub_DPjXXzL5DeSiPf", "PUBLIC-KEY");
            }
        });

        WhipzAirOrberScript.setStatus("GUI Closed");
        while (true) {
            try {
                WhipzAirOrberScript.setStatus("Starting Script");
                new WhipzAirOrberEvent(this).execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        return new String[]{"Whipz Air Orbs", "Time Running: " + corePaint.getRuntimeString(), "Status: " + this.getStatus(), "Orbs Made: " + (Skills.getXP(Skills.SKILLS.MAGIC) - WhipzAirOrberEvent.startXP) / 76};
    }
}
