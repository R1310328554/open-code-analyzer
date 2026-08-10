package org.keycloak.config;

import java.util.Arrays;
import java.util.List;

import org.keycloak.common.Profile;
import org.keycloak.common.crypto.FipsMode;

/**
 * 安全与 FIPS 合规模式相关配置选项。
 */
public class SecurityOptions {

    /** 配置选项：fips-mode，FIPS 运行模式。 */
    public static final Option<FipsMode> FIPS_MODE = new OptionBuilder<>("fips-mode", FipsMode.class)
            .category(OptionCategory.SECURITY)
            .expectedValues(getFipsModeValues())
            .buildTime(true)
            .description("Sets the FIPS mode. If '" + FipsMode.NON_STRICT + "' is set, FIPS is enabled but on non-approved mode. For full FIPS compliance, set '" + FipsMode.STRICT + "' to run on approved mode. "
                    + "This option defaults to '" + FipsMode.DISABLED + "' when '" + Profile.Feature.FIPS.getKey() + "' feature is disabled, which is by default. "
                    + "This option defaults to '" + FipsMode.NON_STRICT + "' when '" + Profile.Feature.FIPS.getKey() + "' feature is enabled.")
            .defaultValue(FipsMode.DISABLED)
            .build();

    /** 返回 FIPS 模式下可用的枚举取值列表。 */
    private static List<String> getFipsModeValues() {
        return Arrays.asList(FipsMode.NON_STRICT.toString(), FipsMode.STRICT.toString());
    }
}
