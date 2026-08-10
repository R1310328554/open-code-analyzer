/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.exception;

import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * Nacos API 全局异常处理器：拦截 {@link NacosApi} 标注控制器抛出的异常，映射为 HTTP 状态码与 {@link Result} 响应体。
 * Exception Handler for Nacos API.
 * @author dongyafei
 * @date 2022/7/22
 */

@Order(-1)
@ControllerAdvice(annotations = {NacosApi.class})
@ResponseBody
public class NacosApiExceptionHandler {
    
    /** 异常日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosApiExceptionHandler.class);
    
    /**
     * 处理 {@link NacosApiException}，返回对应 HTTP 状态与详细错误码。
     */
    @ExceptionHandler(NacosApiException.class)
    public ResponseEntity<Result<String>> handleNacosApiException(NacosApiException e) {
        LOGGER.error("got exception. {} {}", e.getErrAbstract(), e.getErrMsg());
        return ResponseEntity.status(e.getErrCode())
            .body(new Result<>(e.getDetailErrCode(), e.getErrAbstract(), e.getErrMsg()));
    }
    
    /**
     * 处理 {@link NacosException}，统一映射为服务端错误。
     */
    @ExceptionHandler(NacosException.class)
    public ResponseEntity<Result<String>> handleNacosException(NacosException e) {
        LOGGER.error("got exception. {}", e.getErrMsg());
        return ResponseEntity.status(e.getErrCode())
            .body(Result.failure(ErrorCode.SERVER_ERROR, e.getErrMsg()));
    }
    
    /**
     * 处理 {@link NacosRuntimeException} 运行时异常。
     */
    @ExceptionHandler(NacosRuntimeException.class)
    public ResponseEntity<Result<String>> handleNacosRuntimeException(NacosRuntimeException e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return ResponseEntity.status(e.getErrCode())
            .body(Result.failure(ErrorCode.SERVER_ERROR, e.getMessage()));
    }
    
    /** 请求体不可读时返回 400 与参数缺失错误码。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(ErrorCode.PARAMETER_MISSING, e.getMessage());
    }
    
    /** HTTP 消息转换失败时返回参数校验错误。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageConversionException.class)
    public Result<String> handleHttpMessageConversionException(HttpMessageConversionException e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(ErrorCode.PARAMETER_VALIDATE_ERROR, e.getMessage());
    }
    
    /** 数字格式错误时返回参数校验失败。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NumberFormatException.class)
    public Result<String> handleNumberFormatException(NumberFormatException e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(ErrorCode.PARAMETER_VALIDATE_ERROR, e.getMessage());
    }
    
    /** 非法参数异常时返回参数校验失败。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(ErrorCode.PARAMETER_VALIDATE_ERROR, e.getMessage());
    }
    
    /** 缺少 Servlet 请求参数时返回参数缺失错误。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(ErrorCode.PARAMETER_MISSING, e.getMessage());
    }
    
    /** HTTP 媒体类型不匹配时返回媒体类型错误。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMediaTypeException.class)
    public Result<String> handleHttpMediaTypeException(HttpMediaTypeException e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(ErrorCode.MEDIA_TYPE_ERROR, e.getMessage());
    }
    
    /** 鉴权失败时返回 403 与访问拒绝错误码。 */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessException.class)
    public Result<String> handleAccessException(AccessException e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(ErrorCode.ACCESS_DENIED, e.getErrMsg());
    }
    
    /** 数据访问、Servlet 或 IO 异常时返回 500 与数据访问错误码。 */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(
        value = {DataAccessException.class, ServletException.class, IOException.class})
    public Result<String> handleDataAccessException(Exception e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(ErrorCode.DATA_ACCESS_ERROR, e.getMessage());
    }
    
    /** 兜底处理未分类异常，返回 500 与异常消息。 */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result<String> handleOtherException(Exception e) {
        LOGGER.error("got exception. {} {}", e.getMessage(), ExceptionUtil.getAllExceptionMsg(e));
        return Result.failure(e.getMessage());
    }
}
