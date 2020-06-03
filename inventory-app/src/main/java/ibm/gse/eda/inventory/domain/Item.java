package ibm.gse.eda.inventory.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;

import com.ibm.db2.cmx.annotation.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Item extends PanacheEntity {
    @Id
    @SequenceGenerator(name = "item-seq-generator",sequenceName="item_seq")
    @GeneratedValue(generator = "item-seq-generator", strategy = GenerationType.SEQUENCE)
    @Column(unique=true, nullable=false)
    public Long id;
    @Column(length = 10, unique = true)
    public String code;
    @Column(length = 100)
    public String title;
    public double price;

    public Item(){}
}