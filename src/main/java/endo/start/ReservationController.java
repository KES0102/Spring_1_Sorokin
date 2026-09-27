package endo.start;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    @GetMapping("/{id}")
    public Reservation getReservationById(
            @PathVariable("id") Long id
            ) {
        log.info("getReservationById");
        return reservationService.getReservationById(id);
    }

    @GetMapping("/all")
    public List<Reservation> getAllReservation() {
        log.info("getAllReservation");
        return reservationService.getAllReservation();
    }

    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservationToCreate) {
        log.info("Called createReservation");
        return reservationService.createReservation(reservationToCreate);
    }
    //111
    //222
    // Change_1 with Ubuntu
}
