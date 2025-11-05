package com.example.demo.service.impl;

import com.example.demo.service.IService;
import com.example.demo.service.SwaggerExcludeFields;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceImpl implements IService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();
    private int count;

    @Override
    public String createSwagger(HttpServletRequest httpServletRequest, Map<String, String> requestHeaders, Map<String, String> requestParams, String stringData) throws JsonProcessingException {
        count = 1;
        Map<String, Object> data;
        String contentType = requestHeaders.get("content-type");
        Map<String, Object> requestBody;
        Object response;
        if (contentType.contains("json")) {
            data = objectMapper.readValue(stringData, Map.class);
        } else {
            data = xmlMapper.readValue(stringData, Map.class);
        }
        requestBody = (Map<String, Object>) data.get("request");
        response = data.get("response");
        Swagger swagger = new Swagger();
        swagger.setSwagger("2.0");
        Info info = new Info();
        info.setTitle(data.get("title").toString());
        info.setVersion(data.get("version").toString());
        swagger.setInfo(info);
        swagger.setHost(data.get("host").toString());
        swagger.setBasePath("/");
        swagger.setSchemes(List.of(Scheme.HTTP));
        Map<String, Model> definitions = new HashMap<>();
        Map<String, Path> paths = new HashMap<>();
        Path path = new Path();
        Operation operation = new Operation();
        operation.setConsumes(List.of(contentType));
        operation.setProduces(List.of(contentType));
        operation.setParameters(getHeaderAndQueryParameters(requestHeaders, requestParams));
        if (!requestBody.isEmpty()) {
            operation.addParameter(handleBodyParameter(definitions, requestBody, contentType));
        }
        if (response instanceof Map) {
            operation.setResponses(handleResponse(definitions, response));
        }
        path.set(httpServletRequest.getMethod().toLowerCase(), operation);
        paths.put(httpServletRequest.getRequestURI(), path);
        swagger.setPaths(paths);
        swagger.setDefinitions(definitions);


        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.addMixIn(Object.class, SwaggerExcludeFields.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
    }

    private List<Parameter> getHeaderAndQueryParameters(Map<String, String> requestHeaders, Map<String, String> requestParams) {
        List<Parameter> list = new ArrayList<>();
        for (String header : requestHeaders.keySet()) {
            HeaderParameter parameter = new HeaderParameter();
            parameter.setName(header);
            parameter.setIn("header");
            parameter.setDescription("");
            parameter.setType("string");
            list.add(parameter);
        }
        for (String query : requestParams.keySet()) {
            QueryParameter parameter = new QueryParameter();
            parameter.setName(query);
            parameter.setIn("query");
            parameter.setDescription("");
            parameter.setType("string");
            list.add(parameter);
        }
        return list;
    }

    private Map<String, Response> handleResponse(Map<String, Model> definitions, Object responseBody, String contentType) {
        Response response = new Response();
        response.setDescription("a Response to be returned");
        if (responseBody instanceof Map) {
            if (!contentType.contains("json")) {
                Map<String, Object> responseMap = (Map<String, Object>) responseBody;
                String name = responseMap.keySet().toArray()[0].toString();
                definitions.putAll(addDefinition(name, (Map<String, Object>) responseMap.get(name)));
                response.setResponseSchema(new RefModel("#/definitions/" + name));
            } else {
                definitions.putAll(addDefinition("Response", (Map<String, Object>) responseBody));
                response.setResponseSchema(new RefModel("#/definitions/Response"));
            }
        } else {
            ModelImpl model = new ModelImpl();
            model.setType("String");
            response.setResponseSchema(model);
        }
        Map<String, Response> responseMap = new HashMap<>();
        responseMap.put("200", response);
        return responseMap;
    }

    private Parameter handleBodyParameter(Map<String, Model> definitions, Map<String, Object> requestBody, String contentType) {
        BodyParameter bodyParameter = new BodyParameter();
        bodyParameter.setName("body");
        bodyParameter.setIn("body");
        bodyParameter.setDescription("");
        if (!contentType.contains("json")) {
            String name = requestBody.keySet().toArray()[0].toString();
            definitions.putAll(addDefinition(name, (Map<String, Object>) requestBody.get(name)));
            bodyParameter.setSchema(new RefModel("#/definitions/" + name));
        } else {
            definitions.putAll(addDefinition("Request", requestBody));
            bodyParameter.setSchema(new RefModel("#/definitions/Request"));
        }
        return bodyParameter;
    }

    private Map<String, Model> addDefinition(String name, Map<String, Object> object) {
        Map<String, Model> modelMap = new HashMap<>();
        Map<String, Property> propertyMap = new HashMap<>();
        for (String key : object.keySet()) {
            Object item = object.get(key);
            if (item instanceof Map) {
                RefProperty property = new RefProperty();
                property.setType("object");
                property.set$ref("#/definitions/" + key + "_" + count);
                propertyMap.put(key, property);
                modelMap.putAll(addDefinition(key + "_" + count, (Map<String, Object>) item));
                count++;
            } else if (item instanceof List) {
                ArrayProperty property = new ArrayProperty();
                property.setType("array");
                Object firstItem = ((List<?>) item).get(0);
                if (firstItem.getClass().getName().startsWith("java.lang")) {
                    ObjectProperty objectProperty = new ObjectProperty();
                    objectProperty.setType(firstItem.getClass().getName().replace("java.lang.", "").toLowerCase());
                    property.setItems(objectProperty);
                    property.setExample(((List<?>) item).toArray());
                    propertyMap.put(key, property);
                } else {
                    property.setItems(new RefProperty("#/definitions/" + key + "_" + count));
                    propertyMap.put(key, property);
                    modelMap.putAll(addDefinition(key + "_" + count, (Map<String, Object>) ((List<?>) item).get(0)));
                    count++;
                }
            } else if (item.getClass().getName().startsWith("java.lang")) {
                ObjectProperty property = new ObjectProperty();
                property.setType(item.getClass().getName().replace("java.lang.", "").toLowerCase());
                property.setExample(item);
                propertyMap.put(key, property);
            }
        }
        Model model = new ModelImpl();
        model.setProperties(propertyMap);
        modelMap.put(name, model);
        return modelMap;
    }

}
