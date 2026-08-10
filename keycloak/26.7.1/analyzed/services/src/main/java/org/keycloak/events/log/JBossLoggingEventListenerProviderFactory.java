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

package org.keycloak.events.log;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import org.jboss.logging.Logger;

/**
 * JBoss Logging 事件监听器 SPI 工厂。
 * <p>配置成功/错误日志级别、值清理与引号包裹等行为，创建 {@link JBossLoggingEventListenerProvider} 实例。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JBossLoggingEventListenerProviderFactory implements EventListenerProviderFactory {

    /** SPI 工厂标识：{@code jboss-logging}。 */
    public static final String ID = "jboss-logging";

    /** 事件日志使用的 Logger 名称。 */
    private static final Logger logger = Logger.getLogger("org.keycloak.events");

    /** 成功事件日志级别。 */
    private Logger.Level successLevel;
    /** 错误事件日志级别。 */
    private Logger.Level errorLevel;
    /** 是否清理日志值中的空格与引号。 */
    private boolean sanitize;
    /** 日志值包裹引号字符。 */
    private Character quotes;
    /** 管理事件日志是否包含 representation JSON。 */
    private boolean includeRepresentation;

    @Override
    /** @param session 当前会话 @return JBoss Logging 事件监听器实例 */
    public EventListenerProvider create(KeycloakSession session) {
        return new JBossLoggingEventListenerProvider(session, logger, successLevel, errorLevel, quotes, sanitize, includeRepresentation);
    }

    @Override
    /** 从配置加载日志级别、清理与引号等选项。 */
    public void init(Config.Scope config) {
        successLevel = Logger.Level.valueOf(config.get("success-level", "debug").toUpperCase());
        errorLevel = Logger.Level.valueOf(config.get("error-level", "warn").toUpperCase());
        sanitize = config.getBoolean("sanitize", true);
        String quotesString = config.get("quotes", "\"");
        if (!quotesString.equals("none") && quotesString.length() > 1) {
            logger.warn("Invalid quotes configuration, it should be none or one character to use as quotes. Using default \" quotes");
            quotesString = "\"";
        }
        quotes = quotesString.equals("none")? null : quotesString.charAt(0);
        includeRepresentation = config.getBoolean("include-representation", false);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @return 日志级别、清理与 representation 等配置元数据 */
    public List<ProviderConfigProperty> getConfigMetadata() {
        String[] logLevels = Arrays.stream(Logger.Level.values())
                .map(Logger.Level::name)
                .map(String::toLowerCase)
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        return ProviderConfigurationBuilder.create()
                .property()
                .name("success-level")
                .type("string")
                .helpText("The log level for success messages.")
                .options(logLevels)
                .defaultValue("debug")
                .add()
                .property()
                .name("error-level")
                .type("string")
                .helpText("The log level for error messages.")
                .options(logLevels)
                .defaultValue("warn")
                .add()
                .property()
                .name("sanitize")
                .type("boolean")
                .helpText("If true the log messages are sanitized to avoid line breaks. If false messages are not sanitized.")
                .defaultValue("true")
                .add()
                .property()
                .name("quotes")
                .type("string")
                .helpText("The quotes to use for values, it should be one character like \" or '. Use \"none\" if quotes are not needed.")
                .defaultValue("\"")
                .add()
                .property()
                .name("include-representation")
                .type("boolean")
                .helpText("""
                          When "true" the "representation" field with the JSON admin object is also added to the message.
                          The realm should be also configured to include representation for the admin events.
                          """)
                .defaultValue("false")
                .add()
                .build();
    }
}
