/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.logging;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import org.keycloak.common.util.MultiSiteUtils;
import org.keycloak.config.LoggingOptions;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import io.quarkus.bootstrap.logging.InitialConfigurator;
import io.quarkus.logging.LoggingFilter;
import org.infinispan.commons.jdkspecific.ThreadCreator;
import org.jboss.logging.Logger;
import org.jboss.logmanager.ExtLogRecord;
import org.jboss.logmanager.handlers.ConsoleHandler;
import org.jboss.logmanager.handlers.FileHandler;
import org.jboss.logmanager.handlers.SyslogHandler;

/**
 * Keycloak Quarkus 日志过滤器基类：抑制已知噪声日志、在虚拟线程上异步转发日志，
 * 并为控制台/文件/Syslog 各 handler 提供具体子类。
 *
 * @author Alexander Schwartz
 */
public abstract class KeycloakLogFilter implements Filter {

    private static final Logger logger = Logger.getLogger(KeycloakLogFilter.class);

    // 仅对 sessions/offlineSessions/clientSessions/offlineClientSessions 缓存抑制 ISPN000312
    private static final Pattern ISPN000312_PATTERN = Pattern.compile(
            "^\\[Context=(" + String.join("|", InfinispanConnectionProvider.USER_SESSION_CACHE_NAME, InfinispanConnectionProvider.CLIENT_SESSION_CACHE_NAME, InfinispanConnectionProvider.OFFLINE_USER_SESSION_CACHE_NAME, InfinispanConnectionProvider.OFFLINE_CLIENT_SESSION_CACHE_NAME) + ")] ISPN000312: .*");

    // 虚拟线程同步写日志可能导致 pinning 与死锁；用单线程池异步转发以保持顺序
    private final ExecutorService executor;
    // 对应 handler 的原始 JBoss LogManager Handler
    private Handler handler;

    public KeycloakLogFilter() {
        // ThreadCreator 须在此初始化；若在 isLoggable() 中首次触发会导致递归日志
        if (ThreadCreator.useVirtualThreads() && isHandlerEnabled() && !isAsyncLoggingEnabled()) {
            executor = Executors.newSingleThreadExecutor();
        } else {
            executor = null;
        }
    }

    /** 返回本过滤器绑定的 LogManager Handler 类型。 */
    protected abstract Class<? extends Handler> getHandlerClass();

    /**
     * 对应 handler 的日志输出是否已启用。
     */
    public abstract boolean isHandlerEnabled();

    /**
     * 对应 handler 是否已启用 Quarkus 异步日志。
     */
    public abstract boolean isAsyncLoggingEnabled();

    @Override
    public boolean isLoggable(LogRecord record) {
        // ARJUNA012125 消息会先记录再抛出，属于 log-and-throw 反模式，此处直接丢弃
        // https://narayana.zulipchat.com/#narrow/channel/323714-users/topic/Message.20.22ARJUNA012125.22.20implements.20log-and-throw.20antipattern
        if (Objects.equals(record.getLevel(), Level.WARNING) && record.getLoggerName().equals("com.arjuna.ats.arjuna") && record.getMessage().startsWith("ARJUNA012125:")) {
            return false;
        }

        if (MultiSiteUtils.isPersistentSessionsEnabled()) {
            // 持久化会话场景下 ISPN000312 为预期行为（单 owner），不应作为警告输出
            // https://github.com/keycloak/keycloak/issues/39816
            if (Objects.equals(record.getLevel(), Level.WARNING) && record.getLoggerName().equals("org.infinispan.CLUSTER") && ISPN000312_PATTERN.matcher(record.getMessage()).matches()) {
                return false;
            }
        }

        if (executor != null && ThreadCreator.isVirtual(Thread.currentThread())) {
            executor.submit(new RecordLogger(ExtLogRecord.wrap(record), this));
            return false;
        }

        return true;
    }

    /** 懒加载并缓存与 {@link #getHandlerClass()} 匹配的原始 handler。 */
    private Handler getHandler() {
        if (handler == null) {
            // 预构建镜像启动时日志尚未完全初始化，此处必须延迟绑定
            synchronized (this) {
                if (handler == null) {
                    Class<? extends Handler> handlerClass = getHandlerClass();
                    // 获取原始 log handler；构建阶段可能尚未注册，虚拟线程场景下后续会 NPE
                    handler = Arrays.stream(InitialConfigurator.DELAYED_HANDLER.getHandlers()).filter(
                            h -> handlerClass.isAssignableFrom(h.getClass())
                    ).findFirst().orElse(null);
                }
                if (handler == null) {
                    executor.submit(() -> logger.error("Can't find handler for " + getHandlerClass()));
                }
            }
        }
        return handler;
    }

    /** 在后台线程将日志记录转发到真实 handler。 */
    public record RecordLogger(LogRecord record, KeycloakLogFilter filter) implements Runnable {
        @Override
        public void run() {
            Handler handler = filter.getHandler();
            if (handler != null) {
                handler.publish(record);
            }
        }
    }

    /** 控制台日志过滤器。 */
    @LoggingFilter(name = "keycloak-filter-console")
    private static final class KeycloakConsoleLogFilter extends KeycloakLogFilter {
        @Override
        protected Class<? extends Handler> getHandlerClass() {
            return ConsoleHandler.class;
        }

        @Override
        public boolean isHandlerEnabled() {
            return Configuration.isTrue(LoggingOptions.LOG_CONSOLE_ENABLED);
        }

        @Override
        public boolean isAsyncLoggingEnabled() {
            return Configuration.isTrue(LoggingOptions.LOG_CONSOLE_ASYNC);
        }
    }

    /** 文件日志过滤器。 */
    @LoggingFilter(name = "keycloak-filter-file")
    private static final class KeycloakFileLogFilter extends KeycloakLogFilter {
        @Override
        protected Class<? extends Handler> getHandlerClass() {
            return FileHandler.class;
        }

        @Override
        public boolean isHandlerEnabled() {
            return Configuration.isTrue(LoggingOptions.LOG_FILE_ENABLED);
        }

        @Override
        public boolean isAsyncLoggingEnabled() {
            return Configuration.isTrue(LoggingOptions.LOG_FILE_ASYNC);
        }
    }

    /** Syslog 日志过滤器。 */
    @LoggingFilter(name = "keycloak-filter-syslog")
    private static final class KeycloakSyslogLogFilter extends KeycloakLogFilter {
        protected Class<? extends Handler> getHandlerClass() {
            return SyslogHandler.class;
        }

        @Override
        public boolean isHandlerEnabled() {
            return Configuration.isTrue(LoggingOptions.LOG_SYSLOG_ENABLED);
        }

        @Override
        public boolean isAsyncLoggingEnabled() {
            return Configuration.isTrue(LoggingOptions.LOG_SYSLOG_ASYNC);
        }
    }
}
