package com.taobao.arthas.core.env.convert;

/**
 * 源类型到目标类型的不可变配对，用作 {@link DefaultConversionService} 转换器映射的键。
 */
public final class ConvertiblePair {

    /** 源类型 */
    private final Class<?> sourceType;

    /** 目标类型 */
    private final Class<?> targetType;

    /**
     * 创建新的源-目标类型配对。
     * 
     * @param sourceType 源类型
     * @param targetType 目标类型
     */
    public ConvertiblePair(Class<?> sourceType, Class<?> targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /** 返回源类型 */
    public Class<?> getSourceType() {
        return this.sourceType;
    }

    /** 返回目标类型 */
    public Class<?> getTargetType() {
        return this.targetType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != ConvertiblePair.class) {
            return false;
        }
        ConvertiblePair other = (ConvertiblePair) obj;
        return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);
    }

    @Override
    public int hashCode() {
        return this.sourceType.hashCode() * 31 + this.targetType.hashCode();
    }

    /** 返回 {@code 源类名 -> 目标类名} 的可读形式 */
    @Override
    public String toString() {
        return this.sourceType.getName() + " -> " + this.targetType.getName();
    }
}
