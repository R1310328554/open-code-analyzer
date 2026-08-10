package org.keycloak.representations.idm;

/**
 * 组织成员邀请的 REST 表示，包含受邀人信息与邀请状态。
 */
public class OrganizationInvitationRepresentation {

    /** 邀请生命周期状态。 */
    public enum Status {
        /** 待接受。 */
        PENDING,
        /** 已过期。 */
        EXPIRED
    }

    /** 邀请记录内部 ID。 */
    private String id;
    /** 目标组织 ID。 */
    private String organizationId;
    /** 受邀人邮箱。 */
    private String email;
    /** 受邀人名。 */
    private String firstName;
    /** 受邀人姓。 */
    private String lastName;
    /** 邀请发送时间（Unix 秒）。 */
    private int sentDate;
    /** 邀请过期时间（Unix 秒）。 */
    private int expiresAt;
    /** 当前邀请状态。 */
    private Status status;
    /** 受邀人点击接受的邀请链接。 */
    private String inviteLink;

    /** 无参构造。 */
    public OrganizationInvitationRepresentation() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getSentDate() {
        return sentDate;
    }

    public void setSentDate(int sentDate) {
        this.sentDate = sentDate;
    }

    public int getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(int expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getInviteLink() {
        return inviteLink;
    }

    public void setInviteLink(String inviteLink) {
        this.inviteLink = inviteLink;
    }

    /** 基于邀请 ID 比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationInvitationRepresentation that = (OrganizationInvitationRepresentation) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    /** 基于邀请 ID 计算哈希。 */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
