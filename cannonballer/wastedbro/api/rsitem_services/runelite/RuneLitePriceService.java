package scripts.wastedbro.api.rsitem_services.runelite;

import com.google.gson.*;
import org.tribot.util.Util;
import scripts.wastedbro.api.rsitem_services.CacheablePriceService;
import scripts.wastedbro.api.rsitem_services.IRsItemPriceService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class RuneLitePriceService extends CacheablePriceService<RuneLiteItem> implements IRsItemPriceService {

    private static final String URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
    private static final String CACHE_FILEPATH = Util.getWorkingDirectory() + File.separator + "cache" + File.separator
            + "RuneLitePricesCache.json";
    private static final String DATA_PROPERTY = "data";

    public RuneLitePriceService() {
        super(URL, CACHE_FILEPATH);
        updateCacheIfNecessary();
    }

    @Override
    protected void parseCache() throws IOException {
        Gson gson = new GsonBuilder().create();
        JsonObject data = JsonParser.parseReader(new FileReader(CACHE_FILEPATH)).getAsJsonObject()
                .getAsJsonObject(DATA_PROPERTY);
        for (Map.Entry<String, JsonElement> item : data.entrySet()) {
            try {
                int id = Integer.parseInt(item.getKey());
                RuneLiteItem runeLiteItem = gson.fromJson(item.getValue(), RuneLiteItem.class);
                dataCache.put(id, runeLiteItem);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public Optional<Integer> getPrice(int itemId) {
        return tryGetItemData(itemId).map(RuneLiteItem::getHigh);
    }

    @Override
    public Optional<String> getName(int itemId) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isMembers(int itemId) {
        return Optional.empty();
    }

}
