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

import picocli.CommandLine;
import picocli.CommandLine.Option;

import static org.keycloak.client.cli.util.IoUtil.printOut;

/**
 * 全局 CLI 选项命令的抽象基类。
 * <p>
 * 提供 {@code --help} 与 {@code -x} 等跨命令共享选项，并定义 Picocli 命令的标准执行模板：
 * 打印帮助、校验不支持选项、处理选项后执行具体逻辑。
 */
public abstract class BaseGlobalOptionsCmd implements Runnable {

    /** 启用后打印当前命令的帮助信息并正常退出。 */
    @Option(names = "--help",
            description = "Print command specific help")
    public void setHelp(boolean help) {
        Globals.help = help;
    }

    /** 启用后在异常退出时打印完整堆栈跟踪。 */
    @Option(names = "-x",
            description = "Print full stack trace when exiting with error")
    public void setDumpTrace(boolean dumpTrace) {
        Globals.dumpTrace = dumpTrace;
    }

    /** 若请求帮助或无有效操作，则打印帮助并以相应退出码终止进程。 */
    protected void printHelpIfNeeded() {
        if (Globals.help) {
            printOut(help());
            System.exit(CommandLine.ExitCode.OK);
        } else if (nothingToDo()) {
            printOut(help());
            System.exit(CommandLine.ExitCode.USAGE);
        }
    }

    /** 子类可覆盖：无业务操作时视为“无事可做”，将触发用法错误退出。 */
    protected boolean nothingToDo() {
        return false;
    }

    /** 返回当前命令的帮助文本。 */
    protected abstract String help();

    /** Picocli 标准入口：帮助检查 → 选项校验 → 选项处理 → 业务执行。 */
    @Override
    public void run() {
        printHelpIfNeeded();

        checkUnsupportedOptions(getUnsupportedOptions());

        processOptions();

        process();
    }

    /** 返回成对的“选项名-值”列表，用于检测子命令不支持的选项。 */
    protected String[] getUnsupportedOptions() {
        return new String[0];
    }

    /** 子类在业务执行前处理已解析的选项。 */
    protected void processOptions() {

    }

    /** 子类实现具体业务逻辑。 */
    protected void process() {

    }

    /** 校验不支持选项：成对参数中若值非 null 则抛出异常。 */
    protected void checkUnsupportedOptions(String ... options) {
        if (options.length % 2 != 0) {
            throw new IllegalArgumentException("Even number of argument required");
        }

        for (int i = 0; i < options.length; i++) {
            String name = options[i];
            String value = options[++i];

            if (value != null) {
                throw new IllegalArgumentException("Unsupported option: " + name);
            }
        }
    }

    /** 将布尔选项转为校验用的字符串：true 返回 {@code "true"}，否则返回 null。 */
    protected static String booleanOptionForCheck(boolean value) {
        return value ? "true" : null;
    }

    /** 子类可覆盖以进一步配置 {@link CommandLine} 实例。 */
    protected void configureCommandLine(CommandLine cli) {
    }

}
