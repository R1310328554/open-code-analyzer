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
package org.keycloak.client.registration.cli.commands;

import org.keycloak.client.cli.common.BaseConfigTruststoreCmd;
import org.keycloak.client.registration.cli.KcRegMain;

import picocli.CommandLine.Command;

/**
 * {@code kcreg config truststore} 子命令：配置 TLS 信任库路径。
 * <p>
 * 继承 {@link BaseConfigTruststoreCmd}，供 HTTPS 注册端点校验服务器证书。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
@Command(name = "truststore", description = "PATH [ARGUMENTS]")
public class ConfigTruststoreCmd extends BaseConfigTruststoreCmd {

    /** 绑定注册 CLI 的 {@link KcRegMain#COMMAND_STATE}。 */
    public ConfigTruststoreCmd() {
        super(KcRegMain.COMMAND_STATE);
    }

}
