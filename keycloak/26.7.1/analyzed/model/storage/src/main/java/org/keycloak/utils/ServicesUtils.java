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
 * keycloak-services 模块通用工具类，提供带超时监控的函数包装能力。
 */
public class ServicesUtils {

    private static final Logger logger = Logger.getLogger(ServicesUtils.class);

    /**
     * 包装返回 {@link Stream} 的函数，在超时时向主线程发送中断信号。
     *
     * @param session Keycloak 会话，用于获取线程池
     * @param timeout 超时阈值（毫秒）
     * @param func 待执行的原始函数
     */
    public static <T, R> Function<? super T,? extends Stream<? extends R>> timeBound(KeycloakSession session,
                                                                                     long timeout,
                                                                                     Function<T, ? extends Stream<R>> func) {
        ExecutorService executor = session.getProvider(ExecutorsProvider.class).getExecutor("storage-provider-threads");
        return p -> {
            // 在独立线程中运行超时监控：达到 timeout 后向主线程发送中断标志，以尝试中断 func 执行。
            // func 实现需响应中断标志；若不检查，执行可能超出 timeout 阈值。
            Future<?> timeCheckingThread = executor.submit(timeWarningRunnable(timeout, Thread.currentThread()));
            try {
                // 不能在非主线程运行 func：主线程持有 EntityManager 事务上下文，
                // 在其他线程操作 EntityManager 会抛出“事务不存在”错误。
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
     * 包装返回单个值的函数，在超时时向主线程发送中断信号。
     *
     * @param session Keycloak 会话，用于获取线程池
     * @param timeout 超时阈值（毫秒）
     * @param func 待执行的原始函数
     */
    public static <T, R> Function<? super T, R> timeBoundOne(KeycloakSession session,
                                                                                     long timeout,
                                                                                     Function<T, R> func) {
        ExecutorService executor = session.getProvider(ExecutorsProvider.class).getExecutor("storage-provider-threads");
        return p -> {
            // 在独立线程中运行超时监控：达到 timeout 后向主线程发送中断标志，以尝试中断 func 执行。
            // func 实现需响应中断标志；若不检查，执行可能超出 timeout 阈值。
            Future<?> warningThreadFuture = executor.submit(timeWarningRunnable(timeout, Thread.currentThread()));
            try {
                // 不能在非主线程运行 func：主线程持有 EntityManager 事务上下文，
                // 在其他线程操作 EntityManager 会抛出“事务不存在”错误。
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
     * 包装 {@link Consumer}，在超时时向主线程发送中断信号。
     *
     * @param session Keycloak 会话，用于获取线程池
     * @param timeout 超时阈值（毫秒）
     * @param func 待执行的原始 Consumer
     */
    public static <T> Consumer<? super T> consumeWithTimeBound(KeycloakSession session,
                                                             long timeout,
                                                             Consumer<T> func) {
        ExecutorService executor = session.getProvider(ExecutorsProvider.class).getExecutor("storage-provider-threads");
        return p -> {
            // 在独立线程中运行超时监控：达到 timeout 后向主线程发送中断标志，以尝试中断 func 执行。
            // func 实现需响应中断标志；若不检查，执行可能超出 timeout 阈值。
            Future<?> warningThreadFuture = executor.submit(timeWarningRunnable(timeout, Thread.currentThread()));
            try {
                // 不能在非主线程运行 func：主线程持有 EntityManager 事务上下文，
                // 在其他线程操作 EntityManager 会抛出“事务不存在”错误。
                func.accept(p);
            } finally {
                warningThreadFuture.cancel(true);

                if (Thread.interrupted()) {
                    logger.warnf("Execution with object [%s] exceeded specified time limit %d. %s", p, timeout, getShortStackTrace());
                }
            }
        };
    }

    /** 创建超时监控 Runnable：sleep 指定时长后中断主线程。 */
    private static Runnable timeWarningRunnable(long timeout, Thread mainThread) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException exception) {
                    return; // 监控线程被中断时不继续（说明主线程已按时完成）
                }

                mainThread.interrupt();
            }
        };
    }
}
