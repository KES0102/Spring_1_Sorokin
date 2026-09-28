package endo.start;

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
        try {
            return ResponseEntity.status(
                            HttpStatus.OK).
                    body(reservationService.
                            getReservationById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        }

    }


    @GetMapping("/all")
    public ResponseEntity<List<Reservation>> getReservationAll() {
        log.info("Called getReservationAll");
        return ResponseEntity.ok(reservationService.getAllReservation());

    }


    @PostMapping()
    public ResponseEntity<Reservation> createReservation(
            @RequestBody Reservation reservationToCreate
    ) {
        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED).
                header("my-header", "Good header").
                body(reservationService.createReservation(reservationToCreate));

    }


    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody Reservation reservationToUpdate) {
        log.info("Called method reservationToUpdate id = {}; reservationToUpdate = {}", id, reservationToUpdate);

        try {
            var updated = reservationService.updateReservation(id, reservationToUpdate);
            return ResponseEntity.status(HttpStatus.OK).body(updated);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build();
        }


    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called method deleteReservation: id = {}", id);
        try {
            reservationService.deleteReservation(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        }

    }


    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(@PathVariable Long id) {
        log.info("Called approveReservation");
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }
}
