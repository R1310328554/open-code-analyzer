package com.taobao.arthas.core.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记嵌套配置字段。
 * <p>
 * 标注在字段上时，{@link BinderUtils} 会递归实例化并以字段名为子前缀注入子对象属性。
 *
 * @author hengyunabc 2019-08-05
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NestedConfig {

}