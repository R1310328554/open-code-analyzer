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

package org.keycloak.truststore;

/**
 * {@link TruststoreProvider} 的全局单例持有者，供 {@link SSLSocketFactory} 等静态上下文访问。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
class TruststoreProviderSingleton {

    static private TruststoreProvider provider;

    /** 注册全局信任库提供者实例。 */
    static void set(TruststoreProvider tp) {
        provider = tp;
    }

    /** 返回已注册的信任库提供者，未初始化时为 null。 */
    static TruststoreProvider get() {
        return provider;
    }
}
