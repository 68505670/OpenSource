package scripts.core.cannonballs;

import org.tribot.api.General;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting;
import scripts.core.gui.GUI;
import scripts.core.paint.PaintInfo;
import scripts.core.paint.Painter;
import scripts.dax_api.api_lib.WebWalkerServerApi;
import scripts.dax_api.api_lib.models.DaxCredentials;
import scripts.dax_api.api_lib.models.DaxCredentialsProvider;
import scripts.wastedbro.api.rsitem_services.RsItemPriceService;
import scripts.wastedbro.api.rsitem_services.grand_exchange_api.GrandExchangePriceService;
import scripts.wastedbro.api.rsitem_services.runelite.RuneLitePriceService;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@ScriptManifest(name = "Whipz Cannon Baller", category = "Money Making", authors = {"Whipz"})
public class WhipzCannonBaller extends Script implements Painting, PaintInfo, MessageListening07 {

    public static int cannonBall;
    public static int restockAmount;
    public static boolean dwarfCannonQuest, knightSwordQuest, doricQuest, levelTask;
    public static double sellPrice, buyPrice;
    static RsItemPriceService prices;
    static long startTime;
    final Painter corePaint = new Painter(this, Painter.PaintLocations.TOP_LEFT_PLAY_SCREEN,
            new Color[]{new Color(255, 251, 255)}, "Trebuchet MS", new Color[]{new Color(93, 156, 236, 255)},
            new Color[]{new Color(39, 95, 175)}, 1, false, 5, 3, 0);
    WhipzCannonGUIController controller;
    private URL fxml;
    private GUI gui;

    public static long getCannonBallPH() {
        return (long) (cannonBall / ((System.currentTimeMillis() - startTime) / 3600000.0D));
    }

    @Override
    public void run() {
        prices = new RsItemPriceService.Builder()
                .addPriceService(new RuneLitePriceService())
                .addPriceService(new GrandExchangePriceService())
                .build();
        startTime = System.currentTimeMillis();
        try {
            fxml = new URL("https://raw.githubusercontent.com/Whipz/guis/main/WhipzCannonBallerGUI.fxml");
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
        WebWalkerServerApi.getInstance().setDaxCredentialsProvider(new DaxCredentialsProvider() {
            @Override
            public DaxCredentials getDaxCredentials() {
                return new DaxCredentials("sub_DPjXXzL5DeSiPf", "PUBLIC-KEY");
            }
        });
        while (true) {
            try {
                new WhipzCannonBallerEvent(this).execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isQuesting() {
       return dwarfCannonQuest|| knightSwordQuest|| doricQuest;
    }

    @Override
    public String[] getPaintInfo() {

        return new String[]{
                "Whipz CannonBaller",
                "Time Ran: " + corePaint.getRuntimeString(),
                "CannonBalls: " + cannonBall,
                "CannonBalls P/H: " + getCannonBallPH(),
                "Restocking: " + true,
                "Questing: " + isQuesting()
        };
    }

    public void serverMessageReceived(String message) {
        if (message.contains("to form 4")) {
            cannonBall = cannonBall + 4;
        }
    }

    @Override
    public void onPaint(Graphics g) {
        corePaint.paint(g);
    }
}
