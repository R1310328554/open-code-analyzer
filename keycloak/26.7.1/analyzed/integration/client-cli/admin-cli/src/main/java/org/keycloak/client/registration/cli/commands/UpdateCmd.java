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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.keycloak.client.cli.common.AttributeOperation;
import org.keycloak.client.cli.config.ConfigData;
import org.keycloak.client.registration.cli.CmdStdinContext;
import org.keycloak.client.registration.cli.EndpointType;
import org.keycloak.client.registration.cli.EndpointTypeConverter;
import org.keycloak.client.registration.cli.ReflectionUtil;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonParseException;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static org.keycloak.client.cli.common.AttributeOperation.Type.DELETE;
import static org.keycloak.client.cli.common.AttributeOperation.Type.SET;
import static org.keycloak.client.cli.util.ConfigUtil.credentialsAvailable;
import static org.keycloak.client.cli.util.ConfigUtil.getRegistrationToken;
import static org.keycloak.client.cli.util.ConfigUtil.loadConfig;
import static org.keycloak.client.cli.util.ConfigUtil.saveMergeConfig;
import static org.keycloak.client.cli.util.ConfigUtil.setRegistrationToken;
import static org.keycloak.client.cli.util.HttpUtil.APPLICATION_JSON;
import static org.keycloak.client.cli.util.HttpUtil.doGet;
import static org.keycloak.client.cli.util.HttpUtil.doPut;
import static org.keycloak.client.cli.util.HttpUtil.urlencode;
import static org.keycloak.client.cli.util.IoUtil.printOut;
import static org.keycloak.client.cli.util.IoUtil.readFully;
import static org.keycloak.client.cli.util.IoUtil.warnfErr;
import static org.keycloak.client.cli.util.OsUtil.PROMPT;
import static org.keycloak.client.cli.util.ParseUtil.parseKeyVal;
import static org.keycloak.client.registration.cli.EndpointType.DEFAULT;
import static org.keycloak.client.registration.cli.EndpointType.OIDC;
import static org.keycloak.client.registration.cli.KcRegMain.CMD;

/**
 * {@code update} 子命令：更新已有客户端的配置。
 * <p>
 * 支持从文件覆盖、命令行属性增量修改、与服务端配置合并等多种模式；
 * 认证优先级为：{@code -t} 令牌 → 输入文件中的 registrationAccessToken → 本地配置 → 当前会话。
 * </p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
@Command(name = "update", description = "CLIENT_ID [ARGUMENTS]")
public class UpdateCmd extends AbstractAuthOptionsCmd {

    /** 注册端点类型，仅支持 {@code default} 与 {@code oidc}。 */
    @Option(names = {"-e", "--endpoint"}, description = "Endpoint type to use - one of: 'default', 'oidc'", converter = EndpointTypeConverter.class)
    private EndpointType regType = null;

    /** 包含完整或部分客户端定义的 JSON 文件路径，{@code -} 表示从标准输入读取。 */
    @Option(names = {"-f", "--file"}, description = "Use the file or standard input if '-' is specified")
    private String file = null;

    /** 为 {@code true} 时先拉取服务端现有配置再与本地/命令行变更合并后提交。 */
    @Option(names = {"-m", "--merge"}, description = "Merge new values with existing configuration on the server")
    private boolean mergeMode = false;

    /** 更新成功后是否将新配置输出到标准输出。 */
    @Option(names = {"-o", "--output"}, description = "After update output the new client configuration")
    private boolean outputClient = false;

    /** 为 {@code true} 时不美化 JSON 输出。 */
    @Option(names = {"-c", "--compressed"}, description = "Don't pretty print the output")
    private boolean compressed = false;

    /** 要更新的客户端 ID。 */
    @Parameters(arity = "0..1")
    String clientId;

    /** picocli 参数组：保持 {@code -s} 与 {@code -d} 操作在命令行中的相对顺序。 */
    static class AttributeOperations {
        @Option(names = {"-s", "--set"}, required = true) String set;
        @Option(names = {"-d", "--delete"}, required = true) String delete;
    }

    @ArgGroup(exclusive = true, multiplicity = "0..*")
    List<AttributeOperations> rawAttributeOperations = new ArrayList<>();

    /** 解析后的属性设置/删除操作列表。 */
    List<AttributeOperation> attrs = new LinkedList<>();

    /** 将 picocli 原始参数组转换为 {@link AttributeOperation} 列表。 */
    @Override
    protected void processOptions() {
        super.processOptions();

        for (AttributeOperations entry : rawAttributeOperations) {
            if (entry.delete != null) {
                attrs.add(new AttributeOperation(DELETE, entry.delete));
            } else {
                String[] keyVal = parseKeyVal(entry.set);
                attrs.add(new AttributeOperation(SET, keyVal[0], keyVal[1]));
            }
        }
    }

    /**
     * 执行更新流程：解析输入、确定合并模式、获取/合并配置并 PUT 到注册端点。
     */
    @Override
    protected void process() {
        if (clientId == null) {
            throw new IllegalArgumentException("CLIENT_ID not specified");
        }

        if (clientId.startsWith("-")) {
            warnfErr(CmdStdinContext.CLIENT_OPTION_WARN, clientId);
        }

        if (file == null && attrs.size() == 0) {
            throw new IllegalArgumentException("No file nor attribute values specified");
        }

        // 更新模式说明：
        //
        // A) 指定文件：用文件内容覆盖服务端状态（常规 get → 编辑 → update 流程）
        //
        // B) 文件 + 命令行覆盖：以文件为模板，命令行 -s/-d 覆盖文件中的值（适合批处理）
        //
        // C) 无文件、仅有属性：从服务端拉取当前配置，应用变更后回写（默认 merge 模式）
        //
        // D) --merge + 文件：先拉取服务端配置，再与文件/命令行变更合并
        //
        // SAML XML 不支持 update，仅 create。
        //
        if (file == null && attrs.size() > 0) {
            mergeMode = true;
        }

        CmdStdinContext ctx = new CmdStdinContext();
        if (file != null) {
            ctx = CmdStdinContext.parseFileOrStdin(file, regType);
            regType = ctx.getEndpointType();
        }

        if (regType == null) {
            regType = DEFAULT;
            ctx.setEndpointType(regType);
        } else if (regType != DEFAULT && regType != OIDC) {
            throw new RuntimeException("Update not supported for endpoint type: " + regType.getEndpoint());
        }

        // 在读取 stdin 之后再初始化配置，以便与 `get | update` 管道配合：
        // get 会消耗旧注册令牌并将新令牌写入配置
        ConfigData config = loadConfig();
        config = copyWithServerInfo(config);

        final String server = config.getServerUrl();
        final String realm = config.getRealm();

        if (externalToken == null) {
            // 未通过 --token 指定时，先检查 -s/-d 是否覆盖 registrationAccessToken，再尝试输入文件
            boolean processed = false;
            for (AttributeOperation op: attrs) {
                if ("registrationAccessToken".equals(op.getKey().toString())) {
                    processed = true;
                    if (op.getType() == AttributeOperation.Type.SET) {
                        externalToken = op.getValue();
                    }
                    // 否则为 delete，保持 externalToken 为 null
                    break;
                }
            }
            if (!processed) {
                externalToken = ctx.getRegistrationAccessToken();
            }
        }

        if (externalToken == null) {
            // 仍未指定时，从本地配置文件读取该客户端的注册访问令牌
            externalToken = getRegistrationToken(config.sessionRealmConfigData(), clientId);
        }

        setupTruststore(config);

        String auth = externalToken;
        if (auth == null) {
            config = ensureAuthInfo(config);
            config = copyWithServerInfo(config);
            if (credentialsAvailable(config)) {
                auth = ensureToken(config);
            }
        }

        auth = auth != null ? "Bearer " + auth : null;


        if (mergeMode) {
            InputStream response = doGet(server + "/realms/" + realm + "/clients-registrations/" + regType.getEndpoint() + "/" + urlencode(clientId),
                    APPLICATION_JSON, auth);

            String json = readFully(response);

            CmdStdinContext ctxremote = new CmdStdinContext();
            ctxremote.setContent(json);
            ctxremote.setEndpointType(regType);
            try {

                if (regType == DEFAULT) {
                    ctxremote.setClient(JsonSerialization.readValue(json, ClientRepresentation.class));
                    externalToken = ctxremote.getClient().getRegistrationAccessToken();
                } else if (regType == OIDC) {
                    ctxremote.setOidcClient(JsonSerialization.readValue(json, OIDCClientRepresentation.class));
                    externalToken = ctxremote.getOidcClient().getRegistrationAccessToken();
                }
            } catch (JsonParseException e) {
                throw new RuntimeException("Not a valid JSON document. " + e.getMessage(), e);
            } catch (IOException e) {
                throw new RuntimeException("Not a valid JSON document", e);
            }

            // 必须使用 GET 响应中的注册令牌，以保证乐观锁语义
            if (externalToken != null) {
                // 后续 doPut 使用此 auth
                auth = "Bearer " + externalToken;

                String newToken = externalToken;
                String clientToUpdate = clientId;
                saveMergeConfig(cfg -> {
                    setRegistrationToken(cfg.ensureRealmConfigData(server, realm), clientToUpdate, newToken);
                });
            }

            // 将本地/文件侧表示合并到远端拉取的配置上
            if (ctx.getClient() != null) {
                ReflectionUtil.merge(ctx.getClient(), ctxremote.getClient());
            } else if (ctx.getOidcClient() != null) {
                ReflectionUtil.merge(ctx.getOidcClient(), ctxremote.getOidcClient());
            }
            ctx = ctxremote;
        }

        if (attrs.size() > 0) {
            ctx = CmdStdinContext.mergeAttributes(ctx, attrs);
        }

        // 提交更新后的客户端配置
        InputStream response = doPut(server + "/realms/" + realm + "/clients-registrations/" + regType.getEndpoint() + "/" + urlencode(clientId),
                APPLICATION_JSON, APPLICATION_JSON, ctx.getContent(), auth);
        try {
            if (regType == DEFAULT) {
                ClientRepresentation clirep = JsonSerialization.readValue(response, ClientRepresentation.class);
                outputResult(clirep);
                externalToken = clirep.getRegistrationAccessToken();
            } else if (regType == OIDC) {
                OIDCClientRepresentation clirep = JsonSerialization.readValue(response, OIDCClientRepresentation.class);
                outputResult(clirep);
                externalToken = clirep.getRegistrationAccessToken();
            }

            String newToken = externalToken;
            String clientToUpdate = clientId;
            saveMergeConfig(cfg -> {
                setRegistrationToken(cfg.ensureRealmConfigData(server, realm), clientToUpdate, newToken);
            });

        } catch (IOException e) {
            throw new RuntimeException("Failed to process HTTP response", e);
        }
    }

    /**
     * 若启用了 {@code -o}，将更新后的客户端对象序列化并输出。
     *
     * @param result {@link ClientRepresentation} 或 {@link OIDCClientRepresentation}
     */
    private void outputResult(Object result) throws IOException {
        if (outputClient) {
            if (compressed) {
                printOut(JsonSerialization.writeValueAsString(result));
            } else {
                printOut(JsonSerialization.writeValueAsPrettyString(result));
            }
        }
    }

    /** 判断是否未提供任何有效参数。 */
    @Override
    protected boolean nothingToDo() {
        return super.nothingToDo() && regType == null && file == null && rawAttributeOperations.isEmpty() && clientId == null;
    }

    /** 返回 {@code update} 子命令的详细用法说明与示例。 */
    @Override
    protected String help() {
        StringWriter sb = new StringWriter();
        PrintWriter out = new PrintWriter(sb);
        out.println("Usage: " + CMD + " update CLIENT [ARGUMENTS]");
        out.println();
        out.println("Command to update an existing client configuration. If registration access token is specified it is used.");
        out.println("Otherwise, if 'registrationAccessToken' attribute is set, that is used. Otherwise, if registration access");
        out.println("token is available in configuration file, we use that. Finally, if it's not available anywhere, the current ");
        out.println("active session is used.");
        globalOptions(out);
        out.println("  Command specific options:");
        out.println("    CLIENT                ClientId of the client to update");
        out.println("    -t, --token TOKEN     Use the specified Registration Access Token for authorization");
        out.println("    -s, --set KEY=VALUE   Set specific attribute to a specified value");
        out.println("              KEY+=VALUE  Add item to an array");
        out.println("    -d, --delete NAME     Delete the specific attribute, or array item");
        out.println("    -e, --endpoint TYPE   Endpoint type to use - one of: 'default', 'oidc'");
        out.println("    -f, --file FILENAME   Use the file or standard input if '-' is specified");
        out.println("    -m, --merge           Merge new values with existing configuration on the server");
        out.println("                          Merge is automatically enabled unless --file is specified");
        out.println("    -o, --output          After update output the new client configuration");
        out.println("    -c, --compressed      Don't pretty print the output");
        out.println();
        out.println();
        out.println("Nested attributes are supported by using '.' to separate components of a KEY. Optionally, the KEY components ");
        out.println("can be quoted with double quotes - e.g. my_client.attributes.\"external.user.id\". If VALUE starts with [ and ");
        out.println("ends with ] the attribute will be set as a JSON array. If VALUE starts with { and ends with } the attribute ");
        out.println("will be set as a JSON object. If KEY ends with an array index - e.g. clients[3]=VALUE - then the specified item");
        out.println("of the array is updated. If KEY+=VALUE syntax is used, then KEY is assumed to be an array, and another item is");
        out.println("added to it.");
        out.println();
        out.println("Attributes can also be deleted. If KEY ends with an array index, then the targeted item of an array is removed");
        out.println("and the following items are shifted.");
        out.println();
        out.println("Merged mode fetches current configuration from the server, applies attribute changes to it, and sends it");
        out.println("back to the server, overwriting existing configuration there. If available, Registration Access Token is used ");
        out.println("for authorization when doing changes. Otherwise current session's authorization is used in which case user needs");
        out.println("manage-clients permission for update to work.");
        out.println();
        out.println();
        out.println("Examples:");
        out.println();
        out.println("Update a client by fetching current configuration from server, and applying specified changes.");
        out.println("  " + PROMPT + " " + CMD + " update my_client -s enabled=true -s 'redirectUris=[\"http://localhost:8080/myapp/*\"]'");
        out.println();
        out.println("Update a client by overwriting existing configuration on the server with a new one:");
        out.println("  " + PROMPT + " " + CMD + " update my_client -f new_my_client.json");
        out.println();
        out.println("Update a client by overwriting existing configuration using local file as a template:");
        out.println("  " + PROMPT + " " + CMD + " update my_client -f new_my_client.json -s enabled=true");
        out.println();
        out.println("Update client by fetching current configuration from server and merging with specified changes:");
        out.println("  " + PROMPT + " " + CMD + " update my_client -f new_my_client.json -s enabled=true --merge");
        out.println();
        out.println("Update a client using 'oidc' endpoint:");
        out.println("  " + PROMPT + " " + CMD + " update my_client -e oidc -s 'redirect_uris=[\"http://localhost:8080/myapp/*\"]'");
        out.println();
        out.println();
        out.println("Use '" + CMD + " help' for general information and a list of commands");
        return sb.toString();
    }
}
