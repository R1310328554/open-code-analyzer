/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models;

import org.keycloak.common.util.Time;

/**
 * 组织邀请模型：表示向用户发出的加入组织邀请。
 * Model representing an organization invitation.
 */
public interface OrganizationInvitationModel {

    /**
     * 组织邀请查询过滤器。
     * Filters for querying organization invitations.
     *
     * <p>{@link #EMAIL}, {@link #FIRST_NAME}, and {@link #LAST_NAME} perform case-insensitive exact matching.
     * Use {@link #SEARCH} for case-insensitive substring matching across email, firstName, and lastName fields.
     */
    enum Filter {
        /** 按邀请状态精确匹配（如 PENDING、EXPIRED） */ STATUS,
        /** 名（不区分大小写精确匹配） */ FIRST_NAME,
        /** 姓（不区分大小写精确匹配） */ LAST_NAME,
        /** 邮箱（不区分大小写精确匹配） */ EMAIL,
        /** 在邮箱、名、姓字段中进行不区分大小写的子串匹配 */ SEARCH
    }

    /** 组织邀请状态。 */
    enum InvitationStatus {
        /** 待处理 */ PENDING,
        /** 已过期 */ EXPIRED
    }

    /**
     * 返回邀请唯一标识符。
     * Returns the unique identifier of this invitation.
     *
     * @return the unique identifier
     */
    String getId();

    /**
     * 返回邀请所属组织 ID。
     * Returns the organization ID this invitation belongs to.
     *
     * @return the organization ID
     */
    String getOrganizationId();

    /**
     * 返回被邀请用户的邮箱地址。
     * Returns the email address of the invited user.
     *
     * @return the email address
     */
    String getEmail();

    /**
     * 设置被邀请用户的邮箱地址。
     * Sets the email address of the invited user.
     *
     * @param email the email address
     */
    void setEmail(String email);

    /**
     * 返回被邀请用户的名。
     * Returns the first name of the invited user.
     *
     * @return the first name
     */
    String getFirstName();

    /**
     * 设置被邀请用户的名。
     * Sets the first name of the invited user.
     *
     * @param firstName the first name
     */
    void setFirstName(String firstName);

    /**
     * 返回被邀请用户的姓。
     * Returns the last name of the invited user.
     *
     * @return the last name
     */
    String getLastName();

    /**
     * 设置被邀请用户的姓。
     * Sets the last name of the invited user.
     *
     * @param lastName the last name
     */
    void setLastName(String lastName);

    /**
     * 返回邀请创建时间戳。
     * Returns the timestamp when this invitation was created.
     *
     * @return the creation timestamp
     */
    int getCreatedAt();

    /**
     * 返回邀请过期时间戳。
     * Returns the timestamp when this invitation expires.
     *
     * @return the expiration timestamp, or null if no expiration
     */
    int getExpiresAt();

    /**
     * 设置邀请过期时间戳。
     * Sets the timestamp when this invitation expires.
     *
     * @param expiresAt the expiration timestamp
     */
    void setExpiresAt(int expiresAt);

    /**
     * 返回邀请链接。
     * Returns the invitation link.
     *
     * @return the invitation link
     */
    String getInviteLink();

    /**
     * 设置邀请链接。
     * Sets the invitation link.
     *
     * @param inviteLink the invitation link
     */
    void setInviteLink(String inviteLink);

    /**
     * 返回邀请当前状态。
     * Returns the current status of this invitation.
     *
     * @return the invitation status
     */
    InvitationStatus getStatus();

    /**
     * 判断邀请是否已过期。
     * Returns whether this invitation has expired.
     *
     * @return true if expired, false otherwise
     */
    default boolean isExpired() {
        return Time.currentTime() > getExpiresAt();
    }
}
