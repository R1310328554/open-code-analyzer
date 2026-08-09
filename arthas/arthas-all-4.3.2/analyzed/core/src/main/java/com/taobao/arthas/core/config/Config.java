package com.taobao.arthas.core.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记配置类并指定属性前缀。
 * <p>
 * 配合 {@link BinderUtils} 使用：{@link #prefix()} 与 setter 字段名拼接成完整配置键。
 *
 * @author hengyunabc 2019-08-05
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {

    /** 配置键前缀，如 {@code arthas} 对应 arthas.telnetPort 等 */
    String prefix() default "";

}