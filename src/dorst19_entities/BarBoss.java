package dorst19_entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@DiscriminatorValue("B")
public class BarBoss extends User {
    @ManyToMany(mappedBy = "bosses")
    private List<Bar> ownedBars = new ArrayList<>();

    public List<Bar> getOwnedBars()
    {
        return ownedBars;
    }

    public boolean addBar(Bar bar)
    {
        if(ownedBars.contains(bar) == false)
        {
           return ownedBars.add(bar); //returns true if collection has changed
        }
        else return false;
    }

    public boolean removeBar(Bar bar)
    {
        return ownedBars.remove(bar);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarBoss that = (BarBoss) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(ownedBars,that.ownedBars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, ownedBars);
    }
}
