package com.example.demo.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface IService {
    String createSwagger(HttpServletRequest httpServletRequest, Map<String, String> requestHeaders, Map<String, String> requestParams, String stringData) throws JsonProcessingException;
}
