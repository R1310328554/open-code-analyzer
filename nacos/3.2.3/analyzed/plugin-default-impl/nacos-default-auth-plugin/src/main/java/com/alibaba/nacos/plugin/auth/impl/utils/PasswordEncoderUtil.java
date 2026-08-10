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

package com.alibaba.nacos.plugin.auth.impl.utils;

import com.alibaba.nacos.plugin.auth.impl.SafeBcryptPasswordEncoder;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;

/**
 * 密码编解码工具类。
 *
 * <p>委托 {@link SafeBcryptPasswordEncoder} 完成 BCrypt 哈希与校验； {@link #encode} 会校验明文长度不超过 {@link AuthConstants#MAX_PASSWORD_LENGTH}。</p>
 *
 * @author nacos
 */
public class PasswordEncoderUtil {
    
    /** 校验明文密码是否与已编码哈希匹配。 */
    public static Boolean matches(String raw, String encoded) {
        return new SafeBcryptPasswordEncoder().matches(raw, encoded);
    }
    
    /**
     * 对明文密码进行 BCrypt 编码。
     *
     * @param raw password
     * @return encoded password
     */
    public static String encode(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (raw.length() > AuthConstants.MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password length must not exceed "
                + AuthConstants.MAX_PASSWORD_LENGTH + " characters");
        }
        return new SafeBcryptPasswordEncoder().encode(raw);
    }
}
