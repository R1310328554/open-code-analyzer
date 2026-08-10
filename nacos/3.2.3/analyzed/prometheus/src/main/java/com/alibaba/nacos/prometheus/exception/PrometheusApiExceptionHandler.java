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

package com.alibaba.nacos.prometheus.exception;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.model.v2.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Prometheus REST API 全局异常处理器。
 *
 * <p>仅作用于 {@code com.alibaba.nacos.prometheus.controller} 包， 将 {@link NacosException} 与 {@link NacosRuntimeException} 转为统一 {@link Result} JSON 响应。</p>
 *
 * @author karsonto
 * @date 2023/02/01
 */

@Order(-1)
@ControllerAdvice(basePackages = {"com.alibaba.nacos.prometheus.controller"})
@ResponseBody
public class PrometheusApiExceptionHandler {
    
    /** 异常日志记录器。 */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(PrometheusApiExceptionHandler.class);
    
    /** 处理受检 NacosException，返回 500 与错误消息。 */
    @ExceptionHandler(NacosException.class)
    public ResponseEntity<Result<String>> handleNacosException(NacosException e) {
        LOGGER.error("got exception. {}", e.getErrMsg());
        return ResponseEntity.internalServerError().body(Result.failure(e.getErrMsg()));
    }
    
    /** 处理运行时 NacosRuntimeException，按错误码返回 HTTP 状态。 */
    @ExceptionHandler(NacosRuntimeException.class)
    public ResponseEntity<Result<String>> handleNacosRuntimeException(NacosRuntimeException e) {
        LOGGER.error("got exception. {}", e.getMessage());
        return ResponseEntity.status(e.getErrCode()).body(Result.failure(e.getMessage()));
    }
    
}
