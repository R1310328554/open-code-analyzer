/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl;

import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 修复 BCrypt 密码长度漏洞的安全密码编码器。
 *
 * <p>问题：原 {@link BCryptPasswordEncoder} 仅比较前 72 字符，超长密码可能被误判为相同。</p>
 *
 * <p>修复：在 {@link #matches(CharSequence, String)} 中若明文长度超过 {@link AuthConstants#MAX_PASSWORD_LENGTH} 则直接返回 false。</p>
 *
 * <p><strong>建议：</strong>注册与改密流程也应限制密码长度，避免历史脏数据导致无法登录。</p>
 *
 * @see <a href="https://github.com/advisories/GHSA-mg83-c7gq-rv5c">Spring Security Password Length Vulnerability Advisory</a>
 * @author linwumignshi
 */
public class SafeBcryptPasswordEncoder extends BCryptPasswordEncoder {
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 超长密码直接拒绝，避免 BCrypt 截断比较
        if (rawPassword != null && rawPassword.length() > AuthConstants.MAX_PASSWORD_LENGTH) {
            return false;
        }
        return super.matches(rawPassword, encodedPassword);
    }
}
