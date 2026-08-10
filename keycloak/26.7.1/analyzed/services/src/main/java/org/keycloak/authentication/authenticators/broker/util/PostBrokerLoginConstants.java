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

package org.keycloak.authentication.authenticators.broker.util;

/**
 * Post-Broker-Login 流程常量：定义认证会话 note 键名，用于保存序列化的 {@link BrokeredIdentityContext} 及流程状态标记。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface PostBrokerLoginConstants {

    /** 认证会话 note：Post-Broker-Login 流程中序列化的 {@link BrokeredIdentityContext}。 */
    String PBL_BROKERED_IDENTITY_CONTEXT = "PBL_BROKERED_IDENTITY_CONTEXT";

    /** 认证会话 note：true 表示 firstBrokerLogin 完成后首次经此 broker 登录触发 PBL；false 表示后续登录。 */
    String PBL_AFTER_FIRST_BROKER_LOGIN = "PBL_AFTER_FIRST_BROKER_LOGIN";

    /** 认证会话 note 前缀（后缀为 IdP alias），标记指定 broker 的 PBL 是否已成功执行。 */
    String PBL_AUTH_STATE_PREFIX = "PBL_AUTH_STATE.";
}
