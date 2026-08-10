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

import org.keycloak.compatibility.CompatibilityResult;
import org.keycloak.compatibility.Util;
import org.keycloak.quarkus.runtime.cli.PropertyException;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;
import picocli.CommandLine;

/**
 * {@code update-compatibility check} 子命令：校验外部元数据与当前配置是否兼容。
 * <p>
 * 退出码为 0 表示可在旧元数据与当前配置之间执行滚动升级；否则需重建或调整配置。
 */
@CommandLine.Command(
        name = UpdateCompatibilityCheck.NAME,
        description = "Checks if the metadata is compatible with the current configuration. A zero exit code means a rolling update is possible between old and the current metadata."
)
public class UpdateCompatibilityCheck extends AbstractUpdatesCommand {

    /** 子命令名称。 */
    public static final String NAME = "check";
    /** 输入元数据文件路径选项。 */
    public static final String INPUT_OPTION_NAME = "--file";
    /** 元数据 JSON 反序列化类型：providerId → 属性键值对。 */
    public static final TypeReference<Map<String, Map<String, String>>> METADATA_TYPE_REF = new TypeReference<>() {
    };


    /** 待校验的元数据文件路径。 */
    @CommandLine.Option(names = {INPUT_OPTION_NAME}, paramLabel = "FILE",
            description = "The file path to read the metadata.")
    String inputFile;

    /** 逐 provider 比对元数据与当前配置，返回兼容性退出码。 */
    @Override
    int executeAction() {
        var info = readServerInfo();
        var providers = loadAllProviders();
        var idIterator = Util.mergeKeySet(info, providers)
                .sorted()
                .iterator();

        while (idIterator.hasNext()) {
            var id = idIterator.next();
            var provider = providers.get(id);
            if (provider == null) {
                picocli.error("[%s] Provider not found. Rolling Update is not available.".formatted(id));
                return CompatibilityResult.ExitCode.RECREATE.value();
            }

            var result = provider.isCompatible(Map.copyOf(info.getOrDefault(id, Map.of())));
            result.endMessage().ifPresent(picocli.getOutWriter()::println);

            if (Util.isNotCompatible(result)) {
                result.errorMessage().ifPresent(picocli::error);
                return result.exitCode();
            }
        }

        picocli.getOutWriter().println("[OK] Rolling Update is available.");
        return CompatibilityResult.ExitCode.ROLLING.value();
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

    /** 校验 {@code --file} 参数已提供且指向可读文件。 */
    private void validateFileParameter() {
        if (inputFile == null || inputFile.isBlank()) {
            throw new PropertyException("Missing required argument: " + INPUT_OPTION_NAME);
        }
        var file = new File(inputFile);
        if (!file.exists()) {
            throw new PropertyException("Incorrect argument %s. Path '%s' not found".formatted(INPUT_OPTION_NAME, file.getAbsolutePath()));
        }
        validateFileIsNotDirectory(file, INPUT_OPTION_NAME);
    }

    /** 从 JSON 文件读取服务器兼容性元数据。 */
    private Map<String, Map<String, String>> readServerInfo() {
        var file = new File(inputFile);
        try {
            return JsonSerialization.mapper.readValue(file, METADATA_TYPE_REF);
        } catch (IOException e) {
            throw new PropertyException("Unable to read file '%s'".formatted(file.getAbsolutePath()), e);
        }
    }
}
