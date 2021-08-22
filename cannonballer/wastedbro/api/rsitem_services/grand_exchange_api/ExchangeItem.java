package scripts.wastedbro.api.rsitem_services.grand_exchange_api;

import com.allatori.annotations.DoNotRename;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@DoNotRename
class ExchangeItem {

    private static final int K = 1000;
    private static final int M = 1000000;
    private static final int B = 1000000000;

    @DoNotRename
    private int id;

    @DoNotRename
    private String icon;

    @DoNotRename
    @SerializedName("icon_large")
    private String iconLarge;

    @DoNotRename
    private String type;

    @DoNotRename
    private String typeIcon;

    @DoNotRename
    private String name;

    @DoNotRename
    private String description;

    @DoNotRename
    private String members;

    @DoNotRename
    private PriceTrend today;

    @DoNotRename
    private PriceTrend current;

    @DoNotRename
    private TrendChange day30;

    @DoNotRename
    private TrendChange day90;

    @DoNotRename
    private TrendChange day180;

    public String getName() {
        return name;
    }

    public int getCurrentPrice() {
        return current == null ? -1 : fromKmb(current.getPrice());
    }

    public boolean isMembers() {
        return Boolean.parseBoolean(members);
    }

    private int fromKmb(String number) {
        int multiplier = 1;
        number = number.toLowerCase().trim();
        if (number.endsWith("k")) {
            multiplier = K;
        } else if (number.endsWith("m")) {
            multiplier = M;
        } else if (number.endsWith("b")) {
            multiplier = B;
        }
        if (multiplier != 1) {
            number = number.substring(0, number.length() - 1);
        }
        try {
            return (int) (Float.parseFloat(number) * multiplier);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

}
