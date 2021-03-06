package jpa.entities;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Entity
@Table(
        name = "MENU_ENTRY"
)
@NamedQuery(name = "CHECK_DRINK_REF", query = "SELECT m FROM MenuEntry m WHERE m.item.id = :id")
@XmlRootElement
public class MenuEntry {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_fk")
    private Item item;

    public void setItem(Item item) {
        this.item = item;
    }

    @PositiveOrZero(message = "cannot be negative")
    @Column(name = "price")
    private float price;

    @PositiveOrZero(message = "cannot be negative")
    @Column(name = "stock")
    private int stock;

    protected MenuEntry() {

    }

    public MenuEntry(Item item, float price, int stock) {
        this.item = item;
        this.price = price;
        this.stock = stock;
    }

    @XmlElement
    public int getId() {
        return id;
    }

    @XmlElement
    public Item getItem() {
        return item;
    }

    @XmlElement
    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @XmlElement
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MenuEntry that = (MenuEntry) o;
        return Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
