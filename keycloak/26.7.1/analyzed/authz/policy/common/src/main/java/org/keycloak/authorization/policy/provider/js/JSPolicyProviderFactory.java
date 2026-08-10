package org.keycloak.authorization.policy.provider.js;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.ScriptModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.representations.idm.authorization.JSPolicyRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.scripting.EvaluatableScriptAdapter;
import org.keycloak.scripting.ScriptingProvider;

/**
 * JavaScript 策略 SPI 工厂：管理脚本编译缓存，并在 SCRIPTS 特性启用时注册策略类型。
 * <p>
 * 非部署脚本默认禁止上传/创建，需会话属性 {@code ALLOW_CREATE_POLICY} 或部署态工厂。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class JSPolicyProviderFactory implements PolicyProviderFactory<JSPolicyRepresentation>, EnvironmentDependentProviderFactory {

    /** 共享的 JS 策略提供者 */
    private final JSPolicyProvider provider = new JSPolicyProvider(this::getEvaluatableScript);
    /** 已编译脚本适配器缓存 */
    private ScriptCache scriptCache;

    @Override
    public String getName() {
        return "JavaScript";
    }

    @Override
    public String getGroup() {
        return "Rule Based";
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    @Override
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    @Override
    public JSPolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        JSPolicyRepresentation representation = new JSPolicyRepresentation();
        representation.setCode(policy.getConfig().get("code"));
        return representation;
    }

    @Override
    public Class<JSPolicyRepresentation> getRepresentationType() {
        return JSPolicyRepresentation.class;
    }

    @Override
    public void onCreate(Policy policy, JSPolicyRepresentation representation, AuthorizationProvider authorization) {
        throwCanNotUpdatePolicy(authorization);
    }

    @Override
    public void onUpdate(Policy policy, JSPolicyRepresentation representation, AuthorizationProvider authorization) {
        policy.setDecisionStrategy(representation.getDecisionStrategy());
        policy.setDescription(policy.getDescription());
        policy.setLogic(policy.getLogic());
    }

    @Override
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        throwCanNotUpdatePolicy(authorization);
    }

    @Override
    public void onRemove(final Policy policy, final AuthorizationProvider authorization) {
        scriptCache.remove(policy.getId());
    }

    /** 从配置读取缓存容量与条目 TTL，初始化 {@link ScriptCache} */
    @Override
    public void init(Config.Scope config) {
        int maxEntries = Integer.parseInt(config.get("cache-max-entries", "100"));
        int maxAge = Integer.parseInt(config.get("cache-entry-max-age", "-1"));
        scriptCache = new ScriptCache(maxEntries, maxAge);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "js";
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    /** 按策略 ID 缓存已编译脚本，避免重复编译 */
    private EvaluatableScriptAdapter getEvaluatableScript(final AuthorizationProvider authz, final Policy policy) {
        return scriptCache.computeIfAbsent(policy.getId(), id -> {
            final ScriptingProvider scripting = authz.getKeycloakSession().getProvider(ScriptingProvider.class);
            ScriptModel script = getScriptModel(policy, authz.getRealm(), scripting);
            return scripting.prepareEvaluatableScript(script);
        });
    }

    protected ScriptModel getScriptModel(final Policy policy, final RealmModel realm, final ScriptingProvider scripting) {
        String scriptName = policy.getName();
        String scriptCode = policy.getConfig().get("code");
        String scriptDescription = policy.getDescription();

        // TODO 按 scriptId 查找脚本，而非每次从配置创建
        return scripting.createScript(realm.getId(), ScriptModel.TEXT_JAVASCRIPT, scriptName, scriptCode, scriptDescription);
    }

    /** 子类可覆盖：部署脚本工厂返回 {@code true} */
    protected boolean isDeployed() {
        return false;
    }

    /** 未显式允许且非部署脚本时，禁止创建/导入含自定义代码的策略 */
    private void throwCanNotUpdatePolicy(AuthorizationProvider authorization) {
        if (!authorization.getKeycloakSession().getAttributeOrDefault("ALLOW_CREATE_POLICY", false) && !isDeployed()) {
            throw new RuntimeException("Script upload is disabled");
        }
    }

    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.SCRIPTS);
    }
}
