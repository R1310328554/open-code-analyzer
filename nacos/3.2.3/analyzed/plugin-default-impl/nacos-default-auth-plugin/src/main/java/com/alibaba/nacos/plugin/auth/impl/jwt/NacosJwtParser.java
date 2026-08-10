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

import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import com.alibaba.nacos.plugin.auth.impl.utils.Base64Decode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.concurrent.TimeUnit;

/**
 * Nacos JWT 解析与签发器。
 *
 * <p>根据 Base64 密钥长度自动选择 HS256/HS384/HS512， 提供 {@link JwtBuilder} 流式构建 Token 及 {@link #parse} 校验。</p>
 *
 * @author Weizhan▪Yun
 * @date 2023/1/15 21:38
 */
public class NacosJwtParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(NacosJwtParser.class);
    
    private final NacosSignatureAlgorithm signatureAlgorithm;
    
    private final Key key;
    
    /** 从 Base64 编码密钥构造解析器，密钥位数不足 256 时抛异常。 */
    public NacosJwtParser(String base64edKey) {
        this.validKey(base64edKey);
        byte[] decode = Base64Decode.decode(base64edKey);
        int bitLength = decode.length << 3;
        if (bitLength < 256) {
            String msg = "The specified key byte array is " + bitLength + " bits which "
                + "is not secure enough for any JWT HMAC-SHA algorithm.  The JWT "
                + "JWA Specification (RFC 7518, Section 3.2) states that keys used with HMAC-SHA algorithms MUST have a "
                + "size >= 256 bits (the key size must be greater than or equal to the hash "
                + "output size).  See https://tools.ietf.org/html/rfc7518#section-3.2 for more information.";
            throw new IllegalArgumentException(msg);
        }
        
        if (bitLength < 384) {
            this.signatureAlgorithm = NacosSignatureAlgorithm.HS256;
        } else if (bitLength < 512) {
            this.signatureAlgorithm = NacosSignatureAlgorithm.HS384;
        } else {
            this.signatureAlgorithm = NacosSignatureAlgorithm.HS512;
        }
        this.key = new SecretKeySpec(decode, signatureAlgorithm.getJcaName());
    }
    
    /** 校验密钥 Base64 编码格式并记录非标准编码警告。 */
    private void validKey(String base64edKey) {
        int length = base64edKey.toCharArray().length;
        if (length % 4 != 0) {
            LOG.warn("The secret Key currently in use is not a standard Base64 encoding"
                + " and will no longer be supported in future versions;");
        }
    }
    
    /** 使用当前算法对载荷签名生成完整 JWT 字符串。 */
    private String sign(NacosJwtPayload payload) {
        return signatureAlgorithm.sign(payload, key);
    }
    
    /** 创建 JWT 构建器以设置用户名与过期时间。 */
    public JwtBuilder jwtBuilder() {
        return new JwtBuilder();
    }
    
    /** 解析并校验 JWT，返回 {@link NacosUser}。 */
    public NacosUser parse(String token) throws AccessException {
        return NacosSignatureAlgorithm.verify(token, key);
    }
    
    /** 读取 JWT 过期时间戳（秒）。 */
    public long getExpireTimeInSeconds(String token) throws AccessException {
        return NacosSignatureAlgorithm.getExpiredTimeInSeconds(token, key);
    }
    
    /** 流式 JWT 构建器：设置 sub/exp 后调用 {@link #compact()} 签发。 */
    public class JwtBuilder {
        
        private final NacosJwtPayload nacosJwtPayload = new NacosJwtPayload();
        
        /** 设置 JWT subject（用户名）。 */
        public JwtBuilder setUserName(String userName) {
            this.nacosJwtPayload.setSub(userName);
            return this;
        }
        
        /** 设置 Token 有效时长（秒，自当前时间起算）。 */
        public JwtBuilder setExpiredTime(long validSeconds) {
            this.nacosJwtPayload
                .setExp(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + validSeconds);
            return this;
        }
        
        /** 完成载荷设置并返回签名后的 JWT 字符串。 */
        public String compact() {
            return sign(nacosJwtPayload);
        }
    }
}
