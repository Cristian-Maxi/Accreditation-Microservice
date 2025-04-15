package com.microservice.accreditations.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T>  implements Serializable {

    boolean estado;
    String message;
    T data;
    Iterable<T> dataIterable;

    // Constructor for a response with a simple object
    public ApiResponseDTO(boolean estado, String message, T data){
        this.estado=estado;
        this.message=message;
        this.data= data;
    }

    // Constructor for a response with an Iterable (list)
    public ApiResponseDTO(boolean estado, String message, Iterable<T> dataIterable){
        this.estado=estado;
        this.message=message;
        this.dataIterable=dataIterable;
    }
}
