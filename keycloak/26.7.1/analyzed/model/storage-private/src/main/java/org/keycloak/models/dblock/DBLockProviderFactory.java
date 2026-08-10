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

package org.keycloak.models.dblock;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link DBLockProvider} 工厂 SPI 接口，用于创建数据库锁 Provider 实例。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface DBLockProviderFactory extends ProviderFactory<DBLockProvider> {

    /** 测试场景下覆盖锁重检间隔与等待超时（毫秒）。 */
    void setTimeouts(long lockRecheckTimeMillis, long lockWaitTimeoutMillis);
}
