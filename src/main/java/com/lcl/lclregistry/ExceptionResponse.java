package com.lcl.lclregistry;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * @Author conglongli
 * @date 2024/4/23 23:17
 */
@Data
@AllArgsConstructor
public class ExceptionResponse {

    HttpStatus httpStatus;
    String message;
}
