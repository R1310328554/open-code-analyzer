package org.keycloak.admin.api;

/**
 * 列表查询的排序方向。
 */
public enum SortOrder {
    /** 升序。 */
    ASC,
    /** 降序。 */
    DESC;

    /** 是否为升序。 */
    public boolean isAscending() {
        return this == ASC;
    }
}
