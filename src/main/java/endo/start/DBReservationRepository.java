package endo.start;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DBReservationRepository extends JpaRepository<ReservationEntity, Long> {
}
