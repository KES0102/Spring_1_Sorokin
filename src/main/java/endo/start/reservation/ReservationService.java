package endo.start.reservation;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReservationService {

    private final ReservationMapper mapper;
    private final DBReservationRepository db_reservation;


    public ReservationService(ReservationMapper mapper, DBReservationRepository dbReservation) {
        this.mapper = mapper;
        this.db_reservation = dbReservation;

    }


    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = db_reservation.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("Not found reservation by id = "+id
                ));

        return mapper.toReservation(reservationEntity);
    }


    public List<Reservation> getAllReservation() {

        /*List<ReservationEntity> reservationEntities = db_reservation.findAllByStatusIs(ReservationStatus.PENDING);
        return reservationEntities.stream().map(it->
                toDomainReservation(it)).toList();*/

        List<ReservationEntity> allEntities = db_reservation.findAll();
         return allEntities.stream().map(it->
                 mapper.toReservation(it)).toList();
    }


    public Reservation createReservation(Reservation reservationToCreate) {

        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status shoud be empty");
        }

        if ( ! reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day earlier that end date");
        }

        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );


        var savedEntity = db_reservation.save(entityToSave);
        return mapper.toReservation(savedEntity);
    }


    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {

        var reservationEntity = db_reservation.findById(id).orElseThrow(()-> new EntityNotFoundException("Not found reservation by id = "+id));

        if ( ! reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day earlier that end date");
        }
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status= " + reservationEntity.getStatus());
        }

        var entityToSave = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        var updateReservation = db_reservation.save(entityToSave);

        return mapper.toReservation(updateReservation);
    }


    @Transactional
    public void cancelReservation(Long id) {
        var reservation = db_reservation.findById(id).orElseThrow(
                () -> new NoSuchElementException("Not found reservation by id = " + id));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager please");
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel CANCELLED reservation");
        }
        db_reservation.setStatusCancel(id, ReservationStatus.CANCELLED);

    }


    public Reservation approveReservation(Long id) {

        var reservationEntity = db_reservation.findById(id).orElseThrow(()-> new EntityNotFoundException("Not found reservation by id = "+id));


        if(reservationEntity.getStatus()!=ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status= " + reservationEntity.getStatus());
        }

        var isConflict =  isReservationConflict(reservationEntity.getRoomId(), reservationEntity.getStartDate(), reservationEntity.getEndDate());

        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservation because conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);

        db_reservation.save(reservationEntity);

        return mapper.toReservation(reservationEntity);
    }


    //Дополнительные методы
    public boolean isReservationConflict(Long roomId, LocalDate startDate, LocalDate endDate) {
        List<Long> conflictIds = db_reservation.findConflictresrvationIds(roomId, startDate, endDate, ReservationStatus.APPROVED);
        if(conflictIds.isEmpty()){
            return   false;
        }
        return true;

    }


}
