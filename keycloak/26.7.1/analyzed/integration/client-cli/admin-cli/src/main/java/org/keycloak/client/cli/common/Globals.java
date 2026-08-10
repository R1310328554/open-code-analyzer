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
package org.keycloak.client.cli.common;

import java.io.PrintWriter;

import org.keycloak.client.cli.util.ClassLoaderUtil;
import org.keycloak.common.crypto.CryptoIntegration;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Keycloak 客户端 CLI 的全局启动与 Picocli 装配工具。
 * <p>
 * 负责类加载器初始化、加密集成、默认配置路径注入及 {@link CommandLine} 构建。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class Globals {

    /** 是否在异常退出时打印完整堆栈（由 {@code -x} 选项设置）。 */
    public static boolean dumpTrace = false;

    /** 是否请求打印帮助（由 {@code --help} 选项设置）。 */
    public static boolean help = false;

    /**
     * CLI 主入口：初始化类加载器与加密模块，构建命令行并执行。
     *
     * @param args 命令行参数
     * @param rootCommand 根命令对象
     * @param command 命令名称
     * @param defaultConfigFile 默认配置文件路径
     */
    public static void main(String [] args, BaseGlobalOptionsCmd rootCommand, String command, String defaultConfigFile) {
        String libDir = System.getProperty("kc.lib.dir");
        if (libDir == null) {
            throw new RuntimeException("System property kc.lib.dir needs to be set");
        }
        ClassLoader cl = ClassLoaderUtil.resolveClassLoader(libDir);
        Thread.currentThread().setContextClassLoader(cl);

        CryptoIntegration.init(cl);

        System.setProperty(BaseAuthOptionsCmd.DEFAULT_CONFIG_PATH_STRING_KEY, defaultConfigFile);
        CommandLine cli = createCommandLine(rootCommand, command, new PrintWriter(System.err, true));
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    /**
     * 创建并配置 Picocli {@link CommandLine} 实例。
     * <p>
     * 禁用 {@code @} 文件展开与 POSIX 短选项聚簇，并注册统一异常处理器。
     */
    public static CommandLine createCommandLine(BaseGlobalOptionsCmd rootCommand, String command, PrintWriter errorWriter) {
        CommandSpec spec = CommandSpec.forAnnotatedObject(rootCommand).name(command);

        CommandLine cmd = new CommandLine(spec);
        cmd.setExpandAtFiles(false);
        cmd.setPosixClusteredShortOptionsAllowed(false);
        cmd.setExecutionExceptionHandler(new ExecutionExceptionHandler());
        cmd.setParameterExceptionHandler(new ShortErrorMessageHandler());
        cmd.setErr(errorWriter);
        rootCommand.configureCommandLine(cmd);

        return cmd;
    }


}
