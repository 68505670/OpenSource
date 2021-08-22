package scripts.wastedbro.api.rsitem_services.grand_exchange_api;

import com.allatori.annotations.DoNotRename;
import lombok.Data;

@Data
@DoNotRename
class PriceTrend {

    @DoNotRename
    private String trend;

    @DoNotRename
    private String price;

    public String getPrice() {
        return price;
    }

}
