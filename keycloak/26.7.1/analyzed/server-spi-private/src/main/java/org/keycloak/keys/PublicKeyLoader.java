/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
 */

package org.keycloak.keys;

import org.keycloak.crypto.PublicKeysWrapper;

/**
 * 公钥加载回调：由 {@link PublicKeyStorageProvider} 在缓存未命中时调用以拉取远程 JWK/JWKS。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface PublicKeyLoader {

    /**
     * 加载公钥集合。
     *
     * @return 公钥包装
     * @throws Exception 拉取或解析失败
     */
    PublicKeysWrapper loadKeys() throws Exception;

}
