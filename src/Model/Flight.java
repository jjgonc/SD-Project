package Model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Flight implements Serializable {

    String destination;
    int capacity;
    Map<LocalDate, Integer> occupations; // so tem os dias de viagem ja registadas



//CONSTRUCTORS
    public Flight (String destination, int capacity, Map<LocalDate, Integer> occupations) {
        this.destination = destination;
        this.capacity = capacity;
        this.occupations = occupations;
    }


//GETTERS AND SETTERS

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }


    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }


    public Map<LocalDate, Integer> getOccupations(){
        Map<LocalDate, Integer> res = new HashMap<>();
        for(var entry : occupations.entrySet()) {
            res.put(entry.getKey(), entry.getValue());
        }
        return res;
    }

    public void setOccupations (Map<LocalDate, Integer> inOccupations) {
        for(var entry : inOccupations.entrySet()) {
            this.occupations.put(entry.getKey(), entry.getValue());
        }
    }


    public int getOccupationDate (LocalDate date) {
        if (occupations.containsKey(date)) {
            return occupations.get(date);
        } else {
            return 0;
        }

    }

    public void setOccupationDate (LocalDate date, int ocupation) {
        this.occupations.put(date,ocupation);
    }


    // FIXME adicionar lock depois pq se tiverem dois ao mesmo tempo podem ler 1 lugar disponivel os dois
    public int seatsLeft (LocalDate date) {
        if ( this.occupations.containsKey(date)) {
            return (this.capacity - getOccupationDate(date));
        } else {
            return capacity;
        }
    }

    // utilizada quando cancela reserva entao tira 1 de ocupaçao
    public void reduceOccupationDate(LocalDate date) {
        int occ = this.occupations.get(date) - 1;
        this.setOccupationDate(date,occ);
        if (this.occupations.get(date) == 0) { // se tiver 0 ocupacoes remove a data do map
            this.occupations.remove(date);
        }
    }

}