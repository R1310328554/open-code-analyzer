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

package com.alibaba.nacos.consistency.exception;

/**
 * 一致性协议内部运行时异常，封装 Raft/Distro 等协议层的错误。
 *
 * Conformance protocol internal exceptions.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ConsistencyException extends RuntimeException {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1935132712388069418L;
    
    /** 无参构造。 */
    public ConsistencyException() {
        super();
    }
    
    /** 指定错误消息。 */
    public ConsistencyException(String message) {
        super(message);
    }
    
    /** 指定错误消息与根因。 */
    public ConsistencyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** 以根因构造。 */
    public ConsistencyException(Throwable cause) {
        super(cause);
    }
    
    /** 完整控制栈追踪与 suppressed 行为的受保护构造。 */
    protected ConsistencyException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
