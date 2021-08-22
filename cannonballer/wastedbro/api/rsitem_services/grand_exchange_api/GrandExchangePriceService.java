package scripts.wastedbro.api.rsitem_services.grand_exchange_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Setter;
import org.tribot.api.General;
import scripts.wastedbro.api.rsitem_services.IRsItemPriceService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Optional;

public class GrandExchangePriceService implements IRsItemPriceService {

    private static final int CONNECTION_TIMEOUT = 5000;
    private static final String URl = "https://secure.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item=";
    private static final String ITEM_PROPERTY = "item";

    private final HashMap<Integer, ExchangeItem> itemCache;

    @Setter
    private boolean shouldCache;

    public GrandExchangePriceService() {
        this(true);
    }

    public GrandExchangePriceService(boolean shouldCache) {
        this.shouldCache = shouldCache;
        this.itemCache = new HashMap<>();
    }

    public Optional<ExchangeItem> tryGetItemData(int itemId) {
        if (shouldCache && itemCache.containsKey(itemId)) {
            return Optional.of(itemCache.get(itemId));
        }
        Reader reader;
        try {
            Gson gson = new GsonBuilder().create();
            URLConnection urlConnection = new URL(URl + itemId).openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();
            Optional<ExchangeItem> item = Optional
                    .ofNullable(gson.fromJson(data.get(ITEM_PROPERTY), ExchangeItem.class));
            if (shouldCache && item.isPresent()) {
                itemCache.put(itemId, item.get());
            }
            reader.close();

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            General.println("Could not load RSItem via Grand Exchange API");
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getPrice(int itemId) {
        Optional<ExchangeItem> item = tryGetItemData(itemId);
        return item.map(ExchangeItem::getCurrentPrice);
    }

    @Override
    public Optional<String> getName(int itemId) {
        Optional<ExchangeItem> item = tryGetItemData(itemId);
        return item.map(ExchangeItem::getName);
    }

    @Override
    public Optional<Boolean> isMembers(int itemId) {
        Optional<ExchangeItem> item = tryGetItemData(itemId);
        return item.map(ExchangeItem::isMembers);
    }

}
