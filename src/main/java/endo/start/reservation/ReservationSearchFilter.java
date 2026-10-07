package endo.start.reservation;

public record ReservationSearchFilter(
        Long roomId,
        Long userId,
        Long pageSize,
        Integer pageNumber
) {
}
