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

package com.alibaba.nacos.console.exception;

import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.common.model.RestResultUtils;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.core.utils.Commons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.util.HtmlUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 控制台全局异常处理器：统一将鉴权、参数与运行时异常转换为 HTTP 响应。
 * Exception handler for console module.
 *
 * @author nkorange
 * @since 1.2.0
 */
@ControllerAdvice
public class ConsoleExceptionHandler {
    
    /** 控制台异常日志记录器 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleExceptionHandler.class);
    
    /** 处理鉴权失败，返回 403 与错误信息 */
    @ExceptionHandler(AccessException.class)
    private ResponseEntity<String> handleAccessException(AccessException e) {
        LOGGER.error("got exception. {}", e.getErrMsg());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getErrMsg());
    }
    
    /** 处理非法参数，返回 400 与完整异常链信息 */
    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionUtil.getAllExceptionMsg(e));
    }
    
    /** 处理 {@link NacosRuntimeException}，按异常码返回对应 HTTP 状态 */
    @ExceptionHandler(NacosRuntimeException.class)
    private ResponseEntity<String> handleNacosRuntimeException(NacosRuntimeException e) {
        LOGGER.error("got exception. {}", e.getMessage());
        return ResponseEntity.status(e.getErrCode()).body(ExceptionUtil.getAllExceptionMsg(e));
    }
    
    /**
     * 兜底处理未捕获异常；v2 API 返回 {@link RestResultUtils} 结构，其余路径 HTML 转义后返回 500。
     */
    @ExceptionHandler(Exception.class)
    private ResponseEntity<Object> handleException(HttpServletRequest request, Exception e) {
        String uri = request.getRequestURI();
        LOGGER.error("CONSOLE {}", uri, e);
        if (uri.contains(Commons.NACOS_SERVER_VERSION_V2)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RestResultUtils
                    .failed(HtmlUtils.htmlEscape(ExceptionUtil.getAllExceptionMsg(e), "utf-8")));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(HtmlUtils.htmlEscape(ExceptionUtil.getAllExceptionMsg(e), "utf-8"));
    }
}
