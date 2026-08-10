/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.common.Profile;
import org.keycloak.common.Version;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.quarkus.runtime.cli.ExecutionExceptionHandler;
import org.keycloak.quarkus.runtime.cli.Picocli;
import org.keycloak.quarkus.runtime.cli.command.AbstractNonServerCommand;
import org.keycloak.quarkus.runtime.cli.command.DryRunMixin;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.PersistedConfigSource;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.keycloak.quarkus.runtime.integration.jaxrs.QuarkusKeycloakApplication;

import io.quarkus.arc.Arc;
import io.quarkus.bootstrap.runner.RunnerClassLoader;
import io.quarkus.runtime.ApplicationLifecycleManager;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;
import picocli.CommandLine;

import static org.keycloak.common.util.Environment.isNonServerMode;
import static org.keycloak.quarkus.runtime.Environment.getKeycloakModeFromProfile;
import static org.keycloak.quarkus.runtime.Environment.hasEarlyExitLaunchMode;

/**
 * Keycloak Quarkus 主入口：负责初始化 CLI、解析参数并启动 Quarkus 服务器。
 * <p>
 * 实现 {@link QuarkusApplication}，由 {@code @QuarkusMain} 注册为 {@code keycloak} 应用。
 */
@QuarkusMain(name = "keycloak")
@ApplicationScoped
public class KeycloakMain implements QuarkusApplication {

    /** 系统属性：服务器就绪后是否打印运行提示。 */
    public static final String KC_SERVER_PRINT_RUNNING = "kc.server.print_running";
    /** 服务器运行中的控制台消息。 */
    public static final String RUNNING_MESSAGE = "The server is running";
    /** 当前执行的非服务器 CLI 命令（静态，启动期间有效）。 */
    private static AbstractNonServerCommand COMMAND;
    /** 异步退出时的错误处理器。 */
    private static Consumer<Throwable> ERROR_HANDLER;

    static {
        InfinispanUtils.configureVirtualThreads();
    }

    /**
     * JVM 入口：配置虚拟线程与 ForkJoinPool，委托 Picocli 解析并执行命令。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        ensureForkJoinPoolThreadFactoryHasBeenSetToQuarkus();
        InfinispanUtils.ensureVirtualThreadsParallelism();

        Picocli picocli;
        Properties clonedProps = null;
        if (!(Thread.currentThread().getContextClassLoader() instanceof RunnerClassLoader)) {
            clonedProps = (Properties) System.getProperties().clone();
            picocli = new Picocli() { // 非脚本启动路径，避免 System.exit
                @Override
                public void exit(int exitCode) {
                    Quarkus.asyncExit(exitCode);
                };
            };
        } else {
            picocli = new Picocli();
        }

        System.setProperty("kc.version", Version.VERSION);

        try {
            main(args, picocli);
        } finally {
            if (clonedProps != null) {
                reset(clonedProps);
            }
        }
    }

    /**
     * 重置系统属性与全局配置单例（测试或非脚本场景）。
     *
     * @param systemProperties 要恢复的系统属性快照
     */
    public static void reset(Properties systemProperties) {
        System.setProperties((Properties) systemProperties.clone());
        PropertyMappers.reset();
        PersistedConfigSource.getInstance().getConfigValueProperties().clear();
        Profile.reset();
        Configuration.resetConfig();
        ExecutionExceptionHandler.resetExceptionTransformers();
    }

    /**
     * 使用给定 Picocli 实例解析参数；无参数时默认显示帮助。
     *
     * @param args 命令行参数
     * @param picocli CLI 门面
     */
    public static void main(String[] args, Picocli picocli) {
        List<String> cliArgs = List.of(args.length == 0 ? new String[] {"-h"} : args);

        if (DryRunMixin.isDryRunBuild() && (cliArgs.contains(DryRunMixin.DRYRUN_OPTION_LONG) || Boolean.valueOf(System.getenv().get(DryRunMixin.KC_DRY_RUN_ENV)))) {
            PersistedConfigSource.getInstance().useDryRunProperties();
        }

        // 解析参数并执行已配置的子命令
        picocli.parseAndRun(cliArgs);
    }

    /**
     * 验证 Quarkus 设置的 ForkJoinPool 工厂与系统属性一致。
     * 若 Java Agent 或 JMX 在 main 之前初始化公共池，SmallRye 配置加载可能不可靠。
     */
    private static void ensureForkJoinPoolThreadFactoryHasBeenSetToQuarkus() {
        // 此时 CLI 设置已被 QuarkusEntryPoint 覆盖，只能比对工厂类名与配置属性
        final String FORK_JOIN_POOL_COMMON_THREAD_FACTORY = "java.util.concurrent.ForkJoinPool.common.threadFactory";
        String sf = System.getProperty(FORK_JOIN_POOL_COMMON_THREAD_FACTORY);
        //noinspection resource
        if (!ForkJoinPool.commonPool().getFactory().getClass().getName().equals(sf)) {
            Logger.getLogger(KeycloakMain.class).errorf("The ForkJoinPool has been initialized with the wrong thread factory. The property '%s' should be set on the Java CLI to ensure Java's ForkJoinPool will always be initialized with '%s' even if there are Java agents which might initialize logging or other capabilities earlier than the main method.",
                    FORK_JOIN_POOL_COMMON_THREAD_FACTORY,
                    sf);
            throw new RuntimeException("The ForkJoinPool has been initialized with the wrong thread factory");
        }
    }

    /**
     * 启动 Quarkus 运行时（由 {@link Picocli#start()} 调用）。
     *
     * @param picocli CLI 门面
     * @param command 非服务器命令，可为 null
     * @param errorHandler 启动失败时的错误处理器
     */
    public static void start(Picocli picocli, AbstractNonServerCommand command, ExecutionExceptionHandler errorHandler) {
        COMMAND = command; // 理想情况下应通过实例而非静态字段传递
        ERROR_HANDLER = cause -> errorHandler.error(picocli.getErrWriter(),
                String.format("Failed to start server in (%s) mode", getKeycloakModeFromProfile(org.keycloak.common.util.Environment.getProfile())),
                cause.getCause());
        try {
            Quarkus.run(KeycloakMain.class, (exitCode, cause) -> {
                if (cause != null) {
                    errorHandler.error(picocli.getErrWriter(),
                            String.format("Failed to start server in (%s) mode", getKeycloakModeFromProfile(org.keycloak.common.util.Environment.getProfile())),
                            cause.getCause());
                }
                picocli.exit(exitCode);
            });
        } catch (Throwable cause) {
            errorHandler.error(picocli.getErrWriter(),
                    String.format("Unexpected error when starting the server in (%s) mode", getKeycloakModeFromProfile(org.keycloak.common.util.Environment.getProfile())),
                    cause.getCause());
        } finally {
            ERROR_HANDLER = null;
            COMMAND = null;
        }
        picocli.exit(CommandLine.ExitCode.SOFTWARE);
    }

    /**
     * Quarkus 应用就绪后回调：通知非服务器命令、处理早退模式或等待退出。
     * 应在服务器完全初始化后调用。
     *
     * @param args Quarkus 传入的参数（通常未使用）
     * @return 进程退出码
     */
    @Override
    public int run(String... args) throws Exception {
        QuarkusKeycloakApplication application = Arc.container().instance(QuarkusKeycloakApplication.class).get();
        if (COMMAND != null) {
            QuarkusKeycloakSessionFactory sessionFactory = Arc.container().instance(QuarkusKeycloakSessionFactory.class).get();
            COMMAND.onStart(application, sessionFactory);
        }
        if (hasEarlyExitLaunchMode() || isNonServerMode()) {
            // 测试模式下立即退出（后续可按测试需求区分短/长生命周期）
            Quarkus.asyncExit(ApplicationLifecycleManager.getExitCode());
        } else {
            if (Boolean.getBoolean(KC_SERVER_PRINT_RUNNING)) {
                BiConsumer<Void, Throwable> started = (v, t) -> {
                    if (t == null) {
                        System.out.println("\n" + RUNNING_MESSAGE);
                    }
                };
                application.getBootstrapFuture().ifPresentOrElse(future -> future.whenComplete(started),
                        () -> started.accept(null, null));
            }
            Quarkus.waitForExit();
        }

        return ApplicationLifecycleManager.getExitCode();
    }

    /**
     * 异步退出并可选触发错误处理器。
     *
     * @param exitCode 退出码
     * @param t 关联异常
     */
    public static void asyncExit(int exitCode, Throwable t) {
        Optional.ofNullable(ERROR_HANDLER).ifPresent(h -> h.accept(t));
        Quarkus.asyncExit(exitCode);
    }

}
