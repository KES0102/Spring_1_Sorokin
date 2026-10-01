package endo.start;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {


    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;

    private final DBReservationRepository db_reservation;


    public ReservationService(DBReservationRepository dbReservation) {
        this.db_reservation = dbReservation;
        reservationMap = new HashMap<>();
        idCounter=new AtomicLong();
    }


    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = db_reservation.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("Not found reservation by id = "+id
                ));

        return toDomainReservation(reservationEntity);
    }


    public List<Reservation> getAllReservation() {

        List<ReservationEntity> allEntities = db_reservation.findAll();
         return allEntities.stream().map(it->
                 toDomainReservation(it)).toList();
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
        Reservation r = reservationMap.get(id);
        reservationMap.put(id, new Reservation(id, r.userId(), r.roomId(), r.startDate(), r.endDate(), ReservationStatus.CANCELLED));

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

        for (Reservation r : reservationMap.values()) {
            if (r.id().equals(reservation.id())) {
                continue;
            }
            if (!r.roomId().equals(reservation.roomId())) {
                continue;
            }
            if (!r.status().equals(ReservationStatus.APPROVED)) {
                continue;
            }
            if ( reservation.startDate().isBefore(r.endDate())
                && r.startDate().isBefore(reservation.endDate()) ) {
                return true;

            }

        }
        return false;
    }

    private Reservation toDomainReservation(ReservationEntity reservationEntity){
        return new Reservation(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus()
        );
    }
}
