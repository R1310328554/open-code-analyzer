package org.keycloak.compatibility;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 提供者配置兼容性检查工具类。
 * <p>合并新旧配置键集合并逐项比较属性值，汇总不兼容项为 {@link CompatibilityResult}。</p>
 */
public final class Util {

    private Util() {
    }

    /** 合并两个 Map 的键集合并去重。 */
    public static <T> Stream<T> mergeKeySet(Map<T, ?> map1, Map<T, ?> map2) {
        return Stream.concat(
                map1.keySet().stream(),
                map2.keySet().stream()
        ).distinct();
    }

    /**
     * 比较提供者新旧配置是否滚动升级兼容。
     *
     * @param provider 提供者标识
     * @param old 旧版配置
     * @param current 当前配置
     * @return 兼容结果，属性不一致时聚合为 {@link AggregatedCompatibilityResult}
     */
    public static CompatibilityResult isCompatible(String provider, Map<String, String> old, Map<String, String> current) {
        return mergeKeySet(old, current)
                .sorted()
                .map(key -> compare(provider, key, old.get(key), current.get(key)))
                .filter(Util::isNotCompatible)
                .reduce((a, b) -> {
                    if (! (a instanceof AggregatedCompatibilityResult)) {
                        a = new AggregatedCompatibilityResult(a);
                    }

                    return ((AggregatedCompatibilityResult) a).add(b);
                })
                .orElse(CompatibilityResult.providerCompatible(provider));
    }

    /** 判断结果是否非 {@link CompatibilityResult.ExitCode#ROLLING} 兼容。 */
    public static boolean isNotCompatible(CompatibilityResult result) {
        return result.exitCode() != CompatibilityResult.ExitCode.ROLLING.value();
    }

    private static CompatibilityResult compare(String provider, String key, String old, String current) {
        return Objects.equals(old, current) ?
                CompatibilityResult.providerCompatible(provider) :
                CompatibilityResult.incompatibleAttribute(provider, key, old, current);
    }

}
