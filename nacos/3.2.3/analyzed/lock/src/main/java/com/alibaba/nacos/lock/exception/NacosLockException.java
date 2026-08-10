/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.lock.exception;

/**
 * Nacos 分布式锁模块运行时异常。
 *
 * <p>加锁/解锁失败或参数非法时由锁服务抛出，
 * 由 {@link com.alibaba.nacos.lock.remote.rpc.handler.LockRequestHandler} 捕获并转为失败响应。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/11/18 18:57
 */
public class NacosLockException extends RuntimeException {
    
    /** 无参构造。 */
    public NacosLockException() {
    }
    
    /** 以错误消息构造异常。 */
    public NacosLockException(String message) {
        super(message);
    }
    
    /** 以消息与根因构造异常。 */
    public NacosLockException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** 以根因构造异常。 */
    public NacosLockException(Throwable cause) {
        super(cause);
    }
    
    /** 完整参数构造，可控制抑制与栈追踪写入。 */
    public NacosLockException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
