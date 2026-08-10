package org.keycloak.admin.api;

import java.util.Objects;

/**
 * 单个排序条件：字段与升序/降序方向的组合。
 * <p>
 * 可序列化为 sort 查询参数片段（如 {@code displayName|desc}）。
 */
public final class SortOption {

    /** 排序字段。 */
    private final ClientField field;
    /** 排序方向。 */
    private final SortOrder order;

    private SortOption(ClientField field, SortOrder order) {
        this.field = Objects.requireNonNull(field, "field cannot be null");
        this.order = order == null ? SortOrder.ASC : order;
    }

    /** 使用默认升序创建排序选项。 */
    public static SortOption of(ClientField field) {
        return new SortOption(field, SortOrder.ASC);
    }

    /** 指定字段与排序方向创建排序选项。 */
    public static SortOption of(ClientField field, SortOrder order) {
        return new SortOption(field, order);
    }

    public ClientField field() {
        return field;
    }

    public SortOrder order() {
        return order;
    }

    /** 是否为升序。 */
    public boolean isAscending() {
        return order.isAscending();
    }

    /** 转换为 sort 查询参数中的单个片段。 */
    public String toQuerySegment() {
        if (order == SortOrder.ASC) {
            return field.toQueryValue();
        }
        return field.toQueryValue() + "|" + order.name().toLowerCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SortOption)) {
            return false;
        }
        SortOption other = (SortOption) obj;
        return field == other.field && order == other.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, order);
    }
}
