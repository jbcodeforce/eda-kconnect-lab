package ibm.gse.eda.inventory.domain;

import java.util.HashMap;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Inventory {
    public String storeName;
    public HashMap<String,Long> stock = new HashMap<String,Long>();

    public Inventory(){}

    public Inventory(String storeName) {
        this.storeName = storeName;
    }

    public Inventory(String storeName, String itemID, int quantity) {
        this.storeName = storeName;
        this.updateStock(itemID, quantity);
    }

    public Inventory updateStockQuantity(String k, Item newValue) {
        this.storeName = k;
        if (newValue.type.equals("SALE")) 
            newValue.quantity=-newValue.quantity;
        return this.updateStock(newValue.itemCode,newValue.quantity);
    }

    public Inventory updateStock(String itemID, long newV) {
        if (stock.get(itemID) == null) {
            stock.put(itemID, Long.valueOf(newV));
        } else {
            Long currentValue = stock.get(itemID);
            stock.put(itemID, Long.valueOf(newV) + currentValue );
        }
        return this;
    }

}