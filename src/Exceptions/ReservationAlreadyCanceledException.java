package Exceptions;

public class ReservationAlreadyCanceledException extends Exception{
    public ReservationAlreadyCanceledException(){
        super();
    }

    public ReservationAlreadyCanceledException(String s){
        super(s);
    }
}
