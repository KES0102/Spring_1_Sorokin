package endo.start.reservation.availability;

import endo.start.reservation.DBReservationRepository;
import endo.start.reservation.ReservationController;
import endo.start.reservation.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service

public class ReservationAvailabilityService {

    private final DBReservationRepository db_reservations;

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    ReservationAvailabilityService(DBReservationRepository dbReservations) {
        this.db_reservations = dbReservations;
    }

    public boolean isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate)
    {
        log.info("Called method isReservationAvailable");

        if ( ! endDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be 1 day earlier that end date");
        }

        List<Long> conflictIds = db_reservations.findConflictresrvationIds(roomId, startDate, endDate, ReservationStatus.APPROVED);

        if(conflictIds.isEmpty()){
            return   true;
        }
        return false;

    }
}
