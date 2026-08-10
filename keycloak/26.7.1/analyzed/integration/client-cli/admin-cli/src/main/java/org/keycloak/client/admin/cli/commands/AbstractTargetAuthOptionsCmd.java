package org.keycloak.client.admin.cli.commands;

import org.keycloak.client.admin.cli.KcAdmMain;
import org.keycloak.client.cli.common.BaseAuthOptionsCmd;
import org.keycloak.client.cli.config.ConfigData;

import picocli.CommandLine.Option;

/**
 * 扩展 {@link org.keycloak.client.cli.common.BaseAuthOptionsCmd}，增加令牌认证与目标领域（target realm）支持。
 * <p>
 * 允许 CLI 在认证领域与实际操作领域不同时，通过 {@code --target-realm} 指定请求目标。
 */
public abstract class AbstractTargetAuthOptionsCmd extends BaseAuthOptionsCmd implements GlobalOptionsCmdHelper {

    /** 操作目标领域；未指定时使用配置文件中的认证领域。 */
    @Option(names = {"-r", "--target-realm"}, description = "Realm to target - when it's different than the realm we authenticate against")
    protected String targetRealm;

    /** 直接使用外部令牌，设置后忽略其他认证选项。 */
    @Option(names = "--token", description = "Token to use for invocations.  With this option set, every other authentication option is ignored")
    public void setToken(String token) {
        this.externalToken = token;
    }

    public AbstractTargetAuthOptionsCmd() {
        super(KcAdmMain.COMMAND_STATE);
    }

    /** 从父命令继承目标领域等选项。 */
    protected final void initFromParent(AbstractTargetAuthOptionsCmd parent) {
        super.initFromParent(parent);
        targetRealm = parent.targetRealm;
    }

    /** 解析实际请求目标领域：优先命令行选项，否则取配置中的认证领域。 */
    protected String getTargetRealm(ConfigData config) {
        return targetRealm != null ? targetRealm : config.getRealm();
    }
}
