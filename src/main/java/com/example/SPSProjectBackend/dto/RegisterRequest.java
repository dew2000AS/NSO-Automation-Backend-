package com.example.SPSProjectBackend.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
public class RegisterRequest {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String eAccountNo;

}

