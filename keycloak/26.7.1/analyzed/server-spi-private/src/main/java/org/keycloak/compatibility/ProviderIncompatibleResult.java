package org.keycloak.compatibility;

import java.util.Optional;
import java.util.Set;

/**
 * 内部类型：表示提供者与先前元数据不兼容。
 * <p>
 * 记录提供者 ID 及发生变更的属性前后值。
 * </p>
 *
 * @param providerId 提供者标识
 * @param attribute 不兼容的属性名
 * @param previousValue 先前部署中的值
 * @param currentValue 当前部署中的值
 */
record ProviderIncompatibleResult(String providerId, String attribute, String previousValue,
                                  String currentValue) implements CompatibilityResult {
    /** 不兼容时返回 {@link ExitCode#RECREATE}。 */
    @Override
    public int exitCode() {
        return ExitCode.RECREATE.value();
    }

    /** 描述属性从先前值到当前值的变更详情。 */
    @Override
    public Optional<String> errorMessage() {
        return Optional.of("[%s] Rolling Update is not available. '%s.%s' is incompatible: %s -> %s.".formatted(providerId, providerId, attribute, previousValue, currentValue));
    }

    /** 返回包含单个不兼容属性名的集合。 */
    @Override
    public Optional<Set<String>> incompatibleAttributes() {
        return Optional.of(Set.of(attribute));
    }
}
