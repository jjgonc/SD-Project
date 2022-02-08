package Model;

import java.time.LocalDate;
import java.util.List;


public class Reservation {

    String idReservation;
    LocalDate day;
    List<String> route;
    boolean cancel; // se foi cancelada

    public Reservation (String idReservation,LocalDate date, List<String> route) {
        this.idReservation = idReservation;
        this.day = date;
        this.route = route;
        this.cancel = false;
    }

    public Reservation( Reservation r) {
        this.idReservation = r.getIdReservation();
        this.day = r.getDay();
        this.route = r.getRoute();
        this.cancel = r.isCancel();
    }

    public String getIdReservation() {
        return this.idReservation;
    }

    public void setIdReservation(String idReservation) {
        this.idReservation = idReservation;
    }

    public LocalDate getDay() {
        return this.day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public List<String> getRoute() {
        return this.route;
    }

    public void setRoute(List<String> route) {
        this.route = route;
    }

    public boolean isCancel() {
        return this.cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }


    public Reservation clone() {
        Reservation r = new Reservation(this);
        return r;
    }
}
