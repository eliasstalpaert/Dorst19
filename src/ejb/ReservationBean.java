package ejb;

import jpa.embeddables.BarInfo;
import jpa.entities.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.awt.*;

@Stateful(name = "ReservationEJB")
@DependsOn("ReservationCounterBean")
public class ReservationBean {

    @EJB
    ReservationCounterBean reservationCounterBean;

    @PersistenceContext(name = "DorstPersistenceUnit")
    EntityManager entityManager;

    @Resource
    SessionContext ctx;


    BarInfo barInfo = null;
    MenuEntry menuEntry = null;
    Customer customer = null; //zou uit sessioncontext moeten gehaald worden
    int amount = 0;

    public ReservationBean()
    {
    }


    public void setCustomer(Customer customer)
    {
        //if(!ctx.isCallerInRole("Customer")) throw new SecurityException("Only customers can do reservations");
        this.customer = customer;
    }

    public void setBarInfo(BarInfo barInfo)
    {
        this.barInfo = barInfo;
    }

    public void setMenuEntry(MenuEntry menuEntry)
    {
        this.menuEntry = menuEntry;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    @Remove
    public boolean payReservation()
    {
        //if(!ctx.isCallerInRole("Customer")) throw new SecurityException("Only customers can do reservations");
        if(customer == null || menuEntry == null || barInfo == null || amount <= 0) return false;
        Bar bar = entityManager.find(Bar.class, barInfo);
        if(bar != null && bar.getMenu().contains(menuEntry))
        {
            float itemPrice = menuEntry.getPrice();
            float customerCredit = customer.getCredit();
            if(customerCredit >= itemPrice)
            {
                customer.setCredit(customerCredit - itemPrice);
                boolean success = bar.addReservation(customer, menuEntry.getItem(), amount);
                if(success) reservationCounterBean.incReservationsDone();
                return success;
            }
            else return false;
        }
        return false;
    }

}
