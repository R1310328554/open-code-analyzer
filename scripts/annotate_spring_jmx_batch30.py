#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 JMX batch [30:40]."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path("/workspace/springframework/7.0.8/analyzed/spring-context/src/main/java/org/springframework/jmx/export")

FILES: dict[str, str] = {}


def register(rel: str, content: str) -> None:
    FILES[rel] = content


register(
    "annotation/ManagedResource.java",
    """\
/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jmx.export.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 类级别注解，指示将某类的实例注册到 JMX 服务器，
 * 对应 {@link org.springframework.jmx.export.metadata.ManagedResource} 元数据属性。
 *
 * <p><b>注意：</b>该注解标记为 {@code @Inherited}，便于编写可管理的通用基类。
 * 在此场景下，建议<i>不要</i>指定 objectName 值，否则多个子类同时注册时可能发生命名冲突。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.2
 * @see org.springframework.jmx.export.metadata.ManagedResource
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ManagedResource {

\t/**
\t * {@link #objectName} 属性的别名，便于简写默认用法。
\t */
\t@AliasFor("objectName")
\tString value() default "";

\t@AliasFor("value")
\tString objectName() default "";

\t/** MBean 描述信息。 */
\tString description() default "";

\t/** 缓存/刷新时间限制（秒），{@code -1} 表示未指定。 */
\tint currencyTimeLimit() default -1;

\t/** 是否记录 MBean 调用日志。 */
\tboolean log() default false;

\t/** 日志文件路径。 */
\tString logFile() default "";

\t/** 持久化策略。 */
\tString persistPolicy() default "";

\t/** 持久化周期。 */
\tint persistPeriod() default -1;

\t/** 持久化名称。 */
\tString persistName() default "";

\t/** 持久化位置。 */
\tString persistLocation() default "";

}
""",
)

register(
    "assembler/AbstractConfigurableMBeanInfoAssembler.java",
    """\
/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jmx.export.assembler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.modelmbean.ModelMBeanNotificationInfo;

import org.jspecify.annotations.Nullable;

import org.springframework.jmx.export.metadata.JmxMetadataUtils;
import org.springframework.jmx.export.metadata.ManagedNotification;
import org.springframework.util.StringUtils;

/**
 * 支持可配置 JMX 通知行为的 {@code MBeanInfoAssembler} 基类。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractConfigurableMBeanInfoAssembler extends AbstractReflectiveMBeanInfoAssembler {

\tprivate ModelMBeanNotificationInfo @Nullable [] notificationInfos;

\tprivate final Map<String, ModelMBeanNotificationInfo[]> notificationInfoMappings = new HashMap<>();


\tpublic void setNotificationInfos(ManagedNotification[] notificationInfos) {
\t\tModelMBeanNotificationInfo[] infos = new ModelMBeanNotificationInfo[notificationInfos.length];
\t\tfor (int i = 0; i < notificationInfos.length; i++) {
\t\t\tManagedNotification notificationInfo = notificationInfos[i];
\t\t\tinfos[i] = JmxMetadataUtils.convertToModelMBeanNotificationInfo(notificationInfo);
\t\t}
\t\tthis.notificationInfos = infos;
\t}

\tpublic void setNotificationInfoMappings(Map<String, Object> notificationInfoMappings) {
\t\tnotificationInfoMappings.forEach((beanKey, result) ->
\t\t\t\tthis.notificationInfoMappings.put(beanKey, extractNotificationMetadata(result)));
\t}


\t@Override
\tprotected ModelMBeanNotificationInfo[] getNotificationInfo(Object managedBean, String beanKey) {
\t\tModelMBeanNotificationInfo[] result = null;
\t\tif (StringUtils.hasText(beanKey)) {
\t\t\tresult = this.notificationInfoMappings.get(beanKey);
\t\t}
\t\tif (result == null) {
\t\t\tresult = this.notificationInfos;
\t\t}
\t\treturn (result != null ? result : new ModelMBeanNotificationInfo[0]);
\t}

\tprivate ModelMBeanNotificationInfo[] extractNotificationMetadata(Object mapValue) {
\t\tif (mapValue instanceof ManagedNotification mn) {
\t\t\treturn new ModelMBeanNotificationInfo[] {JmxMetadataUtils.convertToModelMBeanNotificationInfo(mn)};
\t\t}
\t\telse if (mapValue instanceof Collection<?> col) {
\t\t\tList<ModelMBeanNotificationInfo> result = new ArrayList<>();
\t\t\tfor (Object colValue : col) {
\t\t\t\tif (!(colValue instanceof ManagedNotification mn)) {
\t\t\t\t\tthrow new IllegalArgumentException(
\t\t\t\t\t\t\t"Property 'notificationInfoMappings' only accepts ManagedNotifications for Map values");
\t\t\t\t}
\t\t\t\tresult.add(JmxMetadataUtils.convertToModelMBeanNotificationInfo(mn));
\t\t\t}
\t\t\treturn result.toArray(new ModelMBeanNotificationInfo[0]);
\t\t}
\t\telse {
\t\t\tthrow new IllegalArgumentException(
\t\t\t\t\t"Property 'notificationInfoMappings' only accepts ManagedNotifications for Map values");
\t\t}
\t}

}
""",
)

register(
    "assembler/AbstractMBeanInfoAssembler.java",
    """\
/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jmx.export.assembler;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.aop.support.AopUtils;
import org.springframework.jmx.support.JmxUtils;

/**
 * {@code MBeanInfoAssembler} 接口的抽象实现，封装 {@code ModelMBeanInfo} 实例的创建，
 * 并将元数据生成委托给子类。
 *
 * <p>本类提供两种从受管 Bean 实例提取 Class 的方式：{@link #getTargetClass} 提取
 * 任意 AOP 代理背后的目标类；{@link #getClassToExpose} 返回将被搜索注解并暴露给
 * JMX 运行时的类或接口。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 */
public abstract class AbstractMBeanInfoAssembler implements MBeanInfoAssembler {

\t/**
\t * 创建 {@code ModelMBeanInfoSupport} 实例，填充全部 JMX 实现，
\t * 并通过调用子类方法填充元数据。
\t * @param managedBean 待暴露的 Bean（可能是 AOP 代理）
\t * @param beanKey 与该受管 Bean 关联的键
\t * @return 已填充的 ModelMBeanInfo 实例
\t * @throws JMException 发生错误时
\t * @see #getDescription(Object, String)
\t * @see #getAttributeInfo(Object, String)
\t * @see #getConstructorInfo(Object, String)
\t * @see #getOperationInfo(Object, String)
\t * @see #getNotificationInfo(Object, String)
\t * @see #populateMBeanDescriptor(javax.management.Descriptor, Object, String)
\t */
\t@Override
\tpublic ModelMBeanInfo getMBeanInfo(Object managedBean, String beanKey) throws JMException {
\t\tcheckManagedBean(managedBean);
\t\tModelMBeanInfo info = new ModelMBeanInfoSupport(
\t\t\t\tgetClassName(managedBean, beanKey), getDescription(managedBean, beanKey),
\t\t\t\tgetAttributeInfo(managedBean, beanKey), getConstructorInfo(managedBean, beanKey),
\t\t\t\tgetOperationInfo(managedBean, beanKey), getNotificationInfo(managedBean, beanKey));
\t\tDescriptor desc = info.getMBeanDescriptor();
\t\tpopulateMBeanDescriptor(desc, managedBean, beanKey);
\t\tinfo.setMBeanDescriptor(desc);
\t\treturn info;
\t}

\t/**
\t * 校验给定 Bean 实例，若其不符合本组装器的暴露条件则抛出 {@code IllegalArgumentException}。
\t * <p>默认实现为空，接受任意 Bean 实例。
\t * @param managedBean 待暴露的 Bean（可能是 AOP 代理）
\t * @throws IllegalArgumentException Bean 不适合暴露
\t */
\tprotected void checkManagedBean(Object managedBean) throws IllegalArgumentException {
\t}

\t/**
\t * 返回给定 Bean 实例的实际类。
\t * 该类用于描述型 JMX 属性。
\t * <p>默认实现对 AOP 代理返回目标类，否则返回普通 Bean 类。
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @return 要暴露的 Bean 类
\t * @see org.springframework.aop.support.AopUtils#getTargetClass(Object)
\t */
\tprotected Class<?> getTargetClass(Object managedBean) {
\t\treturn AopUtils.getTargetClass(managedBean);
\t}

\t/**
\t * 返回给定 Bean 要暴露的类或接口。
\t * 该类将用于搜索属性与操作（例如检查注解）。
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @return 要暴露的 Bean 类
\t * @see JmxUtils#getClassToExpose(Object)
\t */
\tprotected Class<?> getClassToExpose(Object managedBean) {
\t\treturn JmxUtils.getClassToExpose(managedBean);
\t}

\t/**
\t * 返回给定 Bean 类要暴露的类或接口。
\t * 该类将用于搜索属性与操作。
\t * @param beanClass Bean 类（可能是 AOP 代理类）
\t * @return 要暴露的 Bean 类
\t * @see JmxUtils#getClassToExpose(Class)
\t */
\tprotected Class<?> getClassToExpose(Class<?> beanClass) {
\t\treturn JmxUtils.getClassToExpose(beanClass);
\t}

\t/**
\t * 获取 MBean 资源的类名。
\t * <p>默认实现基于类名返回 MBean 的简单描述。
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @param beanKey 该 MBean 在 {@code MBeanExporter} 的 beans 映射中关联的键
\t * @return MBean 描述
\t * @throws JMException 发生错误时
\t */
\tprotected String getClassName(Object managedBean, String beanKey) throws JMException {
\t\treturn getTargetClass(managedBean).getName();
\t}

\t/**
\t * 获取 MBean 资源的描述。
\t * <p>默认实现基于类名返回 MBean 的简单描述。
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @param beanKey 该 MBean 在 {@code MBeanExporter} 的 beans 映射中关联的键
\t * @throws JMException 发生错误时
\t */
\tprotected String getDescription(Object managedBean, String beanKey) throws JMException {
\t\tString targetClassName = getTargetClass(managedBean).getName();
\t\tif (AopUtils.isAopProxy(managedBean)) {
\t\t\treturn "Proxy for " + targetClassName;
\t\t}
\t\treturn targetClassName;
\t}

\t/**
\t * 在 {@code ModelMBeanInfo} 实例构造完成后、传递给 {@code MBeanExporter} 之前调用。
\t * <p>子类可实现此方法，向 MBean 元数据添加额外描述符。默认实现为空。
\t * @param descriptor MBean 资源的 {@code Descriptor}
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @param beanKey 该 MBean 在 {@code MBeanExporter} 的 beans 映射中关联的键
\t * @throws JMException 发生错误时
\t */
\tprotected void populateMBeanDescriptor(Descriptor descriptor, Object managedBean, String beanKey)
\t\t\tthrows JMException {
\t}

\t/**
\t * 获取 MBean 资源的构造器元数据。子类应实现此方法，返回管理接口中应暴露的全部构造器元数据。
\t * <p>默认实现返回空的 {@code ModelMBeanConstructorInfo} 数组。
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @param beanKey 该 MBean 在 {@code MBeanExporter} 的 beans 映射中关联的键
\t * @return 构造器元数据
\t * @throws JMException 发生错误时
\t */
\tprotected ModelMBeanConstructorInfo[] getConstructorInfo(Object managedBean, String beanKey)
\t\t\tthrows JMException {
\t\treturn new ModelMBeanConstructorInfo[0];
\t}

\t/**
\t * 获取 MBean 资源的通知元数据。子类应实现此方法，返回管理接口中应暴露的全部通知元数据。
\t * <p>默认实现返回空的 {@code ModelMBeanNotificationInfo} 数组。
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @param beanKey 该 MBean 在 {@code MBeanExporter} 的 beans 映射中关联的键
\t * @return 通知元数据
\t * @throws JMException 发生错误时
\t */
\tprotected ModelMBeanNotificationInfo[] getNotificationInfo(Object managedBean, String beanKey)
\t\t\tthrows JMException {
\t\treturn new ModelMBeanNotificationInfo[0];
\t}


\t/**
\t * 获取 MBean 资源的属性元数据。子类应实现此方法，返回管理接口中应暴露的全部属性元数据。
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @param beanKey 该 MBean 在 {@code MBeanExporter} 的 beans 映射中关联的键
\t * @return 属性元数据
\t * @throws JMException 发生错误时
\t */
\tprotected abstract ModelMBeanAttributeInfo[] getAttributeInfo(Object managedBean, String beanKey)
\t\t\tthrows JMException;

\t/**
\t * 获取 MBean 资源的操作元数据。子类应实现此方法，返回管理接口中应暴露的全部操作元数据。
\t * @param managedBean Bean 实例（可能是 AOP 代理）
\t * @param beanKey 该 MBean 在 {@code MBeanExporter} 的 beans 映射中关联的键
\t * @return 操作元数据
\t * @throws JMException 发生错误时
\t */
\tprotected abstract ModelMBeanOperationInfo[] getOperationInfo(Object managedBean, String beanKey)
\t\t\tthrows JMException;

}
""",
)

register(
    "assembler/MBeanInfoAssembler.java",
    """\
/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jmx.export.assembler;

import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * 所有可为受管资源创建管理接口元数据的类均需实现的接口。
 *
 * <p>供 {@code MBeanExporter} 为任意非 MBean 类型的 Bean 生成管理接口。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.jmx.export.MBeanExporter
 */
public interface MBeanInfoAssembler {

\t/**
\t * 为给定受管资源创建 {@code ModelMBeanInfo}。
\t * @param managedBean 待暴露的 Bean（可能是 AOP 代理）
\t * @param beanKey 与该受管 Bean 关联的键
\t * @return ModelMBeanInfo 元数据对象
\t * @throws JMException 发生错误时
\t */
\tModelMBeanInfo getMBeanInfo(Object managedBean, String beanKey) throws JMException;

}
""",
)

register(
    "assembler/AutodetectCapableMBeanInfoAssembler.java",
    """\
/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jmx.export.assembler;

/**
 * 扩展 {@code MBeanInfoAssembler}，增加自动检测逻辑。
 * 该接口的实现类可由 {@code MBeanExporter} 调用，以在注册过程中纳入额外的 Bean。
 *
 * <p>具体决定纳入哪些 Bean 的机制由实现类自行定义。
 *
 * @author Rob Harrop
 * @since 1.2
 * @see org.springframework.jmx.export.MBeanExporter
 */
public interface AutodetectCapableMBeanInfoAssembler extends MBeanInfoAssembler {

\t/**
\t * 判断某个 Bean 是否应纳入注册流程（当其未在 {@code MBeanExporter} 的
\t * {@code beans} 映射中显式指定时）。
\t * @param beanClass Bean 的类（可能是代理类）
\t * @param beanName Bean 在 BeanFactory 中的名称
\t * @return 是否纳入自动注册
\t */
\tboolean includeBean(Class<?> beanClass, String beanName);

}
""",
)

def main() -> int:
    ok = 0
    for rel, content in FILES.items():
        text = content.replace("\\t", "\t")
        p = ROOT / rel
        p.write_text(text, encoding="utf-8")
        cn = len(re.findall(r"[\u4e00-\u9fff]", text))
        lic = "Licensed under the Apache License" in text
        if cn < 10 or not lic:
            print(f"FAIL cn={cn} lic={lic} {rel}", file=sys.stderr)
            return 1
        print(f"OK cn={cn} {rel}")
        ok += 1
    print(f"TOTAL={ok}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
