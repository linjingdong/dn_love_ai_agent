package com.dn.dnaiagent.common;

import com.dn.dnaiagent.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/1/13 23:45
 **/
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private T data;
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}



