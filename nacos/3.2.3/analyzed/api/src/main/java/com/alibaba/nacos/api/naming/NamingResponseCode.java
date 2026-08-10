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

package com.alibaba.nacos.api.naming;

import com.alibaba.nacos.api.common.ResponseCode;

/**
 * 命名模块业务响应码常量。
 *
 * <p>各常量取值介于 20001–29999，继承自 {@link ResponseCode}，供命名 REST/gRPC 返回使用。</p>
 *
 * @author nkorange
 * @author 1.2.0
 */
public class NamingResponseCode extends ResponseCode {
    
    /** 请求的资源不存在（HTTP 404 语义，码值 20404）。 */
    public static final int RESOURCE_NOT_FOUND = 20404;
    
    /** 无需继续重试（客户端应停止退避重试，码值 21600）。 */
    public static final int NO_NEED_RETRY = 21600;
    
}
