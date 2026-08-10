package org.keycloak.protocol.docker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.AbstractLoginProtocolFactory;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.docker.mapper.AllowAllDockerProtocolMapper;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * Docker V2 登录协议工厂：注册 {@link DockerAuthV2Protocol}、内置 scope 映射器及协议端点。
 * <p>仅在启用 {@link Profile.Feature#DOCKER} 特性时可用。</p>
 */
public class DockerAuthV2ProtocolFactory extends AbstractLoginProtocolFactory implements EnvironmentDependentProviderFactory {

    static Map<String, ProtocolMapperModel> builtins = new HashMap<>();
    static List<ProtocolMapperModel> defaultBuiltins = new ArrayList<>();

    static {
        final ProtocolMapperModel addAllRequestedScopeMapper = new ProtocolMapperModel();
        addAllRequestedScopeMapper.setName(AllowAllDockerProtocolMapper.PROVIDER_ID);
        addAllRequestedScopeMapper.setProtocolMapper(AllowAllDockerProtocolMapper.PROVIDER_ID);
        addAllRequestedScopeMapper.setProtocol(DockerAuthV2Protocol.LOGIN_PROTOCOL);
        addAllRequestedScopeMapper.setConfig(Collections.emptyMap());
        builtins.put(AllowAllDockerProtocolMapper.PROVIDER_ID, addAllRequestedScopeMapper);
        defaultBuiltins.add(addAllRequestedScopeMapper);
    }

    @Override
    /** Docker 协议不创建默认客户端作用域。 */
    protected void createDefaultClientScopesImpl(RealmModel newRealm) {
        // no-op
    }

    @Override
    /** 为新 Docker 客户端添加内置协议映射器。 */
    protected void addDefaults(final ClientModel client) {
        defaultBuiltins.forEach(builtinMapper -> client.addProtocolMapper(builtinMapper));
    }

    @Override
    /** @return 内置 Docker 协议映射器注册表 */
    public Map<String, ProtocolMapperModel> getBuiltinMappers() {
        return builtins;
    }

    @Override
    /** @return Docker 协议 REST 端点 {@link DockerV2LoginProtocolService} */
    public Object createProtocolEndpoint(final KeycloakSession session, final EventBuilder event) {
        return new DockerV2LoginProtocolService(session, event);
    }

    @Override
    public void setupClientDefaults(final ClientRepresentation rep, final ClientModel newClient) {
        // no-op
    }


    @Override
    /** 创建绑定会话的 {@link DockerAuthV2Protocol} 实例。 */
    public LoginProtocol create(final KeycloakSession session) {
        return new DockerAuthV2Protocol().setSession(session);
    }

    @Override
    /** @return 协议 ID {@link DockerAuthV2Protocol#LOGIN_PROTOCOL} */
    public String getId() {
        return DockerAuthV2Protocol.LOGIN_PROTOCOL;
    }

    @Override
    /** 是否启用 Docker 特性配置文件。 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.DOCKER);
    }

    @Override
    /** @return 工厂排序权重（-100，优先加载） */
    public int order() {
        return -100;
    }
}
