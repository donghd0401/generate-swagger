package com.example.demo.service;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface SwaggerExcludeFields {
    @JsonIgnore
    String getResponsesObject();

    @JsonIgnore
    String getResponseSchema();

    @JsonIgnore
    String getOriginalRef();
}
