package com.example.movie_booking_backend.model.dto;

import lombok.Data;

@Data
public class EmailRequestDTO {
    private String email;
    private String code;

}
