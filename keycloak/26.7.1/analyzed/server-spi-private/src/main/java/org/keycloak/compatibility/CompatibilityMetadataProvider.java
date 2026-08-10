package org.keycloak.compatibility;

import java.util.Map;

/**
 * 为 {@code update-compatibility} 命令提供元数据的 SPI。
 * <p>
 * 实现应返回判断两次 Keycloak 部署间能否兼容升级所需的全部键值对；后续版本可能增删元数据键，实现需优雅处理缺失项。
 * </p>
 * <p>
 * {@link CompatibilityResult} 表示是否可滚动更新；工厂方法提供 {@link CompatibilityResult} 的默认实现。
 * </p>
 */
public interface CompatibilityMetadataProvider {

    /** 默认优先级，用于同 ID 多实现时的排序。 */
    int DEFAULT_PRIORITY = 1;

    /**
     * 返回需持久化的兼容性元数据；空映射表示不保存本实现的信息。
     *
     * Provides the metadata to be persisted.
     * <p>
     * If an empty {@link Map} is returned, no information about this implementation will be persisted. A {@code null}
     * return value is not supported, and it will interrupt the process.
     *
     * @return The metadata required by this provider to determine if a rolling update is possible.
     */
    Map<String, String> metadata();

    /**
     * 将当前元数据与另一部署的元数据比较，默认相等则允许滚动更新。
     *
     * It compares the current metadata with {@code other} from another deployment.
     * <p>
     * The default implementation will allow a rolling update if the metadata from the current server is equal to the
     * {@code other}. Implementations can overwrite this method as required.
     *
     * @param other The other deployment metadata. It only contains the metadata from this implementation.
     * @return The {@link CompatibilityResult} with the outcome.
     * @see CompatibilityResult
     */
    default CompatibilityResult isCompatible(Map<String, String> other) {
        return Util.isCompatible(getId(), other, metadata());
    }

    /**
     * @return 优先级；当多个实现共享同一 {@link #getId()} 或需替换内置实现时生效
     * the default implementation shipped in Keycloak.
     */
    default int priority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * @return 实现唯一标识；相同 ID 与优先级的并存实现无效
     * valid.
     */
    String getId();
}
