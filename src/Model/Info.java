package Model;

import Exceptions.OriginNotFoundOnMapException;
import Exceptions.ReservationAlreadyCanceledException;
import Exceptions.ReservationNotExistException;
import Server.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.*;

import java.time.LocalTime;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Info {

    private Map<String, List<Flight>> flightsMap;   //key is the origin; value is the list of the flights with departure from that origin
    private List<LocalDate> closedScheduleList; // list with closed days
    private Map<String, Account> accountsMap;
    private boolean online;
    private int idCounterReservations;
    private int usersLogged;
    private Server s;
    private ReentrantReadWriteLock l;
    private ReentrantReadWriteLock.ReadLock rl;
    private ReentrantReadWriteLock.WriteLock wl;
    private int doingRequest;


    public Info(Server server) {

        this.flightsMap = new HashMap<>();
        this.accountsMap = new HashMap<>();
        this.closedScheduleList = new ArrayList<>();
        this.idCounterReservations = 0;
        this.online = true;
        this.usersLogged = 0;
        this.s = server;
        this.l = new ReentrantReadWriteLock();
        this.wl = l.writeLock();
        this.rl = l.readLock();
        this.doingRequest = 0;

        // MÉTODOS PARA A PERSISTENCIA DE DADOS
        try {
            File accounts = new File("saves/accounts.txt");
            if (accounts.exists()) {
                loadAccounts("saves");
            } else {
                this.accountsMap = new HashMap<>();
            }

            File flights = new File("saves/flights.txt");
            if (flights.exists()) {
                loadFlights("saves");
            } else {
                this.flightsMap = new HashMap<>();
            }

            File closedSchedule = new File("saves/closedSchedules.txt");
            if (closedSchedule.exists()) {
                loadClosedSchedules("saves");
            } else {
                this.accountsMap = new HashMap<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String getReservations(String username){


        StringBuilder res = new StringBuilder();


        Map<String,Reservation> reservations = this.accountsMap.get(username).reservations;

        if(reservations.values().size() == 0){
            return "NULL";
        }else{
            for(Reservation r : reservations.values()){

                String reservation  =  r.getIdReservation() + " - " + r.getRoute().toString();

                if(r.isCancel()){
                    reservation = reservation + "-" + "Cancelada" + "\n";

                }else {
                    reservation = reservation + "-" + "Validada" + "\n";
                }

                res.append(reservation);
            }
        }

        return res.toString();

    }



    public void wakeup() {
        this.s.wakeup();
    }


    public void closeServer() {

        wl.lock();

        try {
            this.online = false;
        } finally {
            wl.unlock();
        }
    }

    public void increaseUsersLogged() {
        this.wl.lock();

        try {
            this.usersLogged++;
        } finally {
            this.wl.unlock();
        }
    }

    public void decreaseUsersLogged() {
        this.wl.lock();

        try {
            this.usersLogged--;
        } finally {
            this.wl.unlock();
        }
    }

    public void increaseDoingRequest() {
        this.wl.lock();

        try {
            this.doingRequest++;
        } finally {
            this.wl.unlock();
        }
    }

    public void decreaseDoingRequest() {
        this.wl.lock();

        try {
            this.doingRequest--;
        } finally {
            this.wl.unlock();
        }
    }

    public int getDoingRequest() {
        this.rl.lock();
        try {
            return doingRequest;
        } finally {
            this.rl.unlock();
        }

    }

    public int getUsersLogged() {
        this.rl.lock();

        try {
            return usersLogged;
        } finally {
            this.rl.unlock();
        }

    }

    public boolean isOnline() {
        this.rl.lock();

        try {
            return this.online;

        } finally {
            this.rl.unlock();
        }

    }


    public void saveData(String pasta) {
        File folder = new File(pasta);
        if (!folder.exists())
            folder.mkdir();

        try {
            saveAccounts(pasta);
            saveFlights(pasta);
            saveClosedSchedules(pasta);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // MÉTODOS PARA MANIPULAÇÃO DE FLIGHTS (serialize & deserialize)
    public void loadFlights(String folder) throws IOException, ClassNotFoundException {
        File toRead = new File(folder + "/flights.txt");
        FileInputStream fis = new FileInputStream(toRead);
        ObjectInputStream ois = new ObjectInputStream(fis);
        this.flightsMap = (HashMap<String, List<Flight>>) ois.readObject();
        ois.close();
        fis.close();

    }

    public void saveFlights(String folder) throws IOException {
        File file = new File(folder + "/flights.txt");
        if (!file.exists())
            file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this.flightsMap);
        oos.flush();
        oos.close();
        fos.close();
    }

    // MÉTODOS PARA MANIPULAÇÃO DE ACCOUNTS (serialize & deserialize)
    public void loadAccounts(String folder) throws IOException, ClassNotFoundException {
        File toRead = new File(folder + "/accounts.txt");
        FileInputStream fis = new FileInputStream(toRead);
        ObjectInputStream ois = new ObjectInputStream(fis);
        this.accountsMap = (HashMap<String, Account>) ois.readObject();
        ois.close();
        fis.close();
    }

    public void saveAccounts(String folder) throws IOException {
        File file = new File(folder + "/accounts.txt");
        if (!file.exists())
            file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this.accountsMap);
        oos.flush();
        oos.close();
        fos.close();
    }

    // MÉTODOS PARA MANIPULAÇÃO DE CLOSEDSCHEDULE (serialize & deserialize)
    public void loadClosedSchedules(String folder) throws IOException, ClassNotFoundException {
        File toRead = new File(folder + "/closedSchedules.txt");
        FileInputStream fis = new FileInputStream(toRead);
        ObjectInputStream ois = new ObjectInputStream(fis);
        this.closedScheduleList = (List<LocalDate>) ois.readObject();
        ois.close();
        fis.close();
    }

    public void saveClosedSchedules(String folder) throws IOException {
        File file = new File(folder + "/closedSchedules.txt");
        if (!file.exists())
            file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this.closedScheduleList);
        oos.flush();
        oos.close();
        fos.close();
    }


    //Funções de manipulação de login e signup

    public boolean createAccount(String username, String password, boolean admin) {


        this.wl.lock();

        try {
            boolean res = false;

            if (!this.accountsMap.containsKey(username)) {
                Account account = new Account(username, password, admin);
                res = true;
                this.accountsMap.put(username, account);
            }

            return res;
        } finally {
            this.wl.unlock();
        }


    }

    //0 PARA PASS ERRADA
    //1 PARA USER
    //2 PARA ADMIN
    //3 PARA CONTA INEXISTENTE

    public int verifyLogin(String user, String password) {

        wl.lock();

        try {
            int res = 0;

            if (this.accountsMap.containsKey(user)) {
                Account account = this.accountsMap.get(user);
                if (account.getPassword().equals(password)) {
                    if (account.getAdministrador()) res = 2;
                    else res = 1;
                }
            } else {
                res = 3;
            }

            return res;
        } finally {
            wl.unlock();
        }

    }


//3. Inserção de informação sobre voos (origem, destino, capacidade) pelo administrador.

    //devolve true caso insira
    //devolve false caso atualize
    public boolean insertFlight(String origin, String destination, int capacity) {

        wl.lock();

        try {
            boolean inserted = false;

            if (flightsMap.containsKey(origin)) {   //verify if the map with all the flights contains the desired flight (searching for the origin which is the key)
                List<Flight> flightsFromOrigin = flightsMap.get(origin);    //get the list of Flights that takes departure from that origin
                Flight flight = getFlightFromList(flightsFromOrigin, destination);
                if (flight != null) {
                    flight.setCapacity(capacity);//once you got it, simply go to the map of the occupations and update the occupation on the desired date (which comes from an argument)
                    inserted = false;
                } else {      //if the list doesn't contain the flight with the desired destination, we create and add it to the list
                    Flight newFlight = new Flight(destination, capacity, new HashMap<>());
                    List<Flight> newList = flightsMap.get(origin);
                    newList.add(newFlight);
                    flightsMap.put(origin, newList);
                    inserted = true;
                }
            } else {      //in case the origin isn't in the flightsMap
                Flight flight = new Flight(destination, capacity, new HashMap<>());
                List<Flight> newList = new ArrayList<>();
                newList.add(flight);
                flightsMap.put(origin, newList);
                inserted = true;
            }
            return inserted;

        } finally {
            wl.unlock();
        }

    }


    // Encerramento de um dia
    // devolve true se o dia ainda nao tava fechado e pos fechado ou false se ja estava fechado
    public boolean closeDay(LocalDate date) {

        wl.lock();

        try {
            if (verifyCloseDay(date)) {
                return false;
            } else {
                this.closedScheduleList.add(date);
                return true;
            }
        } finally {
            wl.unlock();

        }

    }

    // recebe lista com o percurso completo como uma lista de String de todas as cidades por onde passa
    // primeira é a origem a ultima é o destino
    // e um intervalo de datas

    public String bookTrip(String acountID, List<String> route, LocalDate startDate, LocalDate endDate) {


        wl.lock();

        try {


            String codReserve = null;
            LocalDate testDate = startDate;
            boolean found = false;
            LocalDate foundDate = null;
            if (!verifyRoute(route))  return "ROUTE_NOT_POSSIBLE";

            while(!found && (testDate.isBefore(endDate) || testDate.isEqual(endDate))) {
                    if (verifyCloseDay(testDate)){
                        // se o verify der true ou seja o dia esta encerrado
                        testDate = testDate.plusDays(1);

                    } else {
                        // se o dia nao estiver encerrado
                        boolean possible = true;
                        for (int i = 0; (i < route.size() - 1) && possible; i++) {
                            String originCity = route.get(i);
                            String destinationCity = route.get(i+1);
                            possible = checkFlightDate(originCity,destinationCity,testDate); // se nao for possivel fica false e salta fora do for
                        }
                        if (possible) {
                            foundDate = testDate;
                            found = true;
                        } else {
                            testDate = testDate.plusDays(1);
                        }

                    }

                }

            if (found) {
                 codReserve = registerFlight(acountID,route,foundDate) + "/" + foundDate.toString();
            }
            else  {
                 codReserve = "NO_POSSIBLE";
            }
            return codReserve;
        } finally {
            wl.unlock();
        }

    }


    public boolean verifyCloseDay(LocalDate date) {

        rl.lock();

        try {

            return this.closedScheduleList.contains(date);

        } finally {
            rl.unlock();
        }

    }

    public boolean verifyRoute( List<String> route) {
        boolean b = true;
        for (int i=0; i < route.size() - 1; i++){
            if (this.flightsMap.containsKey(route.get(i))) {
                List<Flight> destinations = this.flightsMap.get(route.get(i));
                Flight f = null;
                f = getFlightFromList(destinations,route.get(i+1));
                if (f == null) {
                    b = false;
                    break;
                }
            } else {
                b = false;
                break;
            }
        }
        return b;
    }

    public boolean checkFlightDate(String originCity,String destinationCity,LocalDate date) {
        try {
            rl.lock();
            boolean r = false;
            if (this.flightsMap.containsKey(originCity)) {
                List<Flight> destinations = this.flightsMap.get(originCity);
                Flight destination = getFlightFromList(destinations, destinationCity);
                if (destination != null) {
                    if (destination.seatsLeft(date) > 0) r = true;
                }
            }
            return r;
        }finally {
            rl.unlock();
        }

    }




    public Flight getFlightFromList(List<Flight> flights, String destination) {

        rl.lock();

        try {

            Flight res = null;
            for (Flight f : flights) {
                if (f.getDestination().compareTo(destination) == 0) {
                    res = f;
                    break;
                }
            }
            return res;

        } finally {
            rl.unlock();
        }
    }


    // regista o voo quando ja sabe que é possivel nesta data
    public String registerFlight(String acountId, List<String> route, LocalDate date) {

        wl.lock();

        try {

            for (int i = 0; (i < route.size() - 1); i++) {
                String originCity = route.get(i);
                String destinationCity = route.get(i + 1);
                Flight f = getFlightFromList(this.flightsMap.get(originCity), destinationCity);
                int newOcupation = f.getOccupationDate(date) + 1;
                f.setOccupationDate(date, newOcupation);
            }


            String idReservation = Integer.toString(idCounterReservations);
            idCounterReservations++;
            Reservation res = new Reservation(idReservation, date, route);
            Account acc = this.accountsMap.get(acountId);
            acc.addReservation(idReservation, res);


            return idReservation;

        } finally {
            wl.unlock();
        }


    }

    // se nao devolver nenhuma exception foi cancelado com suceso
    // quem chamar esta funcao depois tratar a exception que ja leva la o codReserva
    public void cancelFlight(String idUser, String codReservation) throws ReservationNotExistException, ReservationAlreadyCanceledException {

        wl.lock();

        try {

            Account acc = this.accountsMap.get(idUser);
            acc.cancelReservation(codReservation);

            if (acc.getReseervations().containsKey(codReservation)){
                Reservation r = acc.getReseervations().get(codReservation);
                List<String> route = r.getRoute();
                for (int i = 0; i < route.size()-1; i++) {
                    List<Flight> flightList = this.flightsMap.get(route.get(i));
                    Flight f = getFlightFromList(flightList,route.get(i+1));
                    f.reduceOccupationDate(r.getDay());
                }
            }
        } finally {
            wl.unlock();
        }

    }

    public List<AbstractMap.Entry<String, String>> getFlightsList() {

        wl.lock();

        try {

            List<AbstractMap.Entry<String, String>> res = new ArrayList<>();
            for (Map.Entry<String, List<Flight>> flightsOrigin : this.flightsMap.entrySet()) {
                String originCity = flightsOrigin.getKey();
                for (Flight f : flightsOrigin.getValue()) {
                    res.add(new AbstractMap.SimpleEntry(originCity, f.getDestination()));
                }
            }
            return res;

        } finally {
            wl.unlock();
        }

    }


//FUNCIONALIDADES ADICIONAIS:
    //1. Obtenção de uma lista com todos os percursos possíveis para viajar entre uma origem e um des-
    //tino, limitados a duas escalas (três voos). Minimize a quantidade de dados transferidos.


    public List<List<String>> percursosComEscalas(String origin, String destination) throws OriginNotFoundOnMapException {


        wl.lock();

        try {



            List<List<String>> res = new ArrayList<>();
            if (!flightsMap.containsKey(origin)) {
                throw new OriginNotFoundOnMapException(origin);
            }
            List<Flight> flightsFromOrigin = flightsMap.get(origin);
            Flight fl;
            List<String> subList = new ArrayList<>();
            subList.add(origin);


                if ((fl = hasDestination(flightsFromOrigin, destination)) != null) {//se tem destino diretamente para o pretendido, adiciona à lista e dá logo return
                    List<String> destination0Scale = new ArrayList<>(subList);
                    destination0Scale.add(fl.getDestination());
                    res.add(destination0Scale);
                    flightsFromOrigin = flightsFromOrigin.stream().filter(f4 -> !f4.getDestination().equals(destination)).collect(Collectors.toList());
                }                                                                                 //caso nao tenha voo direto para o destino, pegar nos destinos desse e calcular se há algum que vá para o destino final

                for (Flight f : flightsFromOrigin) {
                    System.out.println("destination " + f.getDestination());
                    if (flightsMap.containsKey(f.getDestination())) {
                        List<Flight> flightsFromItermediate = flightsMap.get(f.getDestination());
                        if ((fl = hasDestination(flightsFromItermediate, destination)) != null) {          //se tem destino da primeira escala para o pretendido, adicionamos essa escala à resposta e devolvemos
                            List<String> destination1Scale = new ArrayList<>(subList);
                            destination1Scale.add(f.getDestination());
                            destination1Scale.add(destination);
                            res.add(destination1Scale);
                            flightsFromItermediate = flightsFromItermediate.stream().filter(f5 -> !f5.getDestination().equals(destination)).collect(Collectors.toList());
                        }

                        //caso contrario, vamos verificar se há uma segunda escala que nos leve para esse destino

                        for (Flight f2 : flightsFromItermediate) {//procurar se há uma segunda escala que nos leve ao destino, caso conntrario retornamos null pq atingimos o nº maximo de escalas
                            if (flightsMap.containsKey(f2.getDestination())) {
                                List<Flight> flightToDestination = flightsMap.get(f2.getDestination());
                                if (hasDestination(flightToDestination, destination) != null) {
                                    List<String> destination2Scale = new ArrayList<>(subList);
                                    destination2Scale.add(f.getDestination());
                                    destination2Scale.add(f2.getDestination());
                                    destination2Scale.add(destination);
                                    res.add(destination2Scale);
                                }
                            }
                        }
                    }
                }




            return res;

        } finally {
            wl.unlock();
        }


    }


    public Flight hasDestination(List<Flight> flightsList, String destination) {


        wl.lock();

        try {

            for (Flight f : flightsList) {
                if (f.getDestination().equals(destination)) {
                    return f;
                }
            }
            return null;

        } finally {
            wl.unlock();
        }

    }
}







