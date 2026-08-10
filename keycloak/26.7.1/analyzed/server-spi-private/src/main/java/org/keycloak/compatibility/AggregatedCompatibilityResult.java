package org.keycloak.compatibility;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 聚合多条 {@link CompatibilityResult} 的兼容性检查结果。
 * <p>任一子结果为 {@link ExitCode#RECREATE} 时整体退出码为 RECREATE，否则为 ROLLING。</p>
 *
 * @param compatibilityResults 子结果集合
 */
record AggregatedCompatibilityResult(Set<CompatibilityResult> compatibilityResults) implements CompatibilityResult {

    /** 以单条结果初始化聚合容器。 */
    public AggregatedCompatibilityResult(CompatibilityResult compatibilityResult) {
        this(new HashSet<>());
        this.compatibilityResults.add(compatibilityResult);
    }

    /** 追加一条子结果并返回当前聚合实例。 */
    public AggregatedCompatibilityResult add(CompatibilityResult a) {
        compatibilityResults.add(a);
        return this;
    }

    /** 任一子结果需重建时返回 RECREATE，否则返回 ROLLING。 */
    @Override
    public int exitCode() {
        return compatibilityResults.stream()
                .anyMatch(r -> r.exitCode() == ExitCode.RECREATE.value())
                ? ExitCode.RECREATE.value() : ExitCode.ROLLING.value();
    }

    /** 汇总所有子结果的不兼容错误信息。 */
    @Override
    public Optional<String> errorMessage() {
        StringBuilder sb = new StringBuilder("Aggregated incompatible results:\n");
        for (CompatibilityResult result : compatibilityResults) {
            sb.append(result.errorMessage()).append("\n");
        }
        return Optional.of(sb.toString());
    }

    /** 合并所有 {@link ProviderIncompatibleResult} 的不兼容属性名。 */
    @Override
    public Optional<Set<String>> incompatibleAttributes() {
        return Optional.of(compatibilityResults.stream()
                .filter(r -> ProviderIncompatibleResult.class.isAssignableFrom(r.getClass()))
                .map(ProviderIncompatibleResult.class::cast)
                .flatMap(r -> r.incompatibleAttributes().orElse(Set.of()).stream())
                .collect(Collectors.toSet()));
    }
}
