/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.model.v2;

import java.io.Serializable;

/**
 * Open API v2 统一响应封装。
 *
 * <p>携带业务错误码 {@link #code}、消息 {@link #message} 与泛型载荷 {@link #data}，供 REST 接口返回 JSON 时使用。</p>
 *
 * @author dongyafei
 * @date 2022/7/12
 */
public class Result<T> implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 6258345442767540526L;
    
    /** 业务错误码，成功时为 {@link ErrorCode#SUCCESS} 对应值。 */
    private final Integer code;
    
    /** 人类可读的错误或成功消息。 */
    private final String message;
    
    /** 响应载荷（可为 {@code null}）。 */
    private final T data;
    
    /**
     * 构造完整响应。
     *
     * @param code    错误码
     * @param message 消息
     * @param data    载荷
     */
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /** 构造空载荷的成功响应。 */
    public Result() {
        this(null);
    }
    
    /** 以成功码构造带载荷的响应。 */
    public Result(T data) {
        this(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), data);
    }
    
    /** 构造无载荷的响应。 */
    public Result(Integer code, String message) {
        this(code, message, null);
    }
    
    /**
     * 返回无载荷的成功结果。
     *
     * @param <T> 载荷类型
     * @return 成功 {@link Result}
     */
    public static <T> Result<T> success() {
        return new Result<>();
    }
    
    /**
     * 返回带载荷的成功结果。
     *
     * @param <T> 载荷类型
     * @return 成功 {@link Result}
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }
    
    /**
     * 返回服务端错误，附带自定义消息。
     *
     * @return 失败 {@link Result}
     */
    public static Result<String> failure(String message) {
        return new Result<>(ErrorCode.SERVER_ERROR.getCode(), message);
    }
    
    /**
     * 按 {@link ErrorCode} 返回失败结果。
     *
     * @param <T> 载荷类型
     * @return 失败 {@link Result}
     */
    public static <T> Result<T> failure(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMsg());
    }
    
    /**
     * 按 {@link ErrorCode} 返回失败结果并携带附加数据。
     *
     * @param <T> 载荷类型
     * @return 失败 {@link Result}
     */
    public static <T> Result<T> failure(ErrorCode errorCode, T data) {
        return new Result<>(errorCode.getCode(), errorCode.getMsg(), data);
    }
    
    /**
     * 按自定义错误码与消息返回失败结果。
     *
     * @param <T>  载荷类型
     * @param code 错误码
     * @param msg  错误消息
     * @return 失败 {@link Result}
     */
    public static <T> Result<T> failure(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
    
    /** 返回包含 code、message、data 的字符串表示。 */
    @Override
    public String toString() {
        return "Result{" + "errorCode=" + code + ", message='" + message + '\'' + ", data=" + data
            + '}';
    }
    
    /** 获取业务错误码。 */
    public Integer getCode() {
        return code;
    }
    
    /** 获取响应消息。 */
    public String getMessage() {
        return message;
    }
    
    /** 获取响应载荷。 */
    public T getData() {
        return data;
    }
}
