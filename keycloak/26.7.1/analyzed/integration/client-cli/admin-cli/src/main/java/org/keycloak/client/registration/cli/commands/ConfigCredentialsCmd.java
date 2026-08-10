package org.keycloak.client.registration.cli.commands;

import org.keycloak.client.cli.common.BaseConfigCredentialsCmd;
import org.keycloak.client.registration.cli.KcRegMain;

import picocli.CommandLine.Command;

/**
 * {@code kcreg config credentials} 子命令：配置服务器登录凭据。
 * <p>
 * 继承 {@link BaseConfigCredentialsCmd}，将会话令牌写入 {@code kcreg.config}。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
@Command(name = "credentials", description = "--server SERVER_URL --realm REALM [ARGUMENTS]")
public class ConfigCredentialsCmd extends BaseConfigCredentialsCmd {

    /** 绑定注册 CLI 的 {@link KcRegMain#COMMAND_STATE}。 */
    public ConfigCredentialsCmd() {
        super(KcRegMain.COMMAND_STATE);
    }

}
