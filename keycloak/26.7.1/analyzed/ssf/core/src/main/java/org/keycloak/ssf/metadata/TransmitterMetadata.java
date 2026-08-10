package org.keycloak.ssf.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SSF Transmitter 元数据文档（Well-Known 响应体）。
 * <p>描述 Transmitter 能力、端点 URL 及 subject 投递策略等，
 * 供 Receiver 发现与配置流。</p>
 */
public class TransmitterMetadata {

    /** SSF 规范版本标识。 */
    @JsonProperty("spec_version")
    private String specVersion;

    /** Transmitter 签发者标识（issuer）。 */
    @JsonProperty("issuer")
    private String issuer;

    /** SET 签名验证用 JWKS 端点 URI。 */
    @JsonProperty("jwks_uri")
    private String jwksUri;

    /** Transmitter 支持的投递方式 URI 集合。 */
    @JsonProperty("delivery_methods_supported")
    private Set<String> deliveryMethodSupported;

    /** 流配置端点 URL。 */
    @JsonProperty("configuration_endpoint")
    private String configurationEndpoint;

    /** 流状态查询端点 URL。 */
    @JsonProperty("status_endpoint")
    private String statusEndpoint;

    /** 添加 subject 订阅端点 URL。 */
    @JsonProperty("add_subject_endpoint")
    private String addSubjectEndpoint;

    /** 移除 subject 订阅端点 URL。 */
    @JsonProperty("remove_subject_endpoint")
    private String removeSubjectEndpoint;

    /** 端点验证（Verification）URL。 */
    @JsonProperty("verification_endpoint")
    private String verificationEndpoint;

    /** 关键 subject 成员声明名集合。 */
    @JsonProperty("critical_subject_members")
    private Set<String> criticalSubjectMembers;

    /** 默认 subject 投递策略（{@link DefaultSubjects} 字符串值）。 */
    @JsonProperty("default_subjects")
    private String defaultSubjects;

    /** 支持的授权方案列表。 */
    @JsonProperty("authorization_schemes")
    private List<Map<String, Object>> authorizationSchemes;

    @JsonIgnore
    private final Map<String, Object> metadata = new HashMap<String, Object>();

    public TransmitterMetadata() {
    }

    /**
     * 拷贝构造器。对集合字段做浅拷贝，使新实例可独立于 {@code other} 变更。
     * <p>{@link #authorizationSchemes} 内部的 {@code Map<String, Object>} 条目
     * 仍共享引用——若需变更单个 scheme 映射，调用方应自行拷贝。</p>
     * @param other 源实例
     */
    public TransmitterMetadata(TransmitterMetadata other) {
        this.specVersion = other.specVersion;
        this.issuer = other.issuer;
        this.jwksUri = other.jwksUri;
        this.deliveryMethodSupported = other.deliveryMethodSupported == null
                ? null : new HashSet<>(other.deliveryMethodSupported);
        this.configurationEndpoint = other.configurationEndpoint;
        this.statusEndpoint = other.statusEndpoint;
        this.addSubjectEndpoint = other.addSubjectEndpoint;
        this.removeSubjectEndpoint = other.removeSubjectEndpoint;
        this.verificationEndpoint = other.verificationEndpoint;
        this.criticalSubjectMembers = other.criticalSubjectMembers == null
                ? null : new HashSet<>(other.criticalSubjectMembers);
        this.defaultSubjects = other.defaultSubjects;
        this.authorizationSchemes = other.authorizationSchemes == null
                ? null : new ArrayList<>(other.authorizationSchemes);
        this.metadata.putAll(other.metadata);
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public Set<String> getDeliveryMethodSupported() {
        return deliveryMethodSupported;
    }

    public void setDeliveryMethodSupported(Set<String> deliveryMethodSupported) {
        this.deliveryMethodSupported = deliveryMethodSupported;
    }

    public String getConfigurationEndpoint() {
        return configurationEndpoint;
    }

    public void setConfigurationEndpoint(String configurationEndpoint) {
        this.configurationEndpoint = configurationEndpoint;
    }

    public String getStatusEndpoint() {
        return statusEndpoint;
    }

    public void setStatusEndpoint(String statusEndpoint) {
        this.statusEndpoint = statusEndpoint;
    }

    public String getAddSubjectEndpoint() {
        return addSubjectEndpoint;
    }

    public void setAddSubjectEndpoint(String addSubjectEndpoint) {
        this.addSubjectEndpoint = addSubjectEndpoint;
    }

    public String getRemoveSubjectEndpoint() {
        return removeSubjectEndpoint;
    }

    public void setRemoveSubjectEndpoint(String removeSubjectEndpoint) {
        this.removeSubjectEndpoint = removeSubjectEndpoint;
    }

    public String getVerificationEndpoint() {
        return verificationEndpoint;
    }

    public void setVerificationEndpoint(String verificationEndpoint) {
        this.verificationEndpoint = verificationEndpoint;
    }

    public Set<String> getCriticalSubjectMembers() {
        return criticalSubjectMembers;
    }

    public void setCriticalSubjectMembers(Set<String> criticalSubjectMembers) {
        this.criticalSubjectMembers = criticalSubjectMembers;
    }

    public String getDefaultSubjects() {
        return defaultSubjects;
    }

    public void setDefaultSubjects(String defaultSubjects) {
        this.defaultSubjects = defaultSubjects;
    }

    public List<Map<String, Object>> getAuthorizationSchemes() {
        return authorizationSchemes;
    }

    public void setAuthorizationSchemes(List<Map<String, Object>> authorizationSchemes) {
        this.authorizationSchemes = authorizationSchemes;
    }

    @JsonAnySetter
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "TransmitterMetadata{" +
               "specVersion='" + specVersion + '\'' +
               ", issuer='" + issuer + '\'' +
               ", jwksUri='" + jwksUri + '\'' +
               ", deliveryMethodSupported=" + deliveryMethodSupported +
               ", configurationEndpoint='" + configurationEndpoint + '\'' +
               ", statusEndpoint='" + statusEndpoint + '\'' +
               ", addSubjectEndpoint='" + addSubjectEndpoint + '\'' +
               ", removeSubjectEndpoint='" + removeSubjectEndpoint + '\'' +
               ", verificationEndpoint='" + verificationEndpoint + '\'' +
               ", criticalSubjectMembers=" + criticalSubjectMembers +
               ", defaultSubjects='" + defaultSubjects + '\'' +
               ", authorizationSchemes=" + authorizationSchemes +
               ", metadata=" + metadata +
               '}';
    }
}
