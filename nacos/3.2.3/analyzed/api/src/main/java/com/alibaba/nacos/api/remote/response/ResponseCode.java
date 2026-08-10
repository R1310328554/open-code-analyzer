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

package com.alibaba.nacos.api.remote.response;

/**
 * 远程 RPC 响应状态码枚举。
 *
 * <p>定义 {@link Response} 中 {@code resultCode} 的常用取值，与 HTTP 语义类似：{@link #SUCCESS} 表示处理成功，{@link #FAIL} 表示失败。</p>
 *
 * @author liuzunfei
 * @version $Id: ResponseCode.java, v 0.1 2020年07月14日 2:04 PM liuzunfei Exp $
 */
public enum ResponseCode {
    
    /** 请求处理成功（状态码 200）。 */
    SUCCESS(200, "Response ok"),
    
    /** 请求处理失败（状态码 500）。 */
    FAIL(500, "Response fail");
    
    /** 数值状态码。 */
    int code;
    
    /** 状态描述文本。 */
    String desc;
    
    /** 构造枚举常量并绑定状态码与描述。 */
    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /** 返回数值状态码。 */
    public int getCode() {
        return code;
    }
    
    /** 返回状态描述文本。 */
    public String getDesc() {
        return desc;
    }
}
