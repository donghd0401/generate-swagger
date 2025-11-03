package com.example.demo.controller;

import com.example.demo.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class Controller {
    @Autowired
    private IService service;

    @RequestMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createSwagger(
            HttpServletRequest httpServletRequest,
            @RequestHeader Map<String, String> requestHeaders,
            @RequestParam(required = false) Map<String, String> requestParams,
            @RequestBody String stringData) throws JsonProcessingException {
        return service.createSwagger(httpServletRequest, requestHeaders, requestParams, stringData);
    }
}
