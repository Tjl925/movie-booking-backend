package com.example.movie_booking_backend.common.utls;


import com.example.movie_booking_backend.model.domain.Users;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;



public class SessionUtils {
    private static final String USERKEY = "sessionUser";

    public static HttpSession session() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }

    public static Users getCurrentUserInfo() {
        return (Users) session().getAttribute(USERKEY);
    }

    public static void saveCurrentUserInfo(Users admin) {
        session().setAttribute(USERKEY, admin);
    }
}
