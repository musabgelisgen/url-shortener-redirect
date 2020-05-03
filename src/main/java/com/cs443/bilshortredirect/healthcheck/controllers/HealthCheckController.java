package com.cs443.bilshortredirect.healthcheck.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@Controller
public class HealthCheckController {

    @GetMapping("/healthCheck")
    public ResponseEntity<?> healthCheck(@RequestHeader Map<String, String> map){

        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
