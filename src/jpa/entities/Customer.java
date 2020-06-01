package jpa.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("C")
public class Customer extends User {
    @Column(name = "credit")
    private float credit = 0;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer", orphanRemoval = true)
    private List<ItemReservation> reservations = new ArrayList<>();

    protected Customer()
    {

    }

    public Customer(String username, String password)
    {
        this();
        this.username = username;
        this.password = password;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    public boolean addReservation(ItemReservation itemReservationEntity)
    {
        return reservations.add(itemReservationEntity);
    }

    public boolean removeReservation(ItemReservation itemReservationEntity)
    {
        return reservations.remove(itemReservationEntity); //returns true if collection contained the reservation
    }

    public List<ItemReservation> getReservations()
    {
        return reservations;
    }
}
