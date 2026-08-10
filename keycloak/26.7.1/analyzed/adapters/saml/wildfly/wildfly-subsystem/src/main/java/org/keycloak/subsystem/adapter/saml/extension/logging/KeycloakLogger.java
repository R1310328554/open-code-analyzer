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

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import static org.jboss.logging.Logger.Level.INFO;

/**
 * Keycloak SAML WildFly 子系统的 JBoss Logging 消息日志接口。
 *
 * <p>后续错误消息完全外部化时可继续扩展本接口。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
@MessageLogger(projectCode = "KEYCLOAK")
public interface KeycloakLogger extends BasicLogger {

    /**
     * 以包名作为日志分类的根日志记录器。
     */
    KeycloakLogger ROOT_LOGGER = Logger.getMessageLogger(KeycloakLogger.class, "org.jboss.keycloak");

    /**
     * 记录某部署已被 Keycloak 子系统配置覆盖保护。
     *
     * @param deployment 部署单元名称
     */
    @LogMessage(level = INFO)
    @Message(value = "Keycloak subsystem override for deployment %s")
    void deploymentSecured(String deployment);


}
