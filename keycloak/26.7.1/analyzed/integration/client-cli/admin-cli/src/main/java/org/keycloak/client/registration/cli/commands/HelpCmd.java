package org.keycloak.client.registration.cli.commands;

import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static org.keycloak.client.cli.util.IoUtil.printOut;

/**
 * {@code help} 子命令：输出 kcreg CLI 的总体帮助或指定子命令的详细说明。
 * <p>
 * 无参数时打印顶层用法；带参数时按命令名（如 {@code config}、{@code create}、{@code get}）
 * 分派到对应命令类的 {@code help()} / {@code usage()} 方法。
 * </p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
@Command(name = "help", description = "This help")
public class HelpCmd implements Runnable {

    /** 可选的子命令名称列表，用于查询特定命令的帮助文本。 */
    @Parameters
    private List<String> args;

    /**
     * 根据 {@link #args} 内容输出总体或分命令帮助信息。
     */
    @Override
    public void run() {
        if (args == null || args.size() == 0) {
            printOut(KcRegCmd.usage());
        } else {
            outer:
            switch (args.get(0)) {
                case "config": {
                    if (args.size() > 1) {
                        switch (args.get(1)) {
                            case "credentials": {
                                printOut(new ConfigCredentialsCmd().help());
                                break outer;
                            }
                            case "initial-token": {
                                printOut(ConfigInitialTokenCmd.usage());
                                break outer;
                            }
                            case "registration-token": {
                                printOut(ConfigRegistrationTokenCmd.usage());
                                break outer;
                            }
                            case "truststore": {
                                printOut(new ConfigTruststoreCmd().help());
                                break outer;
                            }
                        }
                    }
                    printOut(ConfigCmd.usage());
                    break;
                }
                case "create": {
                    printOut(new CreateCmd().help());
                    break;
                }
                case "get": {
                    printOut(new GetCmd().help());
                    break;
                }
                case "update": {
                    printOut(new UpdateCmd().help());
                    break;
                }
                case "delete": {
                    printOut(new DeleteCmd().help());
                    break;
                }
                case "attrs": {
                    printOut(AttrsCmd.usage());
                    break;
                }
                case "update-token": {
                    printOut(new UpdateTokenCmd().help());
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown command: " + args.get(0));
                }
            }
        }
    }
}
