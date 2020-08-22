package ibm.gse.eda.inventory.domain;

import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Item {
    public static String RESTOCK = "RESTOCK";
    public static String SALE = "SALE";
    public Long id;
    public String storeName;
    public String itemCode;
    public int quantity;
    public String type;
    public Double price;
    public String timestamp;

    public Item(){}

	public Item(String store, String item, String type, int quantity, double price) {
        this.storeName = store;
        this.itemCode = item;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now().toString();
	}

	public Item(String store, String item, String type, int quantity) {
        this.storeName = store;
        this.itemCode = item;
        this.type = type;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now().toString();
	}
}