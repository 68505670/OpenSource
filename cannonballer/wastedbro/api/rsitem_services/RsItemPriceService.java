package scripts.wastedbro.api.rsitem_services;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Provides a helper class for creating price services that utilize multiple sources.
 * <p>
 * Includes functionality to return proper values for "Coins" despite them not being a part of the Grand Exchange
 */
public class RsItemPriceService implements IRsItemPriceService {

    private final ArrayList<IRsItemPriceService> priceServices;
    private boolean isDebugging;

    public RsItemPriceService() {
        this.priceServices = new ArrayList<>();
        this.isDebugging = false;
    }

    /**
     * Attempts to grab the price of the item by checking each price service in the order they were added.
     * Once the price is found, no further service is checked.
     *
     * @param itemId The item ID to lookup
     * @return The price of the item found. If the item can't be found in any of the services, an empty Optional is returned.
     */
    @Override
    public Optional<Integer> getPrice(int itemId) {
        if (itemId == 995) { // Coins
            return Optional.of(1);
        }
        for (IRsItemPriceService service : priceServices) {
            log("Attempting to get price of [" + itemId + "] from: " + service.getClass().getName());
            Optional<Integer> price = service.getPrice(itemId);
            if (price.isPresent()) {
                log("Successfully retrieved price!");
                return price;
            } else {
                log("Failed to find price from: " + service.getClass().getName());
            }
        }
        return Optional.empty();
    }

    /**
     * Attempts to grab the name of the item by checking each price service in the order they were added.
     * Once the name is found, no further service is checked.
     *
     * @param itemId The item ID to lookup
     * @return The name of the item found. If the item can't be found in any of the services, an empty Optional is returned.
     */
    @Override
    public Optional<String> getName(int itemId) {
        if (itemId == 995) { // Coins
            return Optional.of("Coins");
        }
        for (IRsItemPriceService service : priceServices) {
            log("Attempting to get name of [" + itemId + "] from: " + service.getClass().getName());
            Optional<String> name = service.getName(itemId);
            if (name.isPresent()) {
                log("Successfully retrieved name!");
                return name;
            } else {
                log("Failed to find name from: " + service.getClass().getName());
            }

        }
        return Optional.empty();
    }

    /**
     * Attempts to grab the members status of the item by checking each price service in the order they were added.
     * Once the members status is found, no further service is checked.
     *
     * @param itemId The item ID to lookup
     * @return The members status of the item found. If the item can't be found in any of the services, an empty Optional is returned.
     */
    @Override
    public Optional<Boolean> isMembers(int itemId) {
        if (itemId == 995) { // Coins
            return Optional.of(false);
        }
        for (IRsItemPriceService service : priceServices) {
            log("Attempting to get Member Status of [" + itemId + "] from: " + service.getClass().getName());
            Optional<Boolean> members = service.isMembers(itemId);
            if (members.isPresent()) {
                log("Successfully retrieved members status!");
                return members;
            } else {
                log("Failed to find members status from: " + service.getClass().getName());
            }

        }
        return Optional.empty();
    }

    private void log(String s) {
        if (isDebugging) {
            System.out.println(s);
        }
    }

    public static class Builder {

        private final RsItemPriceService priceService = new RsItemPriceService();

        public Builder addPriceService(IRsItemPriceService service) {
            priceService.priceServices.add(service);
            return this;
        }

        public Builder setDebugging(boolean isDebugging) {
            priceService.isDebugging = isDebugging;
            return this;
        }

        public RsItemPriceService build() {
            return this.priceService;
        }

    }

}
