package Model;

public class Frame {

    public enum Tag {
        LOGIN, SIGNUP, LOGOUT, INSERT_FLY, CLOSE_DAY, CLOSE_SERVICE, BOOK_TRIP, CANCEL_FLIGHT, GET_FLIGHTS_LIST, GET_ALL_ROUTES, STRESSED, RESERVATIONS;
    }

    public final Tag tag;
    public final byte[] data;

    public Frame(Tag tagG, byte[] dataG) {
        tag = tagG;
        data = dataG;
    }
}