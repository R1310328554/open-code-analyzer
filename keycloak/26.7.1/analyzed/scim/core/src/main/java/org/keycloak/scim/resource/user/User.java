package org.keycloak.scim.resource.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.keycloak.scim.resource.ResourceTypeRepresentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.keycloak.scim.resource.Scim.ENTERPRISE_USER_SCHEMA;

/**
 * SCIM User 核心资源类型（RFC 7643 第 4.1 节）。
 * <p>映射用户名、姓名、邮箱、组及企业扩展等属性。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends ResourceTypeRepresentation {

    /** 唯一用户名（登录标识）。 */
    @JsonProperty("userName")
    private String userName;

    /** 结构化姓名。 */
    @JsonProperty("name")
    private Name name;

    /** 显示名称。 */
    @JsonProperty("displayName")
    private String displayName;

    /** 昵称。 */
    @JsonProperty("nickName")
    private String nickName;

    /** 个人资料 URL。 */
    @JsonProperty("profileUrl")
    private String profileUrl;

    /** 职位/头衔。 */
    @JsonProperty("title")
    private String title;

    /** 用户类型（如 Employee、Contractor）。 */
    @JsonProperty("userType")
    private String userType;

    /** 首选语言（BCP 47 格式）。 */
    @JsonProperty("preferredLanguage")
    private String preferredLanguage;

    /** 区域设置。 */
    @JsonProperty("locale")
    private String locale;

    /** 时区。 */
    @JsonProperty("timezone")
    private String timezone;

    /** 账户是否激活。 */
    @JsonProperty("active")
    private Boolean active;

    /** 邮箱列表。 */
    @JsonProperty("emails")
    private List<Email> emails;

    /** 所属组列表。 */
    @JsonProperty("groups")
    private List<GroupMembership> groups;

    // Enterprise User Extension
    /** 企业用户扩展属性。 */
    @JsonProperty("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User")
    private EnterpriseUser enterpriseUser;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public List<GroupMembership> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupMembership> groups) {
        this.groups = groups;
    }

    public EnterpriseUser getEnterpriseUser() {
        return enterpriseUser;
    }

    public void setEnterpriseUser(EnterpriseUser enterpriseUser) {
        this.enterpriseUser = enterpriseUser;
    }

    /** 获取名（不参与 JSON 序列化）。 */
    @JsonIgnore
    public String getFirstName() {
        return Optional.ofNullable(name).map(Name::getGivenName).orElse(null);
    }

    /** 设置名，必要时自动创建 {@link Name} 实例。 */
    public void setFirstName(String firstName) {
        name = Optional.ofNullable(name).orElseGet(Name::new);
        name.setGivenName(firstName);
    }

    /** 获取姓（不参与 JSON 序列化）。 */
    @JsonIgnore
    public String getLastName() {
        return Optional.ofNullable(name).map(Name::getFamilyName).orElse(null);
    }

    /** 设置姓，必要时自动创建 {@link Name} 实例。 */
    public void setLastName(String lastName) {
        name = Optional.ofNullable(name).orElseGet(Name::new);
        name.setFamilyName(lastName);
    }

    /** 获取首个邮箱地址（不参与 JSON 序列化）。 */
    @JsonIgnore
    public String getEmail() {
        if (emails == null || emails.isEmpty()) {
            return null;
        }
        return emails.get(0).getValue();
    }

    /** 将邮箱列表设为单个主工作邮箱。 */
    public void setEmail(String email) {
        emails = List.of(new Email(email));
    }

    @Override
    public Set<String> getSchemas() {
        Set<String> schemas = super.getSchemas();
        if (enterpriseUser != null) {
            schemas.add(ENTERPRISE_USER_SCHEMA);
        }
        return schemas;
    }

    /**
     * 按组 ID 添加组成员关系。
     *
     * @param id 组标识符
     */
    public void addGroup(String id) {
        GroupMembership membership = new GroupMembership();

        membership.setValue(id);

        addGroup(membership);
    }

    /** 添加组成员关系条目。 */
    public void addGroup(GroupMembership membership) {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        groups.add(membership);
    }
}
