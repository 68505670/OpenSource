package scripts.core.grandexchange;

public class GrandExchangeItem {

    public final String itemName;
    public final int price;
    public int qty;
    public int id;

    public GrandExchangeItem(String itemName, int id, int qty, int price) {
        this.itemName = itemName;
        this.id = id;
        this.qty = qty;
        this.price = price;
    }

    public GrandExchangeItem(String itemName, int qty, int price) {
        this.itemName = itemName;
        this.qty = qty;
        this.price = price;
    }

    public GrandExchangeItem(String itemName, int price) {
        this.itemName = itemName;
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQty() {
        return qty;
    }

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }
}
