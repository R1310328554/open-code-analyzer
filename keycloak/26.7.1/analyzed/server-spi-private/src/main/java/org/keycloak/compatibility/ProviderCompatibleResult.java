package org.keycloak.compatibility;

import java.util.Optional;

/**
 * 内部类型：表示提供者与先前元数据兼容。
 *
 * @param providerId 提供者标识
 */
record ProviderCompatibleResult(String providerId) implements CompatibilityResult {

    /** 兼容时返回 {@link ExitCode#ROLLING}。 */
    @Override
    public int exitCode() {
        return ExitCode.ROLLING.value();
    }

    /** 返回提供者兼容的成功提示。 */
    @Override
    public Optional<String> endMessage() {
        return Optional.of("[%s] Provider is compatible.".formatted(providerId));
    }
}
