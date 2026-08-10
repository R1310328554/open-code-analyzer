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

package com.alibaba.nacos.core.distributed.distro.exception;

/**
 * Distro 协议运行时异常：消息前缀统一为 {@code [DISTRO-EXCEPTION]} 便于日志过滤。
 * Distro exception.
 *
 * @author xiweng.yy
 */
public class DistroException extends RuntimeException {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1711141952413139786L;
    
    /** 构造仅含消息的 Distro 异常。 */
    public DistroException(String message) {
        super(message);
    }
    
    /** 构造带根因的 Distro 异常。 */
    public DistroException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** 返回带 {@code [DISTRO-EXCEPTION]} 前缀的消息。 */
    @Override
    public String getMessage() {
        return "[DISTRO-EXCEPTION]" + super.getMessage();
    }
}
