/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.auth.config;

/**
 * 鉴权相关错误码枚举（以 5000X 开头）。
 *
 * @author xiweng.yy
 */
public enum AuthErrorCode {
    
    /** 鉴权插件类型无效或未配置。 */
    INVALID_TYPE(50001,
        "Invalid auth type, Please set `nacos.core.auth.system.type`, detail: https://nacos.io/docs/latest/manual/admin/auth/"),
    
    /** 服务端身份 key/value 未配置。 */
    EMPTY_IDENTITY(50002,
        "Empty identity, Please set `nacos.core.auth.server.identity.key` and `nacos.core.auth.server.identity.value`, detail: https://nacos.io/docs/latest/manual/admin/auth/");
    
    /** 数值错误码。 */
    private final Integer code;
    
    /** 错误描述信息。 */
    private final String msg;
    
    /** 返回数值错误码。 */
    public Integer getCode() {
        return code;
    }
    
    /** 返回错误描述文本。 */
    public String getMsg() {
        return msg;
    }
    
    /** 构造错误码常量。 */
    AuthErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
