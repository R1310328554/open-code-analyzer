/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.keycloak.executors.ExecutorsProvider;
import org.keycloak.models.KeycloakSession;

import org.jboss.logging.Logger;

import static org.keycloak.common.util.StackUtil.getShortStackTrace;

/**
 * keycloak-services 通用辅助工具类。
 * <p>提供带超时警告的函数包装，用于存储提供者等耗时操作监控。</p>
 * @deprecated 待 legacy datastore 模块移除后删除
 */
public class ServicesUtils {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(ServicesUtils.class);

    /**
     * 包装返回 Stream 的函数，超时后向主线程发送中断并记录警告。
     * <p>func 须在主线程执行（EntityManager 事务上下文绑定）。</p>
     *
     * @param session Keycloak 会话
     * @param timeout 超时阈值（毫秒）
     * @param func 待包装函数
     * @return 带超时监控的函数
     */
    public static <T, R> Function<? super T,? extends Stream<? extends R>> timeBound(KeycloakSession session,
                                                                                     long timeout,
                                                                                     Function<T, ? extends Stream<R>> func) {
        ExecutorService executor = session.getProvider(ExecutorsProvider.class).getExecutor("storage-provider-threads");
        return p -> {
            // 辅助线程在超时后向主线程设置中断标志；func 需响应中断才能提前终止
            Future<?> timeCheckingThread = executor.submit(timeWarningRunnable(timeout, Thread.currentThread()));
            try {
                // func 必须在主线程运行，否则 EntityManager 事务上下文不可用
                return func.apply(p);
            } finally {
                timeCheckingThread.cancel(true);

                if (Thread.interrupted()) {
                    logger.warnf("Execution with object [%s] exceeded specified time limit %d. %s", p, timeout, getShortStackTrace());
                }
            }
        };
    }

    /**
     * 包装返回单值的函数，超时后向主线程发送中断并记录警告。
     *
     * @param session Keycloak 会话
     * @param timeout 超时阈值（毫秒）
     * @param func 待包装函数
     * @return 带超时监控的函数
     */
    public static <T, R> Function<? super T, R> timeBoundOne(KeycloakSession session,
                                                                                     long timeout,
                                                                                     Function<T, R> func) {
        ExecutorService executor = session.getProvider(ExecutorsProvider.class).getExecutor("storage-provider-threads");
        return p -> {
            // We are running another thread here, which serves as a time checking thread. When timeout is hit, the time
            // checking thread will send interrupted flag to main thread, which can cause interruption of func execution.
            // To support interruption func implementation should react to interrupt flag.
            // If func doesn't check the interrupted flag, the execution won't be interrupted and can take more time
            // than the threshold given by timeout variable
            Future<?> warningThreadFuture = executor.submit(timeWarningRunnable(timeout, Thread.currentThread()));
            try {
                // We cannot run func in different than main thread, because main thread have, for example, EntityManager
                // transaction context. If we run any operation on EntityManager in a different thread, it will fail
                // with a transaction doesn't exist error
                return func.apply(p);
            } finally {
                warningThreadFuture.cancel(true);

                if (Thread.interrupted()) {
                    logger.warnf("Execution with object [%s] exceeded specified time limit %d. %s", p, timeout, getShortStackTrace());
                }
            }
        };
    }

    /**
     * 包装 {@link Consumer}，超时后向主线程发送中断并记录警告。
     *
     * @param session Keycloak 会话
     * @param timeout 超时阈值（毫秒）
     * @param func 待包装 Consumer
     * @return 带超时监控的 Consumer
     */
    public static <T> Consumer<? super T> consumeWithTimeBound(KeycloakSession session,
                                                             long timeout,
                                                             Consumer<T> func) {
        ExecutorService executor = session.getProvider(ExecutorsProvider.class).getExecutor("storage-provider-threads");
        return p -> {
            // We are running another thread here, which serves as a time checking thread. When timeout is hit, the time
            // checking thread will send interrupted flag to main thread, which can cause interruption of func execution.
            // To support interruption func implementation should react to interrupt flag.
            // If func doesn't check the interrupted flag, the execution won't be interrupted and can take more time
            // than the threshold given by timeout variable
            Future<?> warningThreadFuture = executor.submit(timeWarningRunnable(timeout, Thread.currentThread()));
            try {
                // We cannot run func in different than main thread, because main thread have, for example, EntityManager
                // transaction context. If we run any operation on EntityManager in a different thread, it will fail
                // with a transaction doesn't exist error
                func.accept(p);
            } finally {
                warningThreadFuture.cancel(true);

                if (Thread.interrupted()) {
                    logger.warnf("Execution with object [%s] exceeded specified time limit %d. %s", p, timeout, getShortStackTrace());
                }
            }
        };
    }

    /** 创建超时警告 Runnable：sleep 后向主线程发送 interrupt。 */
    private static Runnable timeWarningRunnable(long timeout, Thread mainThread) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException exception) {
                    return; // 主线程已按时完成，不发送中断
                }

                mainThread.interrupt();
            }
        };
    }
}
