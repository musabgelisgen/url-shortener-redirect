package com.cs443.bilshortredirect.healthcheck.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/healthCheck")
    public ResponseEntity<?> healthCheck(@RequestHeader Map<String, String> map){

        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
