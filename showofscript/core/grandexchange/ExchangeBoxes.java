package scripts.core.grandexchange;

import org.tribot.api2007.Interfaces;
import org.tribot.api2007.types.RSInterface;

public class ExchangeBoxes {

    public static RSInterface Box1 = Interfaces.get(465, 7);
    public static RSInterface Box2 = Interfaces.get(465, 8);
    public static RSInterface Box3 = Interfaces.get(465, 9);
    public static RSInterface Box4 = Interfaces.get(465, 10);
    public static RSInterface Box5 = Interfaces.get(465, 11);
    public static RSInterface Box6 = Interfaces.get(465, 12);
    public static RSInterface Box7 = Interfaces.get(465, 13);
    public static RSInterface Box8 = Interfaces.get(465, 14);

    public static int buyChild = 3;
    public int sellChild = 4;

    //Actions Sell: Create <col=ff9040>Sell</col> offer
    //Actions Buy: Create <col=ff9040>Buy</col> offer

    public int itemTextChild = 19;

    public int offerTypeChild = 16;

    static RSInterface[] allBoxes = new RSInterface[]{Box1, Box2, Box3, Box4, Box5, Box6, Box7, Box8};

    public static RSInterface searchBar = Interfaces.get(162, 42);

    // <col=000000> What would you like to buy?</col> blah*

    public static RSInterface itemList = Interfaces.get(162, 50);

    //<col=ff9040>Item Name</col>




}
