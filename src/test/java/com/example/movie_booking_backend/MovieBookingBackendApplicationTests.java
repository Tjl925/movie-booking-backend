package com.example.movie_booking_backend;

import com.example.movie_booking_backend.common.utils.EmailApi;
import com.example.movie_booking_backend.mapper.UsersMapper;
import com.example.movie_booking_backend.model.domain.Users;
import jakarta.annotation.Resource;
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
    @Resource
    EmailApi emailApi;
    @Test
    void hhhh(){
        String[] to = {"2660435694@qq.com"};
        boolean result = emailApi.sendHtmlEmail("测试", "<h1>测试邮件</h1>", to);
        if (result) {
            System.out.println("邮件发送成功");
        } else {
            System.out.println("邮件发送失败");
        }
    }
    }



