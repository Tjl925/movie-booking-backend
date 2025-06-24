package com.example.movie_booking_backend;

import com.example.movie_booking_backend.mapper.UsersMapper;
import com.example.movie_booking_backend.model.domain.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MovieBookingBackendApplicationTests {

    @Autowired
    private UsersMapper usersMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void hh(){
        Users user = usersMapper.selectById(1);
        System.out.println(user);
    }

}
