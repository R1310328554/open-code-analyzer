/*
 * Copyright 2016 Analytical Graphics, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.keycloak.common.crypto;

import java.security.cert.X509Certificate;


/**
 * 从客户端 X.509 证书链中提取用户身份的 SPI。
 *
 * <p>用于 X.509 客户端认证场景，将证书主题、SAN 或 PEM 等形式映射为 Keycloak 用户标识。</p>
 *
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @date 7/30/2016
 */

public interface UserIdentityExtractor {

    /**
     * 从证书链中提取用户身份对象。
     *
     * @param certs 客户端提交的 X.509 证书链
     * @return 用户身份（通常为字符串或 {@link java.security.Principal}）；无法提取时返回 {@code null}
     */
    public Object extractUserIdentity(X509Certificate[] certs);

}
