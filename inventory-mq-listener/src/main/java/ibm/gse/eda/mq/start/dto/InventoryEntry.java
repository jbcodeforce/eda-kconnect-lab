package ibm.gse.eda.mq.start.dto;

import ibm.gse.eda.mq.start.domain.InventoryMessage;

public class InventoryEntry {

    public String storeName;
    public String itemCode;
    public Long quantity;
    public Double price;
    public String timestamp;

	public static InventoryEntry builder(InventoryMessage itemSold) {
        InventoryEntry e = new InventoryEntry();
        e.storeName = itemSold.storeName;
        e.itemCode = itemSold.itemCode;
        e.quantity = itemSold.quantity;
        e.price = itemSold.price;
        e.timestamp = itemSold.timestamp;
		return e;
	}
    
}