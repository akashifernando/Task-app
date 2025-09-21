package com.project.TaskApp.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
//generic class return all respons
// //data tranfer clean
@Data
@Builder //access the class build method without having new instance create an object
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
   private boolean success;
    private int statusCode;
    private String message;
    private T data;//actual paylaod to return
//gives API reply  same simple envelope—status code, message, data
public static <T> Response<T> ok(T data) {
    return Response.<T>builder()
            .statusCode(200)
            .message("OK")
            .data(data)
            .build();
}

    public static <T> Response<T> created(T data) {
        return Response.<T>builder()
                .statusCode(201)
                .message("Created")
                .data(data)
                .build();
    }

    public static <T> Response<T> noContent() {
        return Response.<T>builder()
                .statusCode(204)
                .message("No Content")
                .build();
    }

    public static <T> Response<T> error(int statusCode, String message) {
        return Response.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .build();
    }
}
