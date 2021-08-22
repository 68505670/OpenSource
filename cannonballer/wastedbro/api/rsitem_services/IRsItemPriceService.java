package scripts.wastedbro.api.rsitem_services;

import java.util.Optional;

/**
 * Provides an interface for classes that can fetch RSItem pricing data
 */
public interface IRsItemPriceService {

    Optional<Integer> getPrice(int itemId);

    Optional<String> getName(int itemId);

    Optional<Boolean> isMembers(int itemId);

}
