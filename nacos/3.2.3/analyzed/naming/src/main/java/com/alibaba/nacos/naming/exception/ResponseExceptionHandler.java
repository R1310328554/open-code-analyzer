/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.exception;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 命名模块 REST API 全局异常处理器。
 *
 * <p>将 Nacos 异常与参数错误映射为 HTTP 状态码与响应体。</p>
 *
 * @author nkorange
 */
@ControllerAdvice(basePackages = "com.alibaba.nacos.naming")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class ResponseExceptionHandler {
    
    /**
     * 处理受检 {@link NacosException}，HTTP 状态码取 errCode。
     *
     * @param e NacosException
     * @return ResponseEntity
     */
    @ExceptionHandler(NacosException.class)
    public ResponseEntity<String> handleNacosException(NacosException e) {
        Loggers.SRV_LOG.error("got exception. {}", ExceptionUtil.getAllExceptionMsg(e));
        return ResponseEntity.status(e.getErrCode()).body(e.getMessage());
    }
    
    /**
     * 处理运行时 {@link NacosRuntimeException}。
     *
     * @param e NacosException
     * @return ResponseEntity
     */
    @ExceptionHandler(NacosRuntimeException.class)
    public ResponseEntity<String> handleNacosRuntimeException(NacosRuntimeException e) {
        Loggers.SRV_LOG.error("got exception. {}", ExceptionUtil.getAllExceptionMsg(e));
        return ResponseEntity.status(e.getErrCode()).body(e.getMessage());
    }
    
    /**
     * 处理非法参数异常，返回 400 Bad Request。
     *
     * @param ex IllegalArgumentException
     * @return ResponseEntity
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleParameterError(IllegalArgumentException ex) {
        Loggers.SRV_LOG.error("got exception. {}", ExceptionUtil.getAllExceptionMsg(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    
    /**
     * 处理缺少必填请求参数，返回 400 与参数名提示。
     *
     * @param ex {@link MissingServletRequestParameterException}
     * @return ResponseEntity
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        Loggers.SRV_LOG.error("got exception. {}", ExceptionUtil.getAllExceptionMsg(ex));
        String name = ex.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Parameter '" + name + "' is missing");
    }
    
    /**
     * 兜底处理其他未捕获异常，返回 500 与完整堆栈信息。
     *
     * @param e other exception
     * @return ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        Loggers.SRV_LOG.error("got exception. {}", ExceptionUtil.getAllExceptionMsg(e));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ExceptionUtil.getAllExceptionMsg(e));
    }
}
