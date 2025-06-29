package com.example.movie_booking_backend.model.dto;

import lombok.Data;

@Data
public class SeatStatusUpdateDTO {
    private Long id;
    private String status;
    private Long SessionId;
    private String Status;
    private Long seatId;
}
