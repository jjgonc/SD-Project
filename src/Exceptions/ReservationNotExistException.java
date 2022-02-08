package Exceptions;

public class ReservationNotExistException extends Exception {
    public ReservationNotExistException(){
        super();
    }

    public ReservationNotExistException(String s){
        super(s);
    }
}
