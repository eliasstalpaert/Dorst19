package ejb;

import jpa.entities.*;
import utilities.ReservationInfo;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.concurrent.TimeUnit;

@Stateful(name = "ReservationEJB")
@StatefulTimeout(unit = TimeUnit.MINUTES, value = 60)
public class ReservationBean {
    @EJB
    ReservationCounterBean reservationCounterBean;

    @PersistenceContext(name = "DorstPersistenceUnit")
    EntityManager entityManager;

    @Resource(mappedName = "jms/reservationMsgDest")
    private Queue reservationDest;

    @Resource(mappedName = "jms/reservationMsg")
    private ConnectionFactory queue;

    int barId = -1;
    int menuEntryId = -1;
    Customer customer = null; //zou uit sessioncontext moeten gehaald worden
    int amount = 0;
    float total = 0;
    boolean ready =false;
    String status = "No reservation set";

    public ReservationBean()
    {
    }

    public void setReservation(int barId, int menuEntryId, Customer customer, int amount)
    {
        Bar bar = entityManager.find(Bar.class, barId);
        MenuEntry menuEntry = bar.getMenuEntryById(menuEntryId);
        if(menuEntry != null && bar != null && amount > 0)
        {
            this.customer = customer;
            this.barId = barId;
            this.menuEntryId = menuEntryId;
            this.amount = amount;
            total = bar.getMenuEntryById(menuEntryId).getPrice() * amount;
            if(menuEntry.getStock() >= amount){
                if(customer.getCredit() >= total)
                {
                    ready = true;
                    status = status = amount + " of " + bar.getMenuEntryById(menuEntryId).getItem().getName()
                            + " in " + bar.getBarInfo().getName()
                            + " with total price: " + total;
                }
                else
                {
                    status = "Insufficient amount of credit (total: " + total + ")";
                    reset();
                }
            }
            else
            {
                status = "Not enough stock in café";
                reset();
            }
        }
        else
        {
            status =  "Invalid reservation, try again";
            reset();
        }

    }

    private void reset()
    {
        this.barId = barId;
        this.menuEntryId = menuEntryId;
        this.customer = null;
        this.amount = 0;
        ready = false;
    }

    public String getStatus()
    {
        return status;
    }

    public boolean readyToPay()
    {
        return ready;
    }

    public void payReservation()
    {
        //Produces ObjectMessage (needs to be serializable!) containing the ItemReservation

        ReservationInfo reservationInfo = new ReservationInfo(barId, menuEntryId, customer.getUsername(), amount);

        try {
            Connection conn = queue.createConnection();
            Session s = conn.createSession();
            MessageProducer mp = s.createProducer(reservationDest);
            ObjectMessage reservationMsg = s.createObjectMessage();
            reservationMsg.setObject(reservationInfo);
            mp.send(reservationMsg);
            status = "Reservation sent to Bar";
            ready = false;
        }
        catch (Exception e)
        {
            System.out.println(e);
            status = "Exception occurred, please try again";
        }


        /*
        Bar bar = entityManager.find(Bar.class, barId);
        MenuEntry menuEntry = bar.getMenuEntryById(menuEntryId);
        if(bar != null && menuEntry != null && ready)
        {
            if(menuEntry.getStock() >= amount)
            {
                ItemReservation success = bar.addReservation(customer, menuEntryId, amount);
                if(success != null)
                {
                    customer.setCredit(customer.getCredit() - (menuEntry.getPrice()*amount));
                    customer.addReservation(success);
                    entityManager.merge(customer);
                    entityManager.merge(bar);
                    reservationCounterBean.incReservationsDone();
                    status = "Reservation complete";
                    reset();
                    return true;
                }
                else
                {
                    status = "Payment failed: failed to create reservation";
                    reset();
                }
            }
            else
            {
                status = "Payment failed: out of stock now";
                reset();
            }
        }
        return false;
        */

    }
}
