package org.keycloak.client.registration.cli.commands;

import org.keycloak.client.cli.common.BaseAuthOptionsCmd;
import org.keycloak.client.registration.cli.KcRegMain;

import picocli.CommandLine.Option;

/**
 * 客户端注册 CLI 的认证选项命令基类。
 * <p>
 * 在 {@link BaseAuthOptionsCmd} 基础上增加 {@code -t/--token} 外部令牌选项，
 * 绑定 {@link KcRegMain#COMMAND_STATE} 作为配置上下文。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public abstract class AbstractAuthOptionsCmd extends BaseAuthOptionsCmd {

    /** 设置初始访问令牌或注册访问令牌（覆盖配置文件中的值）。 */
    @Option(names = {"-t", "--token"}, description = "Initial / Registration access token to use)")
    public void setToken(String token) {
        this.externalToken = token;
    }

    /** 使用 {@link KcRegMain#COMMAND_STATE} 初始化认证选项。 */
    public AbstractAuthOptionsCmd() {
        super(KcRegMain.COMMAND_STATE);
    }

}
