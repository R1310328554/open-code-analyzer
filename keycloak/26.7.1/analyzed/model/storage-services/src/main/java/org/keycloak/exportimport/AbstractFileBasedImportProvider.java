/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.exportimport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.keycloak.common.util.StringPropertyReplacer;

import static org.keycloak.common.util.StringPropertyReplacer.replaceProperties;

/**
 * 基于文件的导入 Provider 抽象基类：解析导入文件并在启用占位符替换时展开环境变量。
 */
public abstract class AbstractFileBasedImportProvider implements ImportProvider {

    /** 使用 {@link System#getenv} 解析 {@code ${VAR}} 形式占位符的属性解析器。 */
    private static final StringPropertyReplacer.PropertyResolver ENV_VAR_PROPERTY_RESOLVER = new StringPropertyReplacer.PropertyResolver() {
        @Override
        public String resolve(String property) {
            return Optional.ofNullable(property).map(System::getenv).orElse(null);
        }
    };

    /**
     * 打开导入文件：若 {@link ExportImportConfig#isReplacePlaceholders()} 为 true，则替换环境变量占位符。
     *
     * @param importFile 待导入文件
     * @return 文件输入流
     */
    protected InputStream parseFile(File importFile) throws IOException {
        if (ExportImportConfig.isReplacePlaceholders()) {
            return replaceProperties(new BufferedInputStream(new FileInputStream(importFile)), ENV_VAR_PROPERTY_RESOLVER);
        }

        return new FileInputStream(importFile);
    }

}
