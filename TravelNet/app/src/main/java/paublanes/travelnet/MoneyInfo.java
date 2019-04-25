package paublanes.travelnet;

import java.io.Serializable;

public class MoneyInfo implements Serializable {
    private String category;
    private int amount;

    public MoneyInfo() {}
    public MoneyInfo(String category, int amount) {
        this.category = category;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
