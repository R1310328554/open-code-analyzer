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
 * Keycloak 事务管理器：协调多个 {@link KeycloakTransaction} 与 JTA 策略。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface KeycloakTransactionManager extends KeycloakTransaction {

    /** JTA 集成策略。 */
    enum JTAPolicy {
        /**
         * 不与 JTA 交互。
         * Do not interact with JTA at all
         *
         */
        NOT_SUPPORTED,
        /**
         * begin() 时创建新 JTA 事务；若已有 JTA 事务则挂起，Keycloak 事务结束后恢复。
         * A new JTA Transaction will be created when Keycloak TM begin() is called.  If an existing JTA transaction
         * exists, it is suspended and resumed after the Keycloak transaction finishes.
         */
        REQUIRES_NEW,
    }

    /** @return 当前 JTA 策略 */
    JTAPolicy getJTAPolicy();
    /** @param policy JTA 策略 */
    void setJTAPolicy(JTAPolicy policy);
    /** @param transaction 注册参与当前事务的子事务 */
    void enlist(KeycloakTransaction transaction);
    /** @param transaction 注册在事务完成后执行的回调事务 */
    void enlistAfterCompletion(KeycloakTransaction transaction);

    /** @param transaction 注册两阶段 prepare 阶段参与者 */
    void enlistPrepare(KeycloakTransaction transaction);
}
