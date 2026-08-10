/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.keycloak.config.ExportOptions;
import org.keycloak.config.Option;
import org.keycloak.config.OptionBuilder;
import org.keycloak.config.OptionCategory;
import org.keycloak.exportimport.UsersExportStrategy;
import org.keycloak.quarkus.runtime.cli.Picocli;
import org.keycloak.quarkus.runtime.cli.PropertyException;
import org.keycloak.quarkus.runtime.cli.command.Export;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;

import static org.keycloak.exportimport.ExportImportConfig.PROVIDER;
import static org.keycloak.quarkus.runtime.configuration.Configuration.getOptionalValue;
import static org.keycloak.quarkus.runtime.configuration.Configuration.isBlank;
import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * Realm 导出（{@code kc.sh export}）相关 {@link PropertyMapper} 分组：
 * 根据 {@code --file} 或 {@code --dir} 推断导出 SPI 提供方（单文件/目录）。
 */
public final class ExportPropertyMappers implements PropertyMapperGrouping {
    /** 导出 SPI 根属性：决定使用 singleFile 还是 dir 提供方。 */
    private static final String EXPORTER_PROPERTY = "kc.spi-export--exporter";
    /** 单文件导出提供方标识。 */
    private static final String SINGLE_FILE = "singleFile";
    /** 目录导出提供方标识。 */
    private static final String DIR = "dir";

    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(EXPORTER_PLACEHOLDER)
                        .to(EXPORTER_PROPERTY)
                        .transformer(ExportPropertyMappers::transformExporter)
                        .paramLabel("file")
                        .build(),
                fromOption(ExportOptions.FILE)
                        .to("kc.spi-export--single-file--file")
                        .paramLabel("file")
                        .isEnabled(c -> c instanceof Export)
                        .build(),
                fromOption(ExportOptions.DIR)
                        .to("kc.spi-export--dir--dir")
                        .paramLabel("dir")
                        .isEnabled(c -> c instanceof Export)
                        .build(),
                fromOption(ExportOptions.REALM)
                        .to("kc.spi-export--single-file--realm-name")
                        .isEnabled(ExportPropertyMappers::isSingleFileProvider)
                        .paramLabel("realm")
                        .build(),
                fromOption(ExportOptions.REALM)
                        .to("kc.spi-export--dir--realm-name")
                        .isEnabled(ExportPropertyMappers::isDirProvider)
                        .paramLabel("realm")
                        .build(),
                fromOption(ExportOptions.USERS)
                        .to("kc.spi-export--dir--users-export-strategy")
                        .addValidator(ExportPropertyMappers::validateUsersUsage)
                        .paramLabel("strategy")
                        .build(),
                fromOption(ExportOptions.USERS_PER_FILE)
                        .to("kc.spi-export--dir--users-per-file")
                        .isEnabled(ExportPropertyMappers::isDirProvider)
                        .paramLabel("number")
                        .build()
        );
    }

    /** 校验 {@code --users} 策略：导出到文件时仅允许 {@code same_file}。 */
    private static void validateUsersUsage(PropertyMapper<?> mapper, ConfigValue value) {
        if (!isBlank(ExportOptions.FILE) && isBlank(ExportOptions.DIR)) {
            var sameFileIsSpecified = UsersExportStrategy.SAME_FILE.toString().toLowerCase().equals(value.getValue());

            if (!sameFileIsSpecified) {
                throw new PropertyException("Property '--users' can be used only when exporting to a directory, or value set to 'same_file' when exporting to a file.");
            }
        }
    }

    @Override
    public void validateConfig(Picocli picocli) {
        if (picocli.getParsedCommand().orElse(null) instanceof Export && getOptionalValue(EXPORTER_PROPERTY).isEmpty() && System.getProperty(PROVIDER) == null) {
            throw new PropertyException("Must specify either --dir or --file options.");
        }
    }

    /** 合成选项：根据 file/dir 推断导出模式。 */
    private static final Option<String> EXPORTER_PLACEHOLDER = new OptionBuilder<>("exporter", String.class)
            .category(OptionCategory.EXPORT)
            .description("Placeholder for determining export mode")
            .buildTime(false)
            .synthetic()
            .build();

    /** 当前导出提供方是否为单文件模式。 */
    private static boolean isSingleFileProvider() {
        return isProvider(SINGLE_FILE);
    }

    /** 当前导出提供方是否为目录模式。 */
    private static boolean isDirProvider() {
        return !isSingleFileProvider();
    }

    private static boolean isProvider(String provider) {
        return Configuration.getOptionalValue(EXPORTER_PROPERTY)
                .filter(provider::equals)
                .isPresent();
    }

    /**
     * 根据已配置的 file/dir 或 SPI 属性推断导出提供方名称。
     *
     * @param option 占位选项值（未使用）
     * @param context 配置上下文
     * @return {@code singleFile}、{@code dir} 或 null（未指定或冲突）
     */
    private static String transformExporter(String option, ConfigSourceInterceptorContext context) {
        ConfigValue exporter = context.proceed(EXPORTER_PROPERTY);
        if (exporter != null) {
            return exporter.getValue();
        }

        var file = Configuration.getOptionalValue("kc.spi-export--single-file--file").map(f -> SINGLE_FILE);
        var dir = Configuration.getOptionalValue("kc.spi-export--dir--dir")
                .or(() -> Configuration.getOptionalValue("kc.dir"))
                .map(f -> DIR);

        // 仅允许指定 file 或 dir 之一（异或）
        boolean xor = file.isPresent() ^ dir.isPresent();

        return xor ? file.or(() -> dir).get() : null;
    }

}
