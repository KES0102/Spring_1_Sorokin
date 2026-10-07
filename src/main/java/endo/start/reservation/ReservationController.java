package endo.start.reservation;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);


    private final ReservationService reservationService;


    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        log.info("Called getReservationById: id = " + id);
        return ResponseEntity.status(
                HttpStatus.OK).
                body(reservationService.getReservationById(id));
    }


    @GetMapping("/all")
    public ResponseEntity<List<Reservation>> getReservationAll(
            @RequestParam("roomId") Long roomId,
            @RequestParam("userId") Long userId,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam("pageNumber") Integer pageNumber
    ) {
        log.info("Called getReservationAll");

        var filter = new ReservationSearchFilter(roomId, userId, pageSize, pageNumber);
        return ResponseEntity.ok(reservationService.searchAllByFilter(filter));

    }


    @PostMapping()
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid Reservation reservationToCreate
    ) {
        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED).
                header("my-header", "Good header").
                body(reservationService.createReservation(reservationToCreate));

    }


    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid Reservation reservationToUpdate) {
        log.info("Called method reservationToUpdate id = {}; reservationToUpdate = {}", id, reservationToUpdate);
        var updated = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(updated);



    }


    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> canselReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called method deleteReservation: id = {}", id);
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        }

    }


    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(@PathVariable Long id) {
        log.info("Called approveReservation");
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.status(HttpStatus.OK).body(reservation);


    }
}
