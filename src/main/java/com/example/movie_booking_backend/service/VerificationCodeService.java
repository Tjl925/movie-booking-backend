package com.example.movie_booking_backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeService {
    // 改用线程安全的ConcurrentHashMap
    private final Map<String, CodeInfo> codeStorage = new ConcurrentHashMap<>();

    private static final int CODE_EXPIRATION_MINUTES = 5;

    public String generateCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        long expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CODE_EXPIRATION_MINUTES);
        codeStorage.put(email, new CodeInfo(code, expireTime));
        System.out.println("Generated code for " + email + ": " + code); // 添加日志
        return code;
    }

    public boolean verifyCode(String email, String inputCode) {
        CodeInfo codeInfo = codeStorage.get(email);

        if (codeInfo == null) {
            System.out.println("No code found for email: " + email);
            return false;
        }

        if (System.currentTimeMillis() > codeInfo.getExpireTime()) {
            System.out.println("Code expired for email: " + email);
            codeStorage.remove(email); // 清理过期验证码
            return false;
        }

        boolean isValid = codeInfo.getCode().equals(inputCode);
        System.out.println("Verification for " + email + ": " + isValid);
        return isValid;
    }

    public void removeCode(String email) {
        codeStorage.remove(email);
        System.out.println("Code removed for email: " + email);
    }

    // 验证码信息内部类
    private static class CodeInfo {
        private final String code;
        private final long expireTime;

        public CodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public String getCode() {
            return code;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }
}