package ejb;

import jpa.entities.Bar;
import jpa.entities.Customer;
import jpa.entities.ItemReservation;
import jpa.entities.MenuEntry;
import utilities.ReservationInfo;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@MessageDriven(
        name = "ReservationConsumerEJB",
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/reservationMsgDest"),
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
        }
)
public class ReservationMDB implements MessageListener {

    @EJB
    ReservationCounterBean reservationCounterBean;

    @PersistenceContext(unitName = "DorstPersistenceUnit")
    EntityManager entityManager;

    public ReservationMDB() {
    }

    @Override
    public void onMessage(Message message) {
        ObjectMessage msg = (ObjectMessage) message;
        try {
            ReservationInfo reservationMsg = (ReservationInfo) msg.getObject();
            Bar bar = entityManager.find(Bar.class, reservationMsg.barId);
            MenuEntry menuEntry = bar.getMenuEntryById(reservationMsg.menuEntryId);
            Customer customer = entityManager.find(Customer.class, reservationMsg.customerUsername);
            if(bar != null && menuEntry != null) {
                if (menuEntry.getStock() >= reservationMsg.amount) {
                    ItemReservation success = bar.addReservation(customer, reservationMsg.menuEntryId, reservationMsg.amount);
                    if (success != null) {
                        customer.setCredit(customer.getCredit() - (menuEntry.getPrice() * reservationMsg.amount));
                        customer.addReservation(success);
                        reservationCounterBean.incReservationsDone();
                        entityManager.merge(customer);
                        entityManager.merge(bar);
                        entityManager.flush();
                    }
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
