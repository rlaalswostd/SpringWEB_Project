package com.example.dto;

import java.util.Date;

import lombok.Data;

@Data
public class Token {

    int no;

    
    String adminEmail;

    String token;

    Date expiretime;

    
    Date createAt;

}
