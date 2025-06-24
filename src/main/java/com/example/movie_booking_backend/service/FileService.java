package com.example.movie_booking_backend.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface FileService {
    Map<String, String> upload(MultipartFile file) throws IOException;
    Map<String, String> uploadFile(MultipartFile file, String subDir) throws IOException;
}
