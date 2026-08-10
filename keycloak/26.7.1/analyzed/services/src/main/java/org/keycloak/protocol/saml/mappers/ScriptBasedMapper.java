package org.keycloak.protocol.saml.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperContainerModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.ScriptModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperConfigException;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.scripting.EvaluatableScriptAdapter;
import org.keycloak.scripting.ScriptCompilationException;
import org.keycloak.scripting.ScriptingProvider;

import org.jboss.logging.Logger;


/**
 * 基于 JavaScript 的 SAML 属性映射器：执行脚本计算 AttributeStatement 中的属性值。
 * <p>支持单值或多值（数组/集合）；多值可合并为单属性多值或拆分为多个属性，结果须可转为字符串。</p>
 * <p>需启用 {@link org.keycloak.common.Profile.Feature#SCRIPTS} 特性。</p>
 *
 * @author Alistair Doswald
 */
public class ScriptBasedMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper, EnvironmentDependentProviderFactory {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "saml-javascript-mapper";
    private static final String SINGLE_VALUE_ATTRIBUTE = "single";
    private static final Logger LOGGER = Logger.getLogger(ScriptBasedMapper.class);

    /* 静态配置块：定义管理控制台与后端共用的映射器配置项 */
    static {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setType(ProviderConfigProperty.SCRIPT_TYPE);
        property.setLabel(ProviderConfigProperty.SCRIPT_TYPE);
        property.setName(ProviderConfigProperty.SCRIPT_TYPE);
        property.setHelpText(
                "Script to compute the attribute value. \n" + //
                        " Available variables: \n" + //
                        " 'user' - the current user.\n" + //
                        " 'realm' - the current realm.\n" + //
                        " 'clientSession' - the current clientSession.\n" + //
                        " 'userSession' - the current userSession.\n" + //
                        " 'keycloakSession' - the current keycloakSession.\n\n" +
                        "To use: the last statement is the value returned to Java.\n" +
                        "The result will be tested if it can be iterated upon (e.g. an array or a collection).\n" +
                        " - If it is not, toString() will be called on the object to get the value of the attribute\n" +
                        " - If it is, toString() will be called on all elements to return multiple attribute values.\n"//
        );
        property.setDefaultValue("/**\n" + //
                " * Available variables: \n" + //
                " * user - the current user\n" + //
                " * realm - the current realm\n" + //
                " * clientSession - the current clientSession\n" + //
                " * userSession - the current userSession\n" + //
                " * keycloakSession - the current keycloakSession\n" + //
                " */\n\n\n//insert your code here..." //
        );
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(SINGLE_VALUE_ATTRIBUTE);
        property.setLabel("Single Value Attribute");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue("true");
        property.setHelpText("If true, all values will be stored under one attribute with multiple attribute values.");
        configProperties.add(property);
        AttributeStatementHelper.setConfigProperties(configProperties);
    }

    /** {@inheritDoc} 返回脚本与单值属性等配置项 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：Javascript Mapper */
    @Override
    public String getDisplayType() {
        return "Javascript Mapper";
    }

    /** {@inheritDoc} 归类为 AttributeStatement 映射器 */
    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    /** {@inheritDoc} 执行 JavaScript 根据上下文生成 SAML 属性值 */
    @Override
    public String getHelpText() {
        return "Evaluates a JavaScript function to produce an attribute value based on context information.";
    }

    /** {@inheritDoc} 需启用 SCRIPTS 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.SCRIPTS);
    }

    /**
     * 执行脚本并将结果写入 AttributeStatement。
     * <p>若脚本返回数组或可迭代对象，按「单属性多值」或「多属性」配置处理。</p>
     * @param attributeStatement 待追加属性的语句
     * @param mappingModel 映射器配置（对应控制台输入）
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSession 客户端会话
     */
    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel,
                                            KeycloakSession session, UserSessionModel userSession,
                                            AuthenticatedClientSessionModel clientSession) {
        UserModel user = userSession.getUser();
        String scriptSource = getScriptCode(mappingModel);
        RealmModel realm = userSession.getRealm();

        String single = mappingModel.getConfig().get(SINGLE_VALUE_ATTRIBUTE);
        boolean singleAttribute = Boolean.parseBoolean(single);

        ScriptingProvider scripting = session.getProvider(ScriptingProvider.class);
        ScriptModel scriptModel = scripting.createScript(realm.getId(), ScriptModel.TEXT_JAVASCRIPT, "attribute-mapper-script_" + mappingModel.getName(), scriptSource, null);

        EvaluatableScriptAdapter script = scripting.prepareEvaluatableScript(scriptModel);
        Object attributeValue;
        try {
            attributeValue = script.eval((bindings) -> {
                bindings.put("user", user);
                bindings.put("realm", realm);
                bindings.put("clientSession", clientSession);
                bindings.put("userSession", userSession);
                bindings.put("keycloakSession", session);
            });
            // 结果为数组或可迭代对象时展开全部元素
            if (attributeValue.getClass().isArray()){
                attributeValue = Arrays.asList((Object[])attributeValue);
            }
            if (attributeValue instanceof Iterable) {
                if (singleAttribute) {
                    AttributeType singleAttributeType = AttributeStatementHelper.createAttributeType(mappingModel);
                    attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(singleAttributeType));
                    for (Object value : (Iterable)attributeValue) {
                        singleAttributeType.addAttributeValue(value);
                    }
                } else {
                    for (Object value : (Iterable)attributeValue) {
                        AttributeStatementHelper.addAttribute(attributeStatement, mappingModel, value.toString());
                    }
                }
            } else {
                // 单值：直接写入一个 SAML 属性
                AttributeStatementHelper.addAttribute(attributeStatement, mappingModel, attributeValue.toString());
            }
        } catch (Exception ex) {
            LOGGER.error("Error during execution of ProtocolMapper script", ex);
            AttributeStatementHelper.addAttribute(attributeStatement, mappingModel, null);
        }
    }

    /** {@inheritDoc} 保存前校验脚本能否成功编译 */
    @Override
    public void validateConfig(KeycloakSession session, RealmModel realm, ProtocolMapperContainerModel client, ProtocolMapperModel mapperModel) throws ProtocolMapperConfigException {

        String scriptCode = getScriptCode(mapperModel);
        if (scriptCode == null) {
            return;
        }

        ScriptingProvider scripting = session.getProvider(ScriptingProvider.class);
        ScriptModel scriptModel = scripting.createScript(realm.getId(), ScriptModel.TEXT_JAVASCRIPT, mapperModel.getName() + "-script", scriptCode, "");

        try {
            scripting.prepareEvaluatableScript(scriptModel);
        } catch (ScriptCompilationException ex) {
            throw new ProtocolMapperConfigException("error", "{0}", ex.getMessage());
        }
    }

    /** 从映射器配置读取 JavaScript 脚本源码 */
    protected String getScriptCode(ProtocolMapperModel mappingModel) {
        return mappingModel.getConfig().get(ProviderConfigProperty.SCRIPT_TYPE);
    }

    /**
     * 工厂方法：创建脚本映射器配置（主要用于测试）。
     * @param name 映射器名称（无运行时作用）
     * @param samlAttributeName SAML 属性名
     * @param nameFormat 名称格式：basic、URI reference 或 unspecified
     * @param friendlyName 控制台友好名
     * @param script 待执行的 JavaScript
     * @param singleAttribute true 时多值合并为单属性
     * @return 协议映射器模型
     */
    public static ProtocolMapperModel create(String name, String samlAttributeName, String nameFormat, String friendlyName, String script, boolean singleAttribute) {
        ProtocolMapperModel mapper =  AttributeStatementHelper.createAttributeMapper(name, null, samlAttributeName, nameFormat, friendlyName,
                PROVIDER_ID);
        Map<String, String> config = mapper.getConfig();
        config.put(ProviderConfigProperty.SCRIPT_TYPE, script);
        config.put(SINGLE_VALUE_ATTRIBUTE, Boolean.toString(singleAttribute));
        return mapper;
    }
}
