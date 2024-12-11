package com.example.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.entity.Menuimage;
import com.example.repository.MenuImageRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private MenuImageRepository menuImageRepository;

    @GetMapping(value = "home.do")
    public String homeGET() {
        return "home";
    }

    // 이미지 반환 (Base64로 반환)
    @GetMapping(value = "/image")
    public ResponseEntity<byte[]> imagePreview(@RequestParam(name = "no") long no) throws IOException {
        Menuimage obj = menuImageRepository.findById(no).orElse(null);
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<byte[]> response = null;

        // DB에 이미지가 있는 경우
        if (obj != null) {
            if (obj.getFiledata().length > 0) {
                headers.setContentType(MediaType.parseMediaType(obj.getFiletype()));
                response = new ResponseEntity<>(obj.getFiledata(), headers, HttpStatus.OK);
                return response;
            }
        }
        // DB에 이미지가 없는 경우(Default image)
        InputStream in = resourceLoader.getResource("classpath:/static/img/Defaultimage.png").getInputStream();
        headers.setContentType(MediaType.IMAGE_PNG);
        response = new ResponseEntity<>(in.readAllBytes(), headers, HttpStatus.OK);
        return response;
    }

}
