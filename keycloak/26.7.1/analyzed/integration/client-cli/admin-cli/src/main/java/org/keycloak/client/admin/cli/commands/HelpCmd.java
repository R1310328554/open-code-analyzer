/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.client.admin.cli.commands;

import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static org.keycloak.client.cli.util.IoUtil.printOut;

/**
 * {@code help} 子命令：输出 Admin CLI 总览或指定子命令的详细用法。
 * <p>
 * 无参数时打印 {@link KcAdmCmd#usage()}；带参数时按命令名分派到对应 {@code help()} 方法。
 */
@Command(name = "help", description = "This Help")
public class HelpCmd implements Runnable {

    /** 可选的子命令名称列表（如 {@code create}、{@code config credentials}）。 */
    @Parameters
    List<String> args;

    /** 根据参数输出全局或子命令帮助文本。 */
    @Override
    public void run() {
        if (args == null || args.size() == 0) {
            printOut(KcAdmCmd.usage());
        } else {
            outer: switch (args.get(0)) {
            case "config": {
                if (args.size() > 1) {
                    switch (args.get(1)) {
                    case "credentials": {
                        printOut(new ConfigCredentialsCmd().help());
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
            case "get-roles": {
                printOut(new GetRolesCmd().help());
                break;
            }
            case "add-roles": {
                printOut(new AddRolesCmd().help());
                break;
            }
            case "remove-roles": {
                printOut(new RemoveRolesCmd().help());
                break;
            }
            case "set-password": {
                printOut(new SetPasswordCmd().help());
                break;
            }
            case "new-object": {
                printOut(NewObjectCmd.usage());
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown command: " + args.get(0));
            }
            }
        }
    }
}
