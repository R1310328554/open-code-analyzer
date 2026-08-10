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

package com.alibaba.nacos.common.model.core;

/**
 * IResultCode.
 * <p>结果码接口（已废弃）：业务枚举实现此接口以提供统一 code 与 message，供 {@link RestResultUtils#buildResult} 构造响应；新代码请使用 v2 Result API。</p>
 *
 * @author klw
 * @ClassName: IResultCode
 * @Description: result code enum needs to be implemented this interface
 * @date 2019/6/28 14:44
 */
@Deprecated
public interface IResultCode {
    
    /**
     * Get the result code.
     * <p>返回数值型业务错误码。</p>
     *
     * @return code value.
     */
    int getCode();
    
    /**
     * Get the result code's message.
     * <p>返回与错误码对应的人类可读描述。</p>
     *
     * @return code's message.
     */
    String getCodeMsg();
}
