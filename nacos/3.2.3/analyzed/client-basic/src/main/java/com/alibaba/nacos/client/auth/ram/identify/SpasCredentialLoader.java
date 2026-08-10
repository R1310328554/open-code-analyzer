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
 * Spas Credential Loader.
 * <p>SPAS 凭证加载器 SPI：从本地文件、环境变量或自定义来源解析 {@link SpasCredential}，由 {@link CredentialService} 统一调度。</p>
 *
 * @author Nacos
 */
public interface SpasCredentialLoader {
    
    /**
     * get Credential.
     * <p>加载并返回当前可用的 SPAS 凭证实例。</p>
     *
     * @return Credential
     */
    SpasCredential getCredential();
}
