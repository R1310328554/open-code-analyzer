package org.keycloak.ssf.subject;

import org.keycloak.ssf.event.caep.CaepSessionRevoked;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 复合主体标识符，将 user、device、session 等多个 {@link SubjectId} 子字段组合为单一 Subject。
 * <p>参见 https://openid.net/specs/openid-sse-framework-1_0.html#complex-subjects</p>
 *
 * <p>各嵌套字段类型均为抽象 {@link SubjectId}，须配合 {@link JsonDeserialize}
 * 与 {@link SubjectIdJsonDeserializer} 按 {@code format} 判别式反序列化；
 * 否则 Jackson 默认 bean 反序列化会尝试实例化抽象类而失败——接收方解析携带复合主体的
 * SET（如 {@link CaepSessionRevoked} 事件上的 {@code ComplexSubjectId{user: IssuerSubjectId, session: OpaqueSubjectId}}）时即会触发。</p>
 */
public class ComplexSubjectId extends SubjectId {

    public static final String TYPE = "complex";

    /** 与事件相关的用户主体。 */
    @JsonProperty("user")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId user;

    /** 与事件相关的设备主体。 */
    @JsonProperty("device")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId device;

    /** 与事件相关的会话主体。 */
    @JsonProperty("session")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId session;

    /** 与事件相关的应用主体。 */
    @JsonProperty("application")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId application;

    /** 与事件相关的租户主体。 */
    @JsonProperty("tenant")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId tenant;

    /** 与事件相关的组织单元主体。 */
    @JsonProperty("org_unit")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId orgUnit;

    /** 与事件相关的组主体。 */
    @JsonProperty("group")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId group;

    public ComplexSubjectId() {
        super(TYPE);
    }

    public SubjectId getUser() {
        return user;
    }

    public void setUser(SubjectId user) {
        this.user = user;
    }

    public SubjectId getDevice() {
        return device;
    }

    public void setDevice(SubjectId device) {
        this.device = device;
    }

    public SubjectId getSession() {
        return session;
    }

    public void setSession(SubjectId session) {
        this.session = session;
    }

    public SubjectId getApplication() {
        return application;
    }

    public void setApplication(SubjectId application) {
        this.application = application;
    }

    public SubjectId getTenant() {
        return tenant;
    }

    public void setTenant(SubjectId tenant) {
        this.tenant = tenant;
    }

    public SubjectId getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(SubjectId orgUnit) {
        this.orgUnit = orgUnit;
    }

    public SubjectId getGroup() {
        return group;
    }

    public void setGroup(SubjectId group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "ComplexSubjectId{" +
               "user=" + user +
               ", device=" + device +
               ", session=" + session +
               ", application=" + application +
               ", tenant=" + tenant +
               ", orgUnit=" + orgUnit +
               ", group=" + group +
               '}';
    }
}
