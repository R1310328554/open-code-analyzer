package org.keycloak.admin.ui.rest.model;

import java.util.Map;

import org.keycloak.representations.idm.UserRepresentation;

/**
 * 扩展 {@link UserRepresentation}，附加暴力破解防护锁定状态供管理 UI 展示。
 */
public class BruteUser extends UserRepresentation {

    /** 暴力破解状态键值对，如是否锁定、剩余锁定时间等。 */
    Map<String, Object> bruteForceStatus;

    /** 从已有用户表示复制全部字段。 */
    public BruteUser(UserRepresentation user) {
        super(user);
    }

    public Map<String, Object> getBruteForceStatus() {
        return bruteForceStatus;
    }

    public void setBruteForceStatus(Map<String, Object> bruteForceStatus) {
        this.bruteForceStatus = bruteForceStatus;
    }
}
