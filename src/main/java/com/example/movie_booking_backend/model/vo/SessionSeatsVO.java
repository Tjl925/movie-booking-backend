package com.example.movie_booking_backend.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class SessionSeatsVO {
    private List<SeatVO> seats;
    private Integer totalSeats;
    private Integer totalRows;
    private Integer totalColumns;
}