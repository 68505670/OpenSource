package scripts.wastedbro.api.rsitem_services.runelite;

import com.allatori.annotations.DoNotRename;
import lombok.Data;

@Data
@DoNotRename
public class RuneLiteItem {

    @DoNotRename
    private int high;

    @DoNotRename
    private long highTime;

    @DoNotRename
    private int low;

    @DoNotRename
    private long lowTime;

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public long getHighTime() {
        return highTime;
    }

    public void setHighTime(long highTime) {
        this.highTime = highTime;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public long getLowTime() {
        return lowTime;
    }

    public void setLowTime(long lowTime) {
        this.lowTime = lowTime;
    }

}
