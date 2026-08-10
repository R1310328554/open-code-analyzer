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
package org.keycloak.subsystem.adapter.saml.extension.logging;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.MessageBundle;

/**
 * Keycloak SAML WildFly 子系统的国际化消息束接口。
 *
 * <p>后续错误消息完全外部化时可继续扩展本接口。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
@MessageBundle(projectCode = "KEYCLOAK")
public interface KeycloakMessages {

    /**
     * 消息束单例，供子系统代码获取本地化字符串。
     */
    KeycloakMessages MESSAGES = Messages.getBundle(KeycloakMessages.class);
}
