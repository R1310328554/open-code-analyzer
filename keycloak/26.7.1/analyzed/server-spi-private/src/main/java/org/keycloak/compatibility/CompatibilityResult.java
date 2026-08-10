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

package org.keycloak.compatibility;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * {@link CompatibilityMetadataProvider#isCompatible(Map)} 的兼容性判定结果。
 * <p>
 * 包含脚本友好的退出码（便于封装 CI 流程）及可选的错误说明。
 * </p>
 */
public interface CompatibilityResult {

    /**
     * @return 表示兼容性结论的进程退出码
     */
    int exitCode();

    /**
     * @return 可选的不兼容原因说明
     */
    default Optional<String> errorMessage() {
        return Optional.empty();
    }

    /**
     * @return 检查完成后的可选提示信息
     */
    default Optional<String> endMessage() {
        return Optional.empty();
    }

    /** @return 不兼容的属性名集合，默认空 */
    default Optional<Set<String>> incompatibleAttributes() {return Optional.empty();}

    /** 创建提供者兼容的结果（退出码 ROLLING）。 */
    static CompatibilityResult providerCompatible(String providerId) {
        return new ProviderCompatibleResult(Objects.requireNonNull(providerId));
    }

    /** 创建因属性变更导致不兼容的结果（退出码 RECREATE）。 */
    static CompatibilityResult incompatibleAttribute(String providerId, String attribute, String previousValue, String currentValue) {
        return new ProviderIncompatibleResult(Objects.requireNonNull(providerId), Objects.requireNonNull(attribute),
                previousValue, currentValue);
    }

    enum ExitCode {
        /** 可滚动更新。 */
        ROLLING(0),
        // see picocli.CommandLine.ExitCode
        // 1 -> software error
        // 2 -> usage error
        /** 需重建集群，不可滚动更新。 */
        RECREATE(3);
        // 4 -> feature 'rolling-updates' disabled

        final int exitCode;

        ExitCode(int exitCode) {
            this.exitCode = exitCode;
        }

        /** @return 对应的整数退出码 */
        public int value() {
            return exitCode;
        }
    }
}
