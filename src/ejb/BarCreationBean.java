package ejb;

import jpa.embeddables.Address;
import jpa.embeddables.BarInfo;
import jpa.entities.*;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;


@Stateless(name = "BarCreationEJB")
@Interceptors(LogInterceptor.class)
public class BarCreationBean {
    @PersistenceContext(name = "DorstPersistenceUnit")
    EntityManager entityManager;

    public BarCreationBean() {
    }

    public boolean createBar(BarBoss initBoss, BarInfo newBarInfo, int capacity) {
        TypedQuery<Bar> barQuery = entityManager.createNamedQuery("CHECK_EXISTING_BARS", Bar.class)
                .setParameter("barinfo", newBarInfo);

        if (barQuery.getResultList().size() <= 0) {
            Bar new_bar = new Bar(newBarInfo, capacity);
            new_bar.addBoss(initBoss);
            validateBar(new_bar);
            entityManager.persist(new_bar);
            entityManager.merge(initBoss);
            return true;
        }
        return false;
    }

    public void removeBar(int cafeId) throws DorstException {
        Bar toDelete = entityManager.find(Bar.class, cafeId);
        entityManager.refresh(toDelete);
        if (toDelete != null) {
            if (!toDelete.getReservations().isEmpty())
                throw new DorstException("There are still customer reservations");
            else entityManager.remove(toDelete);
        } else throw new DorstException("Internal server error");
    }

    private void validateBar(Bar bar) throws DorstException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<Bar>> constraintViolationsBar = validator.validate(bar);
        Set<ConstraintViolation<BarInfo>> constraintViolationsBarInfo = validator.validate(bar.getBarInfo());
        Set<ConstraintViolation<Address>> constraintViolationsAddress = validator.validate(bar.getBarInfo().getAddress());

        if (constraintViolationsBar.size() > 0 || constraintViolationsBarInfo.size() > 0 || constraintViolationsAddress.size() > 0) {

            Set<String> violationMessages = new HashSet<>();
            for (ConstraintViolation<Bar> constraintViolation : constraintViolationsBar) {
                violationMessages.add(constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage() + "\t|\t");
            }
            for (ConstraintViolation<BarInfo> constraintViolation : constraintViolationsBarInfo) {
                violationMessages.add(constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage() + "\t|\t");
            }
            for (ConstraintViolation<Address> constraintViolation : constraintViolationsAddress) {
                violationMessages.add(constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage() + "\t|\t");
            }

            throw new DorstException(String.join("\n", violationMessages));
        }
    }
}
