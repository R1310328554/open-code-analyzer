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

package com.alibaba.nacos.client.auth.ram.identify;

/**
 * Spas Credential Interface.
 * <p>SPAS（Security Product Access Service）凭证接口：抽象 AccessKey 与 SecretKey 的读取，供 {@link SpasCredentialLoader} 加载后用于 RAM 签名鉴权。</p>
 *
 * @author Nacos
 */
public interface SpasCredential {
    
    /**
     * get AccessKey.
     * <p>返回阿里云 RAM AccessKey ID。</p>
     *
     * @return AccessKey
     */
    String getAccessKey();
    
    /**
     * get SecretKey.
     * <p>返回与 AccessKey 配对的 SecretKey，用于 HMAC 签名。</p>
     *
     * @return SecretKey
     */
    String getSecretKey();
}
