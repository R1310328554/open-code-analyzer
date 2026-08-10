/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.push.v2;

import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.naming.NamingResponseCode;

/**
 * 无需重试的推送异常。
 *
 * <p>推送任务捕获此异常后应停止重试，错误码为 {@link NamingResponseCode#NO_NEED_RETRY}。</p>
 *
 * @author xiweng.yy
 */
public class NoRequiredRetryException extends NacosRuntimeException {
    
    private static final long serialVersionUID = -7941235764759109405L;
    
    /** 构造无需重试异常，携带 NO_NEED_RETRY 响应码。 */
    public NoRequiredRetryException() {
        super(NamingResponseCode.NO_NEED_RETRY);
    }
}
