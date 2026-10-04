package endo.start;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DBReservationRepository extends JpaRepository<ReservationEntity, Long> {

   /* @Query(value = "select * from reservations r where r.status= ?1", nativeQuery = true)
    List<ReservationEntity> findAllByStatusIs( ReservationStatus status);*/

    /*@Transactional
    @Modifying
    @Query("""
             UPDATE ReservationEntity r
             set r.userId=:userId,
             r.roomId=:roo,
             r.startDate=:startDate,
             r.endDate= :endDate,
             r.status=:status 
             where r.id = :id
             """)
    int updateReservation(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ReservationStatus status
    );
*/

    @Modifying
    @Query("""
             UPDATE ReservationEntity r
             set r.status=:status
             where r.id = :id
             """)
    void setStatusCancel(
            @Param("id") Long id,
            @Param("status") ReservationStatus status
    );
}
