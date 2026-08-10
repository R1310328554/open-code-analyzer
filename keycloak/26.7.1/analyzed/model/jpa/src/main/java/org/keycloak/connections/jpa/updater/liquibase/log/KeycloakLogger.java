/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.jpa.updater.liquibase.log;

import java.util.logging.Level;

import liquibase.logging.core.AbstractLogger;
import org.jboss.logging.Logger;

/**
 * 将 Liquibase {@link liquibase.logging.Logger} 委托给 JBoss {@link Logger} 的适配器。
 * <p>重新映射日志级别（如 Liquibase INFO → Keycloak DEBUG），并过滤已知无害的数据库警告。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class KeycloakLogger extends AbstractLogger {

    /** 底层 JBoss 日志器，实际输出通道。 */
    private final Logger delegate;

    /** @param clazz Liquibase 组件类，用于命名日志类别 */
    public KeycloakLogger(final Class clazz) {
        super();
        this.delegate = Logger.getLogger(clazz);
    }

    /** Liquibase SEVERE → JBoss ERROR */
    @Override
    public void severe(String message) {
        this.delegate.error(message);
    }

    @Override
    public void severe(String message, Throwable e) {
        this.delegate.error(message, e);
    }

    /** Liquibase WARNING → JBoss WARN；已知级联 DROP 不支持警告降级为 DEBUG。 */
    @Override
    public void warning(String message) {
        // 级联 DROP 并非所有数据库均支持，此警告可忽略并降为 DEBUG
        if ("Database does not support drop with cascade".equals(message)) {
            this.delegate.debug(message);
        } else {
            this.delegate.warn(message);
        }
    }

    @Override
    public void warning(String message, Throwable e) {
        this.delegate.warn(message, e);
    }

    /** Liquibase INFO → JBoss DEBUG，避免迁移日志过于冗长。 */
    @Override
    public void info(String message) {
        this.delegate.debug(message);
    }

    @Override
    public void info(String message, Throwable e) {
        this.delegate.debug(message, e);
    }

    /** Liquibase DEBUG → JBoss TRACE（仅在 TRACE 开启时输出）。 */
    @Override
    public void debug(String message) {
        if (this.delegate.isTraceEnabled()) {
            this.delegate.trace(message);
        }
    }

    @Override
    public void debug(String message, Throwable e) {
        this.delegate.trace(message, e);
    }

    /** 按 {@link Level} 映射到 JBoss 对应级别；{@code OFF} 直接忽略。 */
    @Override
    public void log(Level level, String message, Throwable e) {
        if (level.equals(Level.OFF)) {
            return;
        } else if (level.equals(Level.SEVERE)) {
            this.delegate.error(message, e);
        } else if (level.equals(Level.WARNING)) {
            this.delegate.warn(message, e);
        } else if (level.equals(Level.INFO)) {
            this.delegate.debug(message, e);
        } else if (level.equals(Level.FINE) | level.equals(Level.FINER) | level.equals(Level.FINEST)) {
            if (this.delegate.isTraceEnabled())
                this.delegate.trace(message, e);
        }
    }
}
