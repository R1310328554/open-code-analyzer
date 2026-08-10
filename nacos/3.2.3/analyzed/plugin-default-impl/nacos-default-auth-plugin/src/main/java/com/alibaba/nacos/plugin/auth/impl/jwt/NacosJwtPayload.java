/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.jwt;

import com.alibaba.nacos.common.utils.JacksonUtils;

/**
 * Nacos JWT 载荷（Payload）数据模型。
 *
 * <p>包含 {@code sub}（用户名）与 {@code exp}（过期时间戳，秒）， 序列化为 JSON 后做 URL Base64 编码嵌入 Token。</p>
 *
 * @author Weizhan▪Yun
 * @date 2023/1/15 21:27
 */
public class NacosJwtPayload {
    
    /** JWT subject：登录用户名。 */
    private String sub;
    
    /** 过期时间戳（Unix 秒），默认当前时间。 */
    private long exp = System.currentTimeMillis() / 1000L;
    
    /** 获取用户名（subject）。 */
    public String getSub() {
        return sub;
    }
    
    /** 设置用户名（subject）。 */
    public void setSub(String sub) {
        this.sub = sub;
    }
    
    /** 获取过期时间戳（秒）。 */
    public long getExp() {
        return exp;
    }
    
    /** 设置过期时间戳（秒）。 */
    public void setExp(long exp) {
        this.exp = exp;
    }
    
    /** 序列化为 JSON 字符串供 JWT Payload 编码。 */
    @Override
    public String toString() {
        return JacksonUtils.toJson(this);
    }
}
