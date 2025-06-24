package com.example.movie_booking_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan({"com.example.movie_booking_backend.mapper"})
@EnableScheduling
public class MovieBookingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieBookingBackendApplication.class, args);
    }

}
