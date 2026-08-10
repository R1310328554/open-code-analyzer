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

package org.keycloak.connections.jpa.updater.liquibase.lock;

import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.StandardLockService;

/**
 * 注入 Liquibase 的空实现锁服务。
 * <p>Keycloak 在调用 Liquibase 更新前已通过 {@link CustomLockService} 持有数据库锁，
 * 因此 Liquibase 内部无需再次加锁，所有方法均为空操作。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DummyLockService extends StandardLockService {

    /** 最高优先级，覆盖 Liquibase 默认 {@link StandardLockService}。 */
    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    /** 无需初始化锁表，外层已处理。 */
    @Override
    public void init() throws DatabaseException {
    }

    /** 外层已加锁，直接返回。 */
    @Override
    public void waitForLock() throws LockException {
    }

    /** 锁由外层 {@link LiquibaseDBLockProvider} 管理，此处不释放。 */
    @Override
    public void releaseLock() throws LockException {
    }

}
