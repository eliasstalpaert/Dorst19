package jpa.embeddables;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class TimePeriod {

    @Min(value = 0, message = "minimum is 0")
    @Max(value = 23, message = "maximum is 23")
    @Column(name = "begin_hour")
    private int beginHour;

    @Min(value = 0, message = "minimum is 0")
    @Max(value = 23, message = "maximum is 23")
    @Column(name = "end_hour")
    private int endHour;

    protected TimePeriod() {

    }

    public TimePeriod(int beginHour, int endHour) {
        this();
        this.beginHour = beginHour;
        this.endHour = endHour;
    }

    public int getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(int beginHour) {
        this.beginHour = beginHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimePeriod that = (TimePeriod) o;
        return Objects.equals(beginHour, that.beginHour) &&
                Objects.equals(endHour, that.endHour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginHour, endHour);
    }
}
