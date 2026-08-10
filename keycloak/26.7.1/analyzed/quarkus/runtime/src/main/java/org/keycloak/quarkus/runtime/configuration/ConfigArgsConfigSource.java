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

package org.keycloak.quarkus.runtime.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.quarkus.runtime.cli.command.Main;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;

import io.smallrye.config.PropertiesConfigSource;

import static org.keycloak.quarkus.runtime.cli.Picocli.ARG_PREFIX;
import static org.keycloak.quarkus.runtime.cli.Picocli.ARG_SHORT_PREFIX;

/**
 * CLI 参数配置源：将命令行参数映射为对应的配置属性，供构建与运行阶段识别。
 * <p>
 * 每个参数通过 {@link MicroProfileConfigProvider#NS_KEYCLOAK} 命名空间前缀转换为标准配置键。
 */
public class ConfigArgsConfigSource extends PropertiesConfigSource {

    /** SPI 相关 CLI 选项前缀。 */
    public static final String SPI_OPTION_PREFIX = "--spi";

    /** 接受带值参数的短选项集合（如 Profile、配置文件路径）。 */
    public static final Set<String> SHORT_OPTIONS_ACCEPTING_VALUE = Set.of(Main.PROFILE_SHORT_NAME, Main.CONFIG_FILE_SHORT_NAME);

    /** 保存原始 CLI 参数的系统属性键。 */
    private static final String CLI_ARGS = "kc.config.args";
    /** 本配置源在 MicroProfile 中的名称。 */
    public static final String NAME = "CliConfigSource";
    /** 键值对 CLI 参数的分隔正则（{@code =}）。 */
    private static final Pattern ARG_KEY_VALUE_SPLIT = Pattern.compile("=");

    protected ConfigArgsConfigSource() {
        super(parseArguments(), NAME, 600);
    }

    /**
     * 设置已净化的 CLI 参数（格式为 {@code --property=value}）。
     * <p>
     * 参数序列化写入系统属性，供后续解析与内部命令转发时引用原始调用。
     *
     * @param args 命令行参数字符串数组
     */
    public static void setCliArgs(String... args) {
        System.setProperty(CLI_ARGS,
                Stream.of(args).map(arg -> arg.replaceAll(",", ",,")).collect(Collectors.joining(", ")));
    }

    /**
     * 读取先前保存的原始 CLI 参数列表。
     * <p>
     * 在内部触发其他命令执行时，可通过系统属性获取用户实际调用的命令行。
     *
     * @return 实际调用的 CLI 参数列表；未设置时返回空列表
     */
    public static List<String> getAllCliArgs() {
        String args = System.getProperty(CLI_ARGS);
        if(args == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        boolean escaped = false;
        StringBuilder arg = new StringBuilder();
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            if (c == ',') {
                if (escaped) {
                    arg.append(c);
                }
                escaped = !escaped;
            } else if (c == ' ') {
                if (escaped) {
                    result.add(arg.toString());
                    arg.setLength(0);
                    escaped = false;
                } else {
                    arg.append(c);
                }
            } else {
                arg.append(c);
            }
        }
        result.add(arg.toString());

        return result;
    }

    /** 解析 CLI 参数为 Keycloak 配置键值映射。 */
    private static Map<String, String> parseArguments() {
        final Map<String, String> properties = new HashMap<>();

        parseConfigArgs(getAllCliArgs(), (key, value) -> {
            PropertyMappers.getKcKeyFromCliKey(key).ifPresent(s -> properties.put(s, value));
        }, ignored -> {});

        return properties;
    }

    /**
     * 通用 CLI 参数解析：区分键值对与一元参数。
     *
     * @param args 待解析参数列表
     * @param valueArgConsumer 键值对参数消费者
     * @param unaryConsumer 无值参数消费者
     */
    public static void parseConfigArgs(List<String> args, BiConsumer<String, String> valueArgConsumer, Consumer<String> unaryConsumer) {
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);

            if (!arg.startsWith(ARG_SHORT_PREFIX)) {
                unaryConsumer.accept(arg);
                continue;
            }

            String[] keyValue = ARG_KEY_VALUE_SPLIT.split(arg, 2);
            String key = keyValue[0];

            String value;

            if (keyValue.length == 1) {
                if (args.size() <= i + 1) {
                    unaryConsumer.accept(arg);
                    continue;
                }
                if (arg.startsWith(ARG_PREFIX)) {
                    i++; // 长选项：下一 token 作为值消费
                    value = args.get(i);
                } else {
                    unaryConsumer.accept(arg);
                    continue;
                }
            } else {
                // 内联值形式，例如 --key=value
                value = keyValue[1];
            }

            valueArgConsumer.accept(key, value);
        }
    }
}
