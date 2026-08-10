package org.keycloak.scim.resource.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.keycloak.scim.resource.ResourceTypeRepresentation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SCIM Group 资源表示，包含显示名称与成员列表。
 * <p>对应 RFC 7643 核心 Group schema，用于组管理与成员关联。</p>
 */
public class Group extends ResourceTypeRepresentation {

    /** Group 核心 schema URN。 */
    public static final String SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Group";

    /** 组的可读显示名称。 */
    @JsonProperty("displayName")
    private String displayName;

    /** 组成员列表，每项引用一个 User 资源。 */
    @JsonProperty("members")
    private List<Member> members;

    @Override
    public Set<String> getSchemas() {
        return Set.of(SCHEMA);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    /** 按用户 ID 创建 {@link Member} 并追加到成员列表。 */
    public void addMember(String userId) {
        Member member = new Member();
        member.setValue(userId);
        addMember(member);
    }

    /** 追加已有 {@link Member} 到成员列表。 */
    public void addMember(Member member) {
        if (members == null) {
            members = new ArrayList<>();
        }
        members.add(member);
    }
}
