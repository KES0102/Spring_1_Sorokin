package endo.start;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {


    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;


    public ReservationService() {
        reservationMap = new HashMap<>();
        idCounter=new AtomicLong();
    }


    public Reservation getReservationById(Long id) {
        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Not found reservation by id = "+id);
        }
        return reservationMap.get(id);
    }


    public List<Reservation> getAllReservation() {
        return reservationMap.values().stream().toList();
    }


    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.id() != null) {
            throw new IllegalArgumentException("Id shoud be empty");
        }
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status shoud be empty");
        }
        Reservation newReservation = new Reservation(
                idCounter.incrementAndGet(),
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );


        reservationMap.put(newReservation.id(), newReservation);
        return newReservation;
    }


    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation by id = " + id);
        }
        var reservation = reservationMap.get(id);
        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status= " + reservation.status());
        }
        Reservation updateReservation = new Reservation(
                id,
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        reservationMap.put(id, updateReservation);
        return updateReservation;
    }


    public void deleteReservation(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation by id = " + id);
        }
        reservationMap.remove(id);
    }


    public Reservation approveReservation(Long id) {

        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation by id = " + id);
        }

        var reservation = reservationMap.get(id);


        if(reservation.status()!=ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status= " + reservation.status());
        }

        var isConflict = isReservationConflict(reservation);
        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservation because conflict");
        }

        Reservation approvedReservation = new Reservation(
                id,
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                ReservationStatus.APPROVED
        );
        reservationMap.put(id, approvedReservation);
        return approvedReservation;
    }


    //Дополнительные методы
    public boolean isReservationConflict(Reservation reservation) {

        return false;
    }
}
