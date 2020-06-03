package ibm.gse.eda.inventory.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Inventory extends PanacheEntity {
    @Id
    @SequenceGenerator(name = "inventory-seq-generator", sequenceName="inventory_seq")
    @GeneratedValue(generator = "inventory-seq-generator", strategy = GenerationType.SEQUENCE)
    @Column(unique=true, nullable=false)
    public Long id;
    public Long storeName;
    public Long itemCode;
    public Long quantity;
    public Long timestamp;
    
    public Inventory(){}
}