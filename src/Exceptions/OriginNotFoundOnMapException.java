package Exceptions;

public class OriginNotFoundOnMapException extends Exception {
    public OriginNotFoundOnMapException(){
            super();
    }

    public OriginNotFoundOnMapException(String s){
            super(s);
    }
}
