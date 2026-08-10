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

package org.keycloak.quarkus.runtime.cli.command;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.compatibility.CompatibilityMetadataProvider;
import org.keycloak.quarkus.runtime.cli.PropertyException;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import picocli.CommandLine;

/**
 * {@code update-compatibility metadata} 子命令：导出当前配置的兼容性元数据。
 * <p>
 * 收集各 {@link CompatibilityMetadataProvider} 的元数据，输出到控制台并可选择写入 JSON 文件，
 * 供后续 {@link UpdateCompatibilityCheck} 进行滚动升级兼容性校验。
 */
@CommandLine.Command(
        name = UpdateCompatibilityMetadata.NAME,
        description = "Stores the metadata necessary to determine if a configuration is compatible."
)
public class UpdateCompatibilityMetadata extends AbstractUpdatesCommand {

    /** 子命令名称。 */
    public static final String NAME = "metadata";
    /** 元数据输出文件路径选项。 */
    public static final String OUTPUT_OPTION_NAME = "--file";

    /** 元数据 JSON 输出文件路径（可选）。 */
    @CommandLine.Option(names = {OUTPUT_OPTION_NAME}, paramLabel = "FILE",
            description = "The file path to store the metadata. It is stored in the JSON format.")
    String outputFile;

    /** 聚合各 provider 元数据并输出到控制台与文件。 */
    @Override
    int executeAction() {
        var metadata = loadAllProviders()
                .values()
                .stream()
                .map(Entry::new)
                .filter(Entry::hasMetadata)
                .collect(Collectors.toMap(Entry::id, Entry::metadata));
        printToConsole(metadata);
        writeToFile(metadata);
        return 0;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void validateConfig() {
        super.validateConfig();
        validateFileParameter();
    }

    /** 校验输出路径的父目录可创建，且目标不是目录。 */
    private void validateFileParameter() {
        if (noOutputFileSet()) {
            return;
        }
        var file = new File(outputFile);
        if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new PropertyException("Incorrect argument %s. Unable to create parent directory: %s".formatted(OUTPUT_OPTION_NAME, file.getParentFile().getAbsolutePath()));
        }
        validateFileIsNotDirectory(file, OUTPUT_OPTION_NAME);
    }

    /** 将元数据以格式化 JSON 打印到标准输出。 */
    private void printToConsole(Map<String, Map<String, String>> metadata) {
        try {
            var json = JsonSerialization.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata);
            picocli.getOutWriter().println("Metadata:%n%s".formatted(json));
        } catch (JsonProcessingException e) {
            throw new PropertyException("Unable to create JSON representation of the metadata", e);
        }
    }

    /** 若指定了输出文件，将元数据写入 JSON 文件。 */
    private void writeToFile(Map<String, Map<String, String>> metadata) {
        if (noOutputFileSet()) {
            return;
        }
        var file = new File(outputFile);
        try {
            JsonSerialization.mapper.writeValue(file, metadata);
        } catch (IOException e) {
            throw new PropertyException("Unable to write file '%s'".formatted(file.getAbsolutePath()), e);
        }
    }

    /** 是否未设置输出文件路径。 */
    private boolean noOutputFileSet() {
        return outputFile == null || outputFile.isBlank();
    }

    /** provider 元数据条目：id 与键值对映射。 */
    private record Entry(String id, Map<String, String> metadata) {

        Entry(CompatibilityMetadataProvider provider) {
            this(provider.getId(), provider.metadata());
        }

        /** 元数据非空时参与导出。 */
        boolean hasMetadata() {
            return !metadata().isEmpty();
        }
    }
}
