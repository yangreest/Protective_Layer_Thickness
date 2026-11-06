package com.example.gtj_sd_dg240.net;


public class ApiException extends RuntimeException {

    public ApiException(String errorMessage) {
        super(errorMessage);
    }
}
