package endo.start.reservation;

import endo.start.reservation.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReservationService {

    private final ReservationMapper mapper;
    private final DBReservationRepository db_reservation;

    private final ReservationAvailabilityService serviceAvailability;


    public ReservationService(ReservationMapper mapper, DBReservationRepository dbReservation, ReservationAvailabilityService service) {
        this.mapper = mapper;
        this.db_reservation = dbReservation;

        this.serviceAvailability = service;
    }


    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = db_reservation.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("Not found reservation by id = "+id
                ));

        return mapper.toReservation(reservationEntity);
    }


    public List<Reservation> searchAllByFilter(ReservationSearchFilter filter) {

        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);


        List<ReservationEntity> allEntities = db_reservation.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

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

        var isAvailableToApprove =  serviceAvailability.isReservationAvailable(reservationEntity.getRoomId(), reservationEntity.getStartDate(), reservationEntity.getEndDate());

        if (!isAvailableToApprove) {
            throw new IllegalStateException("Cannot approve reservation because conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);

        db_reservation.save(reservationEntity);

        return mapper.toReservation(reservationEntity);
    }


    //Дополнительные методы



}
