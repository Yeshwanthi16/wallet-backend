package com.example.wallet.apierror;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
@NoArgsConstructor@AllArgsConstructor@Data
public class Apierror {

    private String errorDescription;

    private int responseCode;

}
