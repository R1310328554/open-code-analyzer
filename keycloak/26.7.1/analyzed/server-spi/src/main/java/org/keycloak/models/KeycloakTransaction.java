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

package org.keycloak.models;

/**
 * Keycloak 事务抽象：begin/commit/rollback 生命周期。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface KeycloakTransaction {
    /** 启动事务。 */
    void begin();
    /** 提交事务。 */
    void commit();
    /** 回滚事务。 */
    void rollback();
    /** 标记仅回滚（不可提交）。 */
    void setRollbackOnly();
    /** @return 是否已标记仅回滚 */
    boolean getRollbackOnly();
    /** @return 事务是否处于活动状态 */
    boolean isActive();
}
