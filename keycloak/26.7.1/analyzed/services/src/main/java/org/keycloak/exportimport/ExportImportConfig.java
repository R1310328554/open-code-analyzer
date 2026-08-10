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

package org.keycloak.exportimport;

import java.io.Closeable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 导出/导入迁移配置：通过 {@code keycloak.migration.*} 系统属性读写 CLI 与启动参数。
 * <p>涵盖动作类型、提供者 ID、目录/文件路径、用户导出策略与导入冲突策略等。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ExportImportConfig {

    /** 迁移相关系统属性前缀。 */
    public static final String PREFIX = "keycloak.migration.";
    public static final String ACTION = PREFIX + "action";
    public static final String ACTION_EXPORT = "export";
    public static final String ACTION_IMPORT = "import";

    public static final String SINGLE_TRANSACTION = PREFIX + "single-transaction";

    public static final String PROVIDER = PREFIX + "provider";
    public static final String PROVIDER_DEFAULT = "dir";

    // 待导出领域名称；为 null 时触发全量导出
    public static final String REALM_NAME = PREFIX + "realmName";

    // 供 {@code dir} 提供者使用的导出/导入目录
    public static final String DIR = PREFIX + "dir";

    // 供 {@code singleFile} 提供者使用的单文件路径
    public static final String FILE = PREFIX + "file";

    // 导入时是否替换占位符
    public static final String REPLACE_PLACEHOLDERS = PREFIX + "replace-placeholders";

    // {@code dir} 提供者导出领域时用户的文件布局策略
    public static final String USERS_EXPORT_STRATEGY = PREFIX + "usersExportStrategy";
    public static final UsersExportStrategy DEFAULT_USERS_EXPORT_STRATEGY = UsersExportStrategy.DIFFERENT_FILES;

    // {@code dir} 提供者分文件导出时每文件用户数（{@link UsersExportStrategy#DIFFERENT_FILES} 时生效）
    public static final String USERS_PER_FILE = PREFIX + "usersPerFile";
    public static final Integer DEFAULT_USERS_PER_FILE = 50;

    // 导入遇到已存在实体时的冲突处理策略
    public static final String STRATEGY = PREFIX + "strategy";
    public static final Strategy DEFAULT_STRATEGY = Strategy.OVERWRITE_EXISTING;

    /** @return 当前迁移动作（{@code export} 或 {@code import}），未设置时为 null */
    public static String getAction() {
        return System.getProperty(ACTION);
    }

    /** @return 导入策略系统属性值 */
    public static String getStrategy() {
        return System.getProperty(STRATEGY);
    }

    /** 设置导入冲突策略并返回旧值。 */
    public static String setStrategy(Strategy strategy) {
        return System.setProperty(STRATEGY, strategy.toString());
    }

    /** @return 导出/导入目录路径（可选） */
    public static Optional<String> getDir() {
        return Optional.ofNullable(System.getProperty(DIR));
    }

    /** 临时设置迁移动作；关闭返回的 {@link Closeable} 时清除该属性。 */
    public static Closeable setAction(String exportImportAction) {
        System.setProperty(ACTION, exportImportAction);
        return () -> System.getProperties().remove(ACTION);
    }

    /** 设置导出/导入提供者 ID（如 {@code dir}、{@code singleFile}）。 */
    public static void setProvider(String exportImportProvider) {
        System.setProperty(PROVIDER, exportImportProvider);
    }

    /** 设置待导出领域名称；传入 null 时移除该属性。 */
    public static void setRealmName(String realmName) {
        if (realmName != null) {
            System.setProperty(REALM_NAME, realmName);
        } else {
            System.getProperties().remove(REALM_NAME);
        }
    }

    /** 设置导出/导入目录路径。 */
    public static void setDir(String dir) {
        System.setProperty(DIR, dir);
    }

    /** 设置单文件导出/导入路径。 */
    public static void setFile(String file) {
        System.setProperty(FILE, file);
    }

    /** @return 导入时是否替换占位符 */
    public static boolean isReplacePlaceholders() {
        return Boolean.getBoolean(REPLACE_PLACEHOLDERS);
    }

    /** 设置导入时是否替换占位符。 */
    public static void setReplacePlaceholders(boolean replacePlaceholders) {
        System.setProperty(REPLACE_PLACEHOLDERS, String.valueOf(replacePlaceholders));
    }

    /** 清除 FILE、DIR、ACTION、STRATEGY、REPLACE_PLACEHOLDERS 等迁移属性。 */
    public static void reset() {
        Stream.of(FILE, DIR, ACTION, STRATEGY, REPLACE_PLACEHOLDERS)
                .forEach(prop -> System.getProperties().remove(prop));
    }

    /** 设置导入是否在单事务中执行。 */
    public static void setSingleTransaction(boolean b) {
        System.setProperty(SINGLE_TRANSACTION, String.valueOf(b));
    }

    /** @return 导入是否使用单事务，默认 true */
    public static boolean isSingleTransaction() {
        return Optional.ofNullable(System.getProperty(SINGLE_TRANSACTION)).map(Boolean::valueOf).orElse(Boolean.TRUE);
    }

}
