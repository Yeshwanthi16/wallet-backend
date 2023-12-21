package com.example.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
@NoArgsConstructor
@Data
@AllArgsConstructor
public class ApiResponse {

    private HttpStatus status;

    private String response;

}
