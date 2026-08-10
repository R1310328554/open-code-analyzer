package org.keycloak.compatibility;

import java.util.Map;

import org.keycloak.common.Version;
import org.keycloak.migration.ModelVersion;

/**
 * A {@link CompatibilityMetadataProvider} implementation to provide the Keycloak version.
 */
public class KeycloakCompatibilityMetadataProvider implements CompatibilityMetadataProvider {

    /** 兼容性 provider id。 */
    public static final String ID = "keycloak";
    /** 元数据中版本字段键名。 */
    public static final String VERSION_KEY = "version";
    /** 当前 Keycloak 版本字符串。 */
    private final String version;

    // ServiceLoader 所需的无参构造
    @SuppressWarnings("unused")
    public KeycloakCompatibilityMetadataProvider() {
        this(Version.VERSION);
    }

    /** @param version 要报告的版本号 */
    public KeycloakCompatibilityMetadataProvider(String version) {
        this.version = version;
    }

    /** @return 仅含 version 键的单条目元数据 */
    @Override
    public Map<String, String> metadata() {
        return Map.of(VERSION_KEY, version);
    }

    /** 默认相等比较；同 major.minor 且 micro 不降级时允许滚动升级。 */
    @Override
    public CompatibilityResult isCompatible(Map<String, String> other) {
        CompatibilityResult equalComparison = CompatibilityMetadataProvider.super.isCompatible(other);

        // 对端为同 major.minor 的较早 micro 版本时允许滚动加入
        if (!Util.isNotCompatible(equalComparison)) {
            return equalComparison;
        }

        // 对端版本号缺失则沿用默认比较结果
        String otherVersion = other.get(VERSION_KEY);
        if (otherVersion == null)
            return equalComparison;

        // 仅当版本是唯一不兼容项时才尝试 micro 滚动规则
        boolean versionMismatch = equalComparison.incompatibleAttributes()
                .map(erroredAttributes -> erroredAttributes.size() == 1 && erroredAttributes.iterator().next().equals(VERSION_KEY))
                .orElse(false);

        if (!versionMismatch) {
            return equalComparison;
        }

        ModelVersion otherModelVersion = new ModelVersion(otherVersion);
        ModelVersion currentModelVersion = new ModelVersion(version);

        // 要求 major.minor 一致
        if (!currentModelVersion.hasSameMajorMinor(otherModelVersion)) {
            return equalComparison;
        }

        int otherMicro = otherModelVersion.getMicro();
        int currentMicro = currentModelVersion.getMicro();

        // 禁止滚动降级 micro 版本
        return currentMicro < otherMicro ?
                equalComparison :
                CompatibilityResult.providerCompatible(ID);
    }

    /** @return {@value #ID} */
    @Override
    public String getId() {
        return ID;
    }
}
