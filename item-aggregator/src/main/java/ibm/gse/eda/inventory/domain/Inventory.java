package ibm.gse.eda.inventory.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Inventory {
    public String storeName;
    public String itemCode;
    public int quantity;
    public Double price;
    public String timestamp;

    public Inventory(){}

}