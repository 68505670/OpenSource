package scripts.core.banking;

import java.util.HashMap;

public class BankCache {

    static HashMap<String, BankCache> bankCacheHashMap = new HashMap<String, BankCache>();

    public String name;
    public int id;
    public int qty;

    public BankCache(String itemName, int id, int qty) {
        this.name = itemName;
        this.id = id;
        this.qty = qty;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getQty() {
        return qty;
    }


}
