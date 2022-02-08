package Server;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import Exceptions.OriginNotFoundOnMapException;
import Exceptions.ReservationAlreadyCanceledException;
import Exceptions.ReservationNotExistException;
import Model.*;

import Model.Frame.Tag;

public class ServerConnection implements Runnable {
    private final TaggedConnection tC;
    private final Info info;
    private String username;
    private boolean loggedIn;
    private boolean online;
    private boolean stressed;

    public ServerConnection(TaggedConnection tCG, Info info) {
        this.tC = tCG;
        this.info = info;
        this.username = null;
        this.loggedIn = false;
        this.online = true;
        this.stressed = false;
    }

    @Override

    public void run() {
        while (this.online) {
            Frame frame;

            try {
                frame = this.tC.receive();

                if(frame != null){

                    switch (frame.tag) {

                        case SIGNUP:
                            signup(frame);
                            break;

                        case LOGIN:
                            login(frame);
                            break;

                        case LOGOUT:
                            logout();
                            break;

                        case INSERT_FLY:
                            insertFlight(frame);
                            break;

                        case CLOSE_DAY:
                            closeDay(frame);
                            break;

                        case CLOSE_SERVICE:
                            closeServer();
                            break;

                        case BOOK_TRIP:
                            bookTrip(frame);
                            break;

                        case CANCEL_FLIGHT:
                            cancelFlight(frame);
                            break;

                        case GET_FLIGHTS_LIST:
                            getFLYlist();
                            break;

                        case GET_ALL_ROUTES:
                            getAllRoutes(frame);
                            break;

                        case STRESSED:
                            getStressed();
                            break;
                        case RESERVATIONS:
                            getReservations();
                            break;

                        default:
                            break;
                    }
                }else {
                    this.online = false;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void signup(Frame frame) throws IOException {

        String[] received = new String(frame.data).split("/");

        String username = received[0];
        String password = received[1];

        boolean created = info.createAccount(username,password,false);

        String res;
        if(created){
            res = "REGISTADO";
            this.username = username;
            this.loggedIn = true;
            this.info.increaseUsersLogged();

            System.out.println("Utilizador " + username + "registado");


        }else {
            res = "USER-EXISTS";
        }

        tC.send(new Frame(Tag.SIGNUP,res.getBytes()));

    }

    private void login(Frame frame) throws IOException {

        String[] received = new String(frame.data).split("/");

        String username = received[0];
        String password = received[1];

        int logged = info.verifyLogin(username,password);

        String res = switch (logged) {
            case 0 -> "PASSWORD";
            case 1 -> "USER";
            case 2 -> "ADMIN";
            case 3 -> "NOTFOUND";
            default -> null;
        };

        if(logged == 1 || logged == 2){
            this.username = username;
            this.loggedIn = true;
            this.info.increaseUsersLogged();
            System.out.println("User " + username + " logged");


            System.out.println(this.info.getUsersLogged());
        }

        tC.send(new Frame(Tag.LOGIN,res.getBytes()));

    }


    private void logout() throws IOException {
        this.tC.close();
        this.info.decreaseUsersLogged();
        this.online = false;
        this.info.wakeup();
        System.out.println("User " + username + " logout");

        System.out.println(this.info.getUsersLogged());
    }


    public void  insertFlight(Frame frame) throws IOException {

        String[] received = new String(frame.data).split("/");

        String origin = received[0];
        String destination = received[1];
        int capcity = Integer.parseInt(received[2]);


        boolean inserted = info.insertFlight(origin,destination,capcity);

        String res = null;
        if(inserted){
            res = "INSERTED";
            System.out.println("Voo inserido ->" + origin + " " + destination + " " + capcity );
        }else{
            res = "UPDATED";

            System.out.println("Voo atualizado em termos de capacidade ->" + origin + " " + destination + " " + capcity );
        }
        tC.send(new Frame(Tag.INSERT_FLY,res.getBytes()));

    }

    public void closeDay(Frame frame) throws IOException {
        String receiveMessage = new String(frame.data);
        String[] dateDMA = receiveMessage.split("-");
        LocalDate date = LocalDate.of(Integer.parseInt(dateDMA[2]),Integer.parseInt(dateDMA[1]),Integer.parseInt(dateDMA[0]));

        String res = null;
        boolean b = this.info.closeDay(date);
        if (b) {
            res = "DAY_CLOSED";
        }else {
            res = "ALREADY_CLOSED";
        }

        tC.send(new Frame(Tag.CLOSE_DAY,res.getBytes()));

        System.out.println("Dia " + dateDMA[0] + "/" + dateDMA[1] + "/" + dateDMA[2] + " fechado");

    }


    public void closeServer() throws IOException {

        this.info.closeServer();

        String res = "CLOSING";

        System.out.println("Server irá fechar em momentos");

        tC.send(new Frame(Tag.CLOSE_SERVICE,res.getBytes()));
    }

    public void bookTrip (Frame frame) throws IOException {
       /* try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        */


        String receiveMessage = new String(frame.data); // recebe route/dataInicio/DataFim
        String[] rm = receiveMessage.split("/");
        String[] citys = rm[0].split("-");
        List<String> route =  new ArrayList<String>();
        for (String city : citys) {
            route.add(city);
        }
        System.out.println(rm[1]);
        System.out.println(rm[2]);
        String[] dateS = rm[1].split("-");
        String[] dateE = rm[2].split("-");
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateS[2]),Integer.parseInt(dateS[1]),Integer.parseInt(dateS[0]));
        LocalDate endDate = LocalDate.of(Integer.parseInt(dateE[2]),Integer.parseInt(dateE[1]),Integer.parseInt(dateE[0]));
        String codReserve = info.bookTrip(username,route,startDate,endDate);
        System.out.println(codReserve);


        System.out.println("Viagem de " + startDate.toString() + "até " + endDate.toString() + "reservada");
        System.out.println("Pelas cidades : " + route);


        tC.send(new Frame(Tag.BOOK_TRIP,codReserve.getBytes()));

        this.info.decreaseDoingRequest();
    }


    public void cancelFlight(Frame frame) throws IOException {
       /* try {

            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        */


        String idReservation = new String(frame.data);


        String send = null;
        try {
            info.cancelFlight(username,idReservation);
            send = "CANCELED";

            System.out.println("Reserva " + idReservation + " cancelado");
        } catch (ReservationNotExistException e) {
            send = "NOT_EXIST";
            System.out.println("Reserva " + idReservation + " não existe");
        } catch (ReservationAlreadyCanceledException e) {
            send = "ALREADY_EXIST";
            System.out.println("Reserva " + idReservation + " já estava cancelado");
        }

        tC.send(new Frame(Tag.CANCEL_FLIGHT,send.getBytes()));

        this.info.decreaseDoingRequest();
    }

    public void getFLYlist() throws IOException {
        List<Map.Entry<String,String>> flights = info.getFlightsList();
        String send = null;
        if (flights.size() == 0) {
            send = "NO_FLIGHTS";
        } else {
            StringBuilder sb = new StringBuilder(); // controi do tipo origem1-Destino1/origem2-Destino2/
            for (Map.Entry<String, String> entry : flights) {
                sb.append(entry.getKey()).append("-").append(entry.getValue()).append("/");
            }
            sb.deleteCharAt(sb.length() - 1); // apaga a ultima barra a mais
            send = sb.toString();
        }
        tC.send(new Frame(Tag.GET_FLIGHTS_LIST,send.getBytes()));

        System.out.println("Lista de voos possiveis enviada");

    }


    public void getAllRoutes(Frame frame) throws IOException {
        String receiveMessage = new String(frame.data);
        String[] rm = receiveMessage.split("/");
        String send = null;
        try {
            StringBuilder res = new StringBuilder();
            List<List<String>> resList = this.info.percursosComEscalas(rm[0],rm[1]);
            if (resList.isEmpty()) send = "ROUTE_2SCALES_NOT_POSSIBLE";
            else {
                System.out.println(resList);
                for (List<String> ls : resList) {
                    for (String s : ls) {
                        res.append(s).append("->");
                    }
                    res.delete(res.length()-2,res.length());
                    res.append("/");
                    send = res.toString();
                }
            }
        } catch (OriginNotFoundOnMapException e) {
            send = "ORIGIN_NOT_EXIST";
        }
        tC.send(new Frame(Tag.GET_ALL_ROUTES,send.getBytes()));
    }



    public void getStressed() throws IOException {
        String res = null;
        if (this.info.getDoingRequest() >= 1) {
            res = "STRESSED";
            this.info.increaseDoingRequest();
        } else {
            res = "EMPTY";
            this.info.increaseDoingRequest();
        }
        tC.send(new Frame(Tag.STRESSED,res.getBytes()));
    }



    public void getReservations() throws IOException {

        tC.send(new Frame(Tag.RESERVATIONS,this.info.getReservations(this.username).getBytes()));
    }


}
