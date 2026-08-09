#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 JMX wave-10 batch [0:20]."""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springframework/7.0.8"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][:20]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractJmxAttribute.java": [
        (
            "/**\n * Base class for all JMX metadata classes.\n *\n * @author Rob Harrop\n * @since 1.2\n */",
            "/**\n * 所有 JMX 元数据类的基类。\n *\n * @author Rob Harrop\n * @since 1.2\n */",
        ),
        (
            "\t/**\n\t * Set a description for this attribute.\n\t */",
            "\t/**\n\t * 设置该属性的描述信息。\n\t */",
        ),
        (
            "\t/**\n\t * Return a description for this attribute.\n\t */",
            "\t/**\n\t * 返回该属性的描述信息。\n\t */",
        ),
        (
            "\t/**\n\t * Set a currency time limit for this attribute.\n\t */",
            "\t/**\n\t * 设置该属性的缓存/刷新时间限制。\n\t */",
        ),
        (
            "\t/**\n\t * Return a currency time limit for this attribute.\n\t */",
            "\t/**\n\t * 返回该属性的缓存/刷新时间限制。\n\t */",
        ),
    ],
    "InvalidMetadataException.java": [
        (
            "/**\n * Thrown by the {@code JmxAttributeSource} when it encounters\n * incorrect metadata on a managed resource or one of its methods.\n *\n * @author Rob Harrop\n * @since 1.2\n * @see JmxAttributeSource\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n */",
            "/**\n * 当 {@code JmxAttributeSource} 在受管资源或其方法上遇到不正确元数据时抛出。\n *\n * @author Rob Harrop\n * @since 1.2\n * @see JmxAttributeSource\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code InvalidMetadataException} with the supplied\n\t * error message.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * 使用给定错误消息创建新的 {@code InvalidMetadataException}。\n\t * @param msg 详细消息\n\t */",
        ),
    ],
    "JmxAttributeSource.java": [
        (
            "/**\n * Interface used by the {@code MetadataMBeanInfoAssembler} to\n * read source-level metadata from a managed resource's class.\n *\n * @author Rob Harrop\n * @author Jennifer Hickey\n * @since 1.2\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler#setAttributeSource\n * @see org.springframework.jmx.export.MBeanExporter#setAssembler\n */",
            "/**\n * 供 {@code MetadataMBeanInfoAssembler} 从受管资源类读取源码级元数据的接口。\n *\n * @author Rob Harrop\n * @author Jennifer Hickey\n * @since 1.2\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler#setAttributeSource\n * @see org.springframework.jmx.export.MBeanExporter#setAssembler\n */",
        ),
        (
            "\t/**\n\t * Implementations should return an instance of {@link ManagedResource}\n\t * if the supplied {@code Class} has the corresponding metadata.\n\t * @param clazz the class to read the resource data from\n\t * @return the resource, or {@code null} if not found\n\t * @throws InvalidMetadataException in case of invalid metadata\n\t */",
            "\t/**\n\t * 若给定 {@code Class} 具有相应元数据，实现应返回 {@link ManagedResource} 实例。\n\t * @param clazz 读取资源数据的类\n\t * @return 资源元数据，未找到时返回 {@code null}\n\t * @throws InvalidMetadataException 元数据无效时\n\t */",
        ),
        (
            "\t/**\n\t * Implementations should return an instance of {@link ManagedAttribute}\n\t * if the supplied {@code Method} has the corresponding metadata.\n\t * @param method the method to read the attribute data from\n\t * @return the attribute, or {@code null} if not found\n\t * @throws InvalidMetadataException in case of invalid metadata\n\t */",
            "\t/**\n\t * 若给定 {@code Method} 具有相应元数据，实现应返回 {@link ManagedAttribute} 实例。\n\t * @param method 读取属性数据的方法\n\t * @return 属性元数据，未找到时返回 {@code null}\n\t * @throws InvalidMetadataException 元数据无效时\n\t */",
        ),
        (
            "\t/**\n\t * Implementations should return an instance of {@link ManagedMetric}\n\t * if the supplied {@code Method} has the corresponding metadata.\n\t * @param method the method to read the metric data from\n\t * @return the metric, or {@code null} if not found\n\t * @throws InvalidMetadataException in case of invalid metadata\n\t */",
            "\t/**\n\t * 若给定 {@code Method} 具有相应元数据，实现应返回 {@link ManagedMetric} 实例。\n\t * @param method 读取指标数据的方法\n\t * @return 指标元数据，未找到时返回 {@code null}\n\t * @throws InvalidMetadataException 元数据无效时\n\t */",
        ),
        (
            "\t/**\n\t * Implementations should return an instance of {@link ManagedOperation}\n\t * if the supplied {@code Method} has the corresponding metadata.\n\t * @param method the method to read the operation data from\n\t * @return the operation, or {@code null} if not found\n\t * @throws InvalidMetadataException in case of invalid metadata\n\t */",
            "\t/**\n\t * 若给定 {@code Method} 具有相应元数据，实现应返回 {@link ManagedOperation} 实例。\n\t * @param method 读取操作数据的方法\n\t * @return 操作元数据，未找到时返回 {@code null}\n\t * @throws InvalidMetadataException 元数据无效时\n\t */",
        ),
        (
            "\t/**\n\t * Implementations should return an array of {@link ManagedOperationParameter\n\t * ManagedOperationParameters} if the supplied {@code Method} has the corresponding\n\t * metadata.\n\t * @param method the {@code Method} to read the metadata from\n\t * @return the parameter information, or an empty array if no metadata is found\n\t * @throws InvalidMetadataException in case of invalid metadata\n\t */",
            "\t/**\n\t * 若给定 {@code Method} 具有相应元数据，实现应返回 {@link ManagedOperationParameter\n\t * ManagedOperationParameters} 数组。\n\t * @param method 读取元数据的 {@code Method}\n\t * @return 参数信息，未找到元数据时返回空数组\n\t * @throws InvalidMetadataException 元数据无效时\n\t */",
        ),
        (
            "\t/**\n\t * Implementations should return an array of {@link ManagedNotification ManagedNotifications}\n\t * if the supplied {@code Class} has the corresponding metadata.\n\t * @param clazz the {@code Class} to read the metadata from\n\t * @return the notification information, or an empty array if no metadata is found\n\t * @throws InvalidMetadataException in case of invalid metadata\n\t */",
            "\t/**\n\t * 若给定 {@code Class} 具有相应元数据，实现应返回 {@link ManagedNotification ManagedNotifications} 数组。\n\t * @param clazz 读取元数据的 {@code Class}\n\t * @return 通知信息，未找到元数据时返回空数组\n\t * @throws InvalidMetadataException 元数据无效时\n\t */",
        ),
    ],
    "JmxMetadataUtils.java": [
        (
            "/**\n * Utility methods for converting Spring JMX metadata into their plain JMX equivalents.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 将 Spring JMX 元数据转换为标准 JMX 等效形式的工具方法。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Convert the supplied {@link ManagedNotification} into the corresponding\n\t * {@link javax.management.modelmbean.ModelMBeanNotificationInfo}.\n\t */",
            "\t/**\n\t * 将给定 {@link ManagedNotification} 转换为对应的\n\t * {@link javax.management.modelmbean.ModelMBeanNotificationInfo}。\n\t */",
        ),
    ],
    "ManagedAttribute.java": [
        (
            "/**\n * Metadata that indicates to expose a given bean property as JMX attribute.\n * Only valid when used on a JavaBean getter or setter.\n *\n * @author Rob Harrop\n * @since 1.2\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n * @see org.springframework.jmx.export.MBeanExporter\n */",
            "/**\n * 指示将给定 Bean 属性暴露为 JMX 属性的元数据。\n * 仅当用于 JavaBean getter 或 setter 时有效。\n *\n * @author Rob Harrop\n * @since 1.2\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n * @see org.springframework.jmx.export.MBeanExporter\n */",
        ),
        (
            "\t/**\n\t * Empty attributes.\n\t */",
            "\t/**\n\t * 空属性占位实例。\n\t */",
        ),
        (
            "\t/**\n\t * Set the default value of this attribute.\n\t */",
            "\t/**\n\t * 设置该属性的默认值。\n\t */",
        ),
        (
            "\t/**\n\t * Return the default value of this attribute.\n\t */",
            "\t/**\n\t * 返回该属性的默认值。\n\t */",
        ),
    ],
    "ManagedMetric.java": [
        (
            "/**\n * Metadata that indicates to expose a given bean property as a JMX attribute,\n * with additional descriptor properties that indicate that the attribute is a\n * metric. Only valid when used on a JavaBean getter.\n *\n * @author Jennifer Hickey\n * @since 3.0\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n */",
            "/**\n * 指示将给定 Bean 属性暴露为 JMX 属性的元数据，\n * 并附带描述符属性表明该属性为指标（metric）。\n * 仅当用于 JavaBean getter 时有效。\n *\n * @author Jennifer Hickey\n * @since 3.0\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n */",
        ),
        (
            "\t/**\n\t * The category of this metric (ex. throughput, performance, utilization).\n\t */",
            "\t/**\n\t * 该指标的类别（例如吞吐量、性能、利用率）。\n\t */",
        ),
        (
            "\t/**\n\t * A display name for this metric.\n\t */",
            "\t/**\n\t * 该指标的显示名称。\n\t */",
        ),
        (
            "\t/**\n\t * A description of how this metric's values change over time.\n\t */",
            "\t/**\n\t * 描述该指标值随时间变化的特性。\n\t */",
        ),
        (
            "\t/**\n\t * The persist period for this metric.\n\t */",
            "\t/**\n\t * 该指标的持久化周期。\n\t */",
        ),
        (
            "\t/**\n\t * The persist policy for this metric.\n\t */",
            "\t/**\n\t * 该指标的持久化策略。\n\t */",
        ),
        (
            "\t/**\n\t * The expected unit of measurement values.\n\t */",
            "\t/**\n\t * 测量值的预期单位。\n\t */",
        ),
    ],
    "ManagedNotification.java": [
        (
            "/**\n * Metadata that indicates a JMX notification emitted by a bean.\n *\n * @author Rob Harrop\n * @since 2.0\n */",
            "/**\n * 指示 Bean 发出的 JMX 通知的元数据。\n *\n * @author Rob Harrop\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Set a single notification type, or a list of notification types\n\t * as comma-delimited String.\n\t */",
            "\t/**\n\t * 设置单个通知类型，或以逗号分隔字符串形式设置多个通知类型。\n\t */",
        ),
        (
            "\t/**\n\t * Set a list of notification types.\n\t */",
            "\t/**\n\t * 设置通知类型列表。\n\t */",
        ),
        (
            "\t/**\n\t * Return the list of notification types.\n\t */",
            "\t/**\n\t * 返回通知类型列表。\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of this notification.\n\t */",
            "\t/**\n\t * 设置该通知的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of this notification.\n\t */",
            "\t/**\n\t * 返回该通知的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Set a description for this notification.\n\t */",
            "\t/**\n\t * 设置该通知的描述信息。\n\t */",
        ),
        (
            "\t/**\n\t * Return a description for this notification.\n\t */",
            "\t/**\n\t * 返回该通知的描述信息。\n\t */",
        ),
    ],
    "ManagedOperation.java": [
        (
            "/**\n * Metadata that indicates to expose a given method as JMX operation.\n * Only valid when used on a method that is not a JavaBean getter or setter.\n *\n * @author Rob Harrop\n * @since 1.2\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n * @see org.springframework.jmx.export.MBeanExporter\n */",
            "/**\n * 指示将给定方法暴露为 JMX 操作的元数据。\n * 仅当用于非 JavaBean getter 或 setter 的方法时有效。\n *\n * @author Rob Harrop\n * @since 1.2\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n * @see org.springframework.jmx.export.MBeanExporter\n */",
        ),
    ],
    "ManagedOperationParameter.java": [
        (
            "/**\n * Metadata about JMX operation parameters.\n * Used in conjunction with a {@link ManagedOperation} attribute.\n *\n * @author Rob Harrop\n * @since 1.2\n */",
            "/**\n * 关于 JMX 操作参数的元数据。\n * 与 {@link ManagedOperation} 属性配合使用。\n *\n * @author Rob Harrop\n * @since 1.2\n */",
        ),
        (
            "\t/**\n\t * Set the index of this parameter in the operation signature.\n\t */",
            "\t/**\n\t * 设置该参数在操作签名中的索引。\n\t */",
        ),
        (
            "\t/**\n\t * Return the index of this parameter in the operation signature.\n\t */",
            "\t/**\n\t * 返回该参数在操作签名中的索引。\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of this parameter in the operation signature.\n\t */",
            "\t/**\n\t * 设置该参数在操作签名中的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of this parameter in the operation signature.\n\t */",
            "\t/**\n\t * 返回该参数在操作签名中的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Set a description for this parameter.\n\t */",
            "\t/**\n\t * 设置该参数的描述信息。\n\t */",
        ),
        (
            "\t/**\n\t * Return a description for this parameter.\n\t */",
            "\t/**\n\t * 返回该参数的描述信息。\n\t */",
        ),
    ],
    "ManagedResource.java": [
        (
            "/**\n * Metadata indicating that instances of an annotated class\n * are to be registered with a JMX server.\n * Only valid when used on a {@code Class}.\n *\n * @author Rob Harrop\n * @since 1.2\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n * @see org.springframework.jmx.export.naming.MetadataNamingStrategy\n * @see org.springframework.jmx.export.MBeanExporter\n */",
            "/**\n * 指示带注解类的实例应注册到 JMX 服务器的元数据。\n * 仅当用于 {@code Class} 时有效。\n *\n * @author Rob Harrop\n * @since 1.2\n * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler\n * @see org.springframework.jmx.export.naming.MetadataNamingStrategy\n * @see org.springframework.jmx.export.MBeanExporter\n */",
        ),
        (
            "\t/**\n\t * Set the JMX ObjectName of this managed resource.\n\t */",
            "\t/**\n\t * 设置该受管资源的 JMX ObjectName。\n\t */",
        ),
        (
            "\t/**\n\t * Return the JMX ObjectName of this managed resource.\n\t */",
            "\t/**\n\t * 返回该受管资源的 JMX ObjectName。\n\t */",
        ),
    ],
    "IdentityNamingStrategy.java": [
        (
            "/**\n * An implementation of the {@code ObjectNamingStrategy} interface that\n * creates a name based on the identity of a given instance.\n *\n * <p>The resulting {@code ObjectName} will be in the form\n * <i>package</i>:class=<i>class name</i>,hashCode=<i>identity hash (in hex)</i>\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n */",
            "/**\n * {@code ObjectNamingStrategy} 接口的实现，基于给定实例的身份标识创建名称。\n *\n * <p>生成的 {@code ObjectName} 形式为\n * <i>package</i>:class=<i>类名</i>,hashCode=<i>身份哈希（十六进制）</i>\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n */",
        ),
        (
            "\t/**\n\t * The type key.\n\t */",
            "\t/**\n\t * 类型键。\n\t */",
        ),
        (
            "\t/**\n\t * The hash code key.\n\t */",
            "\t/**\n\t * 哈希码键。\n\t */",
        ),
        (
            "\t/**\n\t * Returns an instance of {@code ObjectName} based on the identity\n\t * of the managed resource.\n\t */",
            "\t/**\n\t * 基于受管资源的身份标识返回 {@code ObjectName} 实例。\n\t */",
        ),
    ],
    "KeyNamingStrategy.java": [
        (
            "/**\n * {@code ObjectNamingStrategy} implementation that builds\n * {@code ObjectName} instances from the key used in the\n * \"beans\" map passed to {@code MBeanExporter}.\n *\n * <p>Can also check object name mappings, given as {@code Properties}\n * or as {@code mappingLocations} of properties files. The key used\n * to look up is the key used in {@code MBeanExporter}'s \"beans\" map.\n * If no mapping is found for a given key, the key itself is used to\n * build an {@code ObjectName}.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see #setMappings\n * @see #setMappingLocation\n * @see #setMappingLocations\n * @see org.springframework.jmx.export.MBeanExporter#setBeans\n */",
            "/**\n * 根据传入 {@code MBeanExporter} 的 \"beans\" 映射中的键构建\n * {@code ObjectName} 实例的 {@code ObjectNamingStrategy} 实现。\n *\n * <p>还可检查以 {@code Properties} 或 {@code mappingLocations}\n * 属性文件形式提供的 ObjectName 映射。查找时使用 {@code MBeanExporter}\n * \"beans\" 映射中的键；若未找到映射，则直接使用该键构建 {@code ObjectName}。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see #setMappings\n * @see #setMappingLocation\n * @see #setMappingLocations\n * @see org.springframework.jmx.export.MBeanExporter#setBeans\n */",
        ),
        (
            "\t/**\n\t * {@code Log} instance for this class.\n\t */",
            "\t/**\n\t * 本类的 {@code Log} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Stores the mappings of bean key to {@code ObjectName}.\n\t */",
            "\t/**\n\t * 存储 Bean 键到 {@code ObjectName} 的映射。\n\t */",
        ),
        (
            "\t/**\n\t * Stores the {@code Resource}s containing properties that should be loaded\n\t * into the final merged set of {@code Properties} used for {@code ObjectName}\n\t * resolution.\n\t */",
            "\t/**\n\t * 存储应加载到最终合并 {@code Properties} 集合中的 {@code Resource}，\n\t * 用于 {@code ObjectName} 解析。\n\t */",
        ),
        (
            "\t/**\n\t * Stores the result of merging the {@code mappings} {@code Properties}\n\t * with the properties stored in the resources defined by {@code mappingLocations}.\n\t */",
            "\t/**\n\t * 存储将 {@code mappings} {@code Properties} 与\n\t * {@code mappingLocations} 所定义资源中的属性合并后的结果。\n\t */",
        ),
        (
            "\t/**\n\t * Set local properties, containing object name mappings, for example, via\n\t * the \"props\" tag in XML bean definitions. These can be considered\n\t * defaults, to be overridden by properties loaded from files.\n\t */",
            "\t/**\n\t * 设置包含 ObjectName 映射的本地属性，例如通过 XML Bean 定义中的 \"props\" 标签。\n\t * 可视为默认值，会被从文件加载的属性覆盖。\n\t */",
        ),
        (
            "\t/**\n\t * Set a location of a properties file to be loaded,\n\t * containing object name mappings.\n\t */",
            "\t/**\n\t * 设置要加载的属性文件位置，其中包含 ObjectName 映射。\n\t */",
        ),
        (
            "\t/**\n\t * Set location of properties files to be loaded,\n\t * containing object name mappings.\n\t */",
            "\t/**\n\t * 设置要加载的属性文件位置列表，其中包含 ObjectName 映射。\n\t */",
        ),
        (
            "\t/**\n\t * Merges the {@code Properties} configured in the {@code mappings} and\n\t * {@code mappingLocations} into the final {@code Properties} instance\n\t * used for {@code ObjectName} resolution.\n\t */",
            "\t/**\n\t * 将 {@code mappings} 与 {@code mappingLocations} 中配置的\n\t * {@code Properties} 合并为用于 {@code ObjectName} 解析的最终 {@code Properties} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Attempts to retrieve the {@code ObjectName} via the given key, trying to\n\t * find a mapped value in the mappings first.\n\t */",
            "\t/**\n\t * 尝试通过给定键获取 {@code ObjectName}，优先在映射中查找对应值。\n\t */",
        ),
    ],
    "MetadataNamingStrategy.java": [
        (
            "/**\n * An implementation of the {@link ObjectNamingStrategy} interface\n * that reads the {@code ObjectName} from the source-level metadata.\n * Falls back to the bean key (bean name) if no {@code ObjectName}\n * can be found in source-level metadata.\n *\n * <p>Uses the {@link JmxAttributeSource} strategy interface, so that\n * metadata can be read using any supported implementation. Out of the box,\n * {@link org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource}\n * introspects a well-defined set of annotations that come with Spring.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see ObjectNamingStrategy\n * @see org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource\n */",
            "/**\n * 从源码级元数据读取 {@code ObjectName} 的 {@link ObjectNamingStrategy} 接口实现。\n * 若源码级元数据中找不到 {@code ObjectName}，则回退到 Bean 键（Bean 名称）。\n *\n * <p>使用 {@link JmxAttributeSource} 策略接口，可通过任意支持的实现读取元数据。\n * 开箱即用，{@link org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource}\n * 内省 Spring 自带的一组明确定义的注解。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see ObjectNamingStrategy\n * @see org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource\n */",
        ),
        (
            "\t/**\n\t * The {@code JmxAttributeSource} implementation to use for reading metadata.\n\t */",
            "\t/**\n\t * 用于读取元数据的 {@code JmxAttributeSource} 实现。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code MetadataNamingStrategy} which needs to be\n\t * configured through the {@link #setAttributeSource} method.\n\t */",
            "\t/**\n\t * 创建新的 {@code MetadataNamingStrategy}，需通过 {@link #setAttributeSource} 方法配置。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code MetadataNamingStrategy} for the given\n\t * {@code JmxAttributeSource}.\n\t * @param attributeSource the JmxAttributeSource to use\n\t */",
            "\t/**\n\t * 为给定 {@code JmxAttributeSource} 创建新的 {@code MetadataNamingStrategy}。\n\t * @param attributeSource 要使用的 JmxAttributeSource\n\t */",
        ),
        (
            "\t/**\n\t * Set the implementation of the {@code JmxAttributeSource} interface to use\n\t * when reading the source-level metadata.\n\t */",
            "\t/**\n\t * 设置读取源码级元数据时使用的 {@code JmxAttributeSource} 接口实现。\n\t */",
        ),
        (
            "\t/**\n\t * Specify the default domain to be used for generating ObjectNames\n\t * when no source-level metadata has been specified.\n\t * <p>The default is to use the domain specified in the bean name\n\t * (if the bean name follows the JMX ObjectName syntax); else,\n\t * the package name of the managed bean class.\n\t */",
            "\t/**\n\t * 指定未提供源码级元数据时生成 ObjectName 所用的默认域。\n\t * <p>默认使用 Bean 名称中指定的域（若 Bean 名称遵循 JMX ObjectName 语法）；\n\t * 否则使用受管 Bean 类的包名。\n\t */",
        ),
        (
            "\t/**\n\t * Reads the {@code ObjectName} from the source-level metadata associated\n\t * with the managed resource's {@code Class}.\n\t */",
            "\t/**\n\t * 从与受管资源 {@code Class} 关联的源码级元数据中读取 {@code ObjectName}。\n\t */",
        ),
    ],
    "ObjectNamingStrategy.java": [
        (
            "/**\n * Strategy interface that encapsulates the creation of {@code ObjectName} instances.\n *\n * <p>Used by the {@code MBeanExporter} to obtain {@code ObjectName}s\n * when registering beans.\n *\n * @author Rob Harrop\n * @since 1.2\n * @see org.springframework.jmx.export.MBeanExporter\n * @see javax.management.ObjectName\n */",
            "/**\n * 封装 {@code ObjectName} 实例创建的策略接口。\n *\n * <p>供 {@code MBeanExporter} 在注册 Bean 时获取 {@code ObjectName}。\n *\n * @author Rob Harrop\n * @since 1.2\n * @see org.springframework.jmx.export.MBeanExporter\n * @see javax.management.ObjectName\n */",
        ),
        (
            "\t/**\n\t * Obtain an {@code ObjectName} for the supplied bean.\n\t * @param managedBean the bean that will be exposed under the\n\t * returned {@code ObjectName}\n\t * @param beanKey the key associated with this bean in the beans map\n\t * passed to the {@code MBeanExporter}\n\t * @return the {@code ObjectName} instance\n\t * @throws MalformedObjectNameException if the resulting {@code ObjectName} is invalid\n\t */",
            "\t/**\n\t * 为给定 Bean 获取 {@code ObjectName}。\n\t * @param managedBean 将在返回的 {@code ObjectName} 下暴露的 Bean\n\t * @param beanKey 该 Bean 在传入 {@code MBeanExporter} 的 beans 映射中关联的键\n\t * @return {@code ObjectName} 实例\n\t * @throws MalformedObjectNameException 生成的 {@code ObjectName} 无效时\n\t */",
        ),
    ],
    "SelfNaming.java": [
        (
            "/**\n * Interface that allows infrastructure components to provide their own\n * {@code ObjectName}s to the {@code MBeanExporter}.\n *\n * <p><b>Note:</b> This interface is mainly intended for internal usage.\n *\n * @author Rob Harrop\n * @since 1.2.2\n * @see org.springframework.jmx.export.MBeanExporter\n */",
            "/**\n * 允许基础设施组件向 {@code MBeanExporter} 提供自身 {@code ObjectName} 的接口。\n *\n * <p><b>注意：</b>该接口主要用于内部用途。\n *\n * @author Rob Harrop\n * @since 1.2.2\n * @see org.springframework.jmx.export.MBeanExporter\n */",
        ),
        (
            "\t/**\n\t * Return the {@code ObjectName} for the implementing object.\n\t * @throws MalformedObjectNameException if thrown by the ObjectName constructor\n\t * @see javax.management.ObjectName#ObjectName(String)\n\t * @see javax.management.ObjectName#getInstance(String)\n\t * @see org.springframework.jmx.support.ObjectNameManager#getInstance(String)\n\t */",
            "\t/**\n\t * 返回实现对象的 {@code ObjectName}。\n\t * @throws MalformedObjectNameException ObjectName 构造器抛出时\n\t * @see javax.management.ObjectName#ObjectName(String)\n\t * @see javax.management.ObjectName#getInstance(String)\n\t * @see org.springframework.jmx.support.ObjectNameManager#getInstance(String)\n\t */",
        ),
    ],
    "ModelMBeanNotificationPublisher.java": [
        (
            "/**\n * {@link NotificationPublisher} implementation that uses the infrastructure\n * provided by the {@link ModelMBean} interface to track\n * {@link javax.management.NotificationListener javax.management.NotificationListeners}\n * and send {@link Notification Notifications} to those listeners.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Rick Evans\n * @since 2.0\n * @see javax.management.modelmbean.ModelMBeanNotificationBroadcaster\n * @see NotificationPublisherAware\n */",
            "/**\n * 使用 {@link ModelMBean} 接口提供的基础设施跟踪\n * {@link javax.management.NotificationListener javax.management.NotificationListeners}\n * 并向这些监听器发送 {@link Notification Notifications} 的 {@link NotificationPublisher} 实现。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Rick Evans\n * @since 2.0\n * @see javax.management.modelmbean.ModelMBeanNotificationBroadcaster\n * @see NotificationPublisherAware\n */",
        ),
        (
            "\t/**\n\t * The {@link ModelMBean} instance wrapping the managed resource into which this\n\t * {@code NotificationPublisher} will be injected.\n\t */",
            "\t/**\n\t * 包装受管资源的 {@link ModelMBean} 实例，本 {@code NotificationPublisher} 将注入其中。\n\t */",
        ),
        (
            "\t/**\n\t * The {@link ObjectName} associated with the {@link ModelMBean modelMBean}.\n\t */",
            "\t/**\n\t * 与 {@link ModelMBean modelMBean} 关联的 {@link ObjectName}。\n\t */",
        ),
        (
            "\t/**\n\t * The managed resource associated with the {@link ModelMBean modelMBean}.\n\t */",
            "\t/**\n\t * 与 {@link ModelMBean modelMBean} 关联的受管资源。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link ModelMBeanNotificationPublisher} class\n\t * that will publish all {@link javax.management.Notification Notifications}\n\t * to the supplied {@link ModelMBean}.\n\t * @param modelMBean the target {@link ModelMBean}; must not be {@code null}\n\t * @param objectName the {@link ObjectName} of the source {@link ModelMBean}\n\t * @param managedResource the managed resource exposed by the supplied {@link ModelMBean}\n\t * @throws IllegalArgumentException if any of the parameters is {@code null}\n\t */",
            "\t/**\n\t * 创建 {@link ModelMBeanNotificationPublisher} 新实例，\n\t * 将所有 {@link javax.management.Notification Notifications} 发布到给定 {@link ModelMBean}。\n\t * @param modelMBean 目标 {@link ModelMBean}；不得为 {@code null}\n\t * @param objectName 源 {@link ModelMBean} 的 {@link ObjectName}\n\t * @param managedResource 给定 {@link ModelMBean} 暴露的受管资源\n\t * @throws IllegalArgumentException 任一参数为 {@code null} 时\n\t */",
        ),
        (
            "\t/**\n\t * Send the supplied {@link Notification} using the wrapped\n\t * {@link ModelMBean} instance.\n\t * @param notification the {@link Notification} to be sent\n\t * @throws IllegalArgumentException if the supplied {@code notification} is {@code null}\n\t * @throws UnableToSendNotificationException if the supplied {@code notification} could not be sent\n\t */",
            "\t/**\n\t * 使用包装的 {@link ModelMBean} 实例发送给定 {@link Notification}。\n\t * @param notification 要发送的 {@link Notification}\n\t * @throws IllegalArgumentException 给定 {@code notification} 为 {@code null} 时\n\t * @throws UnableToSendNotificationException 无法发送给定 {@code notification} 时\n\t */",
        ),
        (
            "\t/**\n\t * Replaces the notification source if necessary to do so.\n\t * From the {@link Notification javadoc}:\n\t * <i>\"It is strongly recommended that notification senders use the object name\n\t * rather than a reference to the MBean object as the source.\"</i>\n\t * @param notification the {@link Notification} whose\n\t * {@link javax.management.Notification#getSource()} might need massaging\n\t */",
            "\t/**\n\t * 必要时替换通知源。\n\t * 摘自 {@link Notification javadoc}：\n\t * <i>\"强烈建议通知发送者使用 ObjectName 而非 MBean 对象引用作为源。\"</i>\n\t * @param notification 其 {@link javax.management.Notification#getSource()} 可能需要调整的 {@link Notification}\n\t */",
        ),
    ],
    "NotificationPublisher.java": [
        (
            "/**\n * Simple interface allowing Spring-managed MBeans to publish JMX notifications\n * without being aware of how those notifications are being transmitted to the\n * {@link javax.management.MBeanServer}.\n *\n * <p>Managed resources can access a {@code NotificationPublisher} by\n * implementing the {@link NotificationPublisherAware} interface. After a particular\n * managed resource instance is registered with the {@link javax.management.MBeanServer},\n * Spring will inject a {@code NotificationPublisher} instance into it if that\n * resource implements the {@link NotificationPublisherAware} interface.\n *\n * <p>Each managed resource instance will have a distinct instance of a\n * {@code NotificationPublisher} implementation. This instance will keep\n * track of all the {@link javax.management.NotificationListener NotificationListeners}\n * registered for a particular managed resource.\n *\n * <p>Any existing, user-defined MBeans should use standard JMX APIs for notification\n * publication; this interface is intended for use only by Spring-created MBeans.\n *\n * @author Rob Harrop\n * @since 2.0\n * @see NotificationPublisherAware\n * @see org.springframework.jmx.export.MBeanExporter\n */",
            "/**\n * 简单接口，使 Spring 管理的 MBean 能够发布 JMX 通知，\n * 而无需了解通知如何传输到 {@link javax.management.MBeanServer}。\n *\n * <p>受管资源可通过实现 {@link NotificationPublisherAware} 接口访问 {@code NotificationPublisher}。\n * 当特定受管资源实例注册到 {@link javax.management.MBeanServer} 后，\n * 若该资源实现了 {@link NotificationPublisherAware} 接口，Spring 将向其注入\n * {@code NotificationPublisher} 实例。\n *\n * <p>每个受管资源实例拥有独立的 {@code NotificationPublisher} 实现实例，\n * 该实例跟踪为该受管资源注册的全部\n * {@link javax.management.NotificationListener NotificationListeners}。\n *\n * <p>现有用户自定义 MBean 应使用标准 JMX API 发布通知；\n * 本接口仅供 Spring 创建的 MBean 使用。\n *\n * @author Rob Harrop\n * @since 2.0\n * @see NotificationPublisherAware\n * @see org.springframework.jmx.export.MBeanExporter\n */",
        ),
        (
            "\t/**\n\t * Send the specified {@link javax.management.Notification} to all registered\n\t * {@link javax.management.NotificationListener NotificationListeners}.\n\t * Managed resources are <strong>not</strong> responsible for managing the list\n\t * of registered {@link javax.management.NotificationListener NotificationListeners};\n\t * that is performed automatically.\n\t * @param notification the JMX Notification to send\n\t * @throws UnableToSendNotificationException if sending failed\n\t */",
            "\t/**\n\t * 将指定 {@link javax.management.Notification} 发送给所有已注册的\n\t * {@link javax.management.NotificationListener NotificationListeners}。\n\t * 受管资源<strong>不</strong>负责管理已注册\n\t * {@link javax.management.NotificationListener NotificationListeners} 的列表；\n\t * 该工作由框架自动完成。\n\t * @param notification 要发送的 JMX 通知\n\t * @throws UnableToSendNotificationException 发送失败时\n\t */",
        ),
    ],
    "NotificationPublisherAware.java": [
        (
            "/**\n * Interface to be implemented by any Spring-managed resource that is to be\n * registered with an {@link javax.management.MBeanServer} and wishes to send\n * JMX {@link javax.management.Notification javax.management.Notifications}.\n *\n * <p>Provides Spring-created managed resources with a {@link NotificationPublisher}\n * as soon as they are registered with the {@link javax.management.MBeanServer}.\n *\n * <p><b>NOTE:</b> This interface only applies to simple Spring-managed\n * beans which happen to get exported through Spring's\n * {@link org.springframework.jmx.export.MBeanExporter}.\n * It does not apply to any non-exported beans; neither does it apply\n * to standard MBeans exported by Spring. For standard JMX MBeans,\n * consider implementing the {@link javax.management.modelmbean.ModelMBeanNotificationBroadcaster}\n * interface (or implementing a full {@link javax.management.modelmbean.ModelMBean}).\n *\n * @author Rob Harrop\n * @author Chris Beams\n * @since 2.0\n * @see NotificationPublisher\n */",
            "/**\n * 任何需注册到 {@link javax.management.MBeanServer} 并希望发送\n * JMX {@link javax.management.Notification javax.management.Notifications} 的\n * Spring 管理资源应实现的接口。\n *\n * <p>在 Spring 创建的受管资源注册到 {@link javax.management.MBeanServer} 后，\n * 立即为其提供 {@link NotificationPublisher}。\n *\n * <p><b>注意：</b>该接口仅适用于通过 Spring 的\n * {@link org.springframework.jmx.export.MBeanExporter} 导出的简单 Spring 管理 Bean。\n * 不适用于未导出的 Bean，也不适用于 Spring 导出的标准 MBean。\n * 对于标准 JMX MBean，请考虑实现\n * {@link javax.management.modelmbean.ModelMBeanNotificationBroadcaster} 接口\n * （或实现完整的 {@link javax.management.modelmbean.ModelMBean}）。\n *\n * @author Rob Harrop\n * @author Chris Beams\n * @since 2.0\n * @see NotificationPublisher\n */",
        ),
        (
            "\t/**\n\t * Set the {@link NotificationPublisher} instance for the current managed resource instance.\n\t */",
            "\t/**\n\t * 为当前受管资源实例设置 {@link NotificationPublisher} 实例。\n\t */",
        ),
    ],
    "UnableToSendNotificationException.java": [
        (
            "/**\n * Thrown when a JMX {@link javax.management.Notification} is unable to be sent.\n *\n * <p>The root cause of just why a particular notification could not be sent\n * will <i>typically</i> be available via the {@link #getCause()} property.\n *\n * @author Rob Harrop\n * @since 2.0\n * @see NotificationPublisher\n */",
            "/**\n * 当 JMX {@link javax.management.Notification} 无法发送时抛出。\n *\n * <p>特定通知无法发送的根本原因<i>通常</i>可通过 {@link #getCause()} 属性获取。\n *\n * @author Rob Harrop\n * @since 2.0\n * @see NotificationPublisher\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link UnableToSendNotificationException}\n\t * class with the specified error message.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * 使用指定错误消息创建 {@link UnableToSendNotificationException} 新实例。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link UnableToSendNotificationException}\n\t * with the specified error message and root cause.\n\t * @param msg the detail message\n\t * @param cause the root cause\n\t */",
            "\t/**\n\t * 使用指定错误消息和根本原因创建 {@link UnableToSendNotificationException} 新实例。\n\t * @param msg 详细消息\n\t * @param cause 根本原因\n\t */",
        ),
    ],
    "ConnectorServerFactoryBean.java": [
        (
            "/**\n * {@link FactoryBean} that creates a JSR-160 {@link JMXConnectorServer},\n * optionally registers it with the {@link MBeanServer}, and then starts it.\n *\n * <p>The {@code JMXConnectorServer} can be started in a separate thread by setting the\n * {@code threaded} property to {@code true}. You can configure this thread to be a\n * daemon thread by setting the {@code daemon} property to {@code true}.\n *\n * <p>The {@code JMXConnectorServer} is correctly shut down when an instance of this\n * class is destroyed on shutdown of the containing {@code ApplicationContext}.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see JMXConnectorServer\n * @see MBeanServer\n */",
            "/**\n * 创建 JSR-160 {@link JMXConnectorServer} 的 {@link FactoryBean}，\n * 可选地将其注册到 {@link MBeanServer}，然后启动它。\n *\n * <p>将 {@code threaded} 属性设为 {@code true} 可在独立线程中启动 {@code JMXConnectorServer}。\n * 将 {@code daemon} 属性设为 {@code true} 可将该线程配置为守护线程。\n *\n * <p>当包含的 {@code ApplicationContext} 关闭导致本类实例销毁时，\n * {@code JMXConnectorServer} 会被正确关闭。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see JMXConnectorServer\n * @see MBeanServer\n */",
        ),
        (
            "\t/** The default service URL. */",
            "\t/** 默认服务 URL。 */",
        ),
        (
            "\t/**\n\t * Set the service URL for the {@code JMXConnectorServer}.\n\t */",
            "\t/**\n\t * 设置 {@code JMXConnectorServer} 的服务 URL。\n\t */",
        ),
        (
            "\t/**\n\t * Set the environment properties used to construct the {@code JMXConnectorServer}\n\t * as {@code java.util.Properties} (String key/value pairs).\n\t */",
            "\t/**\n\t * 以 {@code java.util.Properties}（字符串键值对）形式设置\n\t * 构造 {@code JMXConnectorServer} 所用的环境属性。\n\t */",
        ),
        (
            "\t/**\n\t * Set the environment properties used to construct the {@code JMXConnector}\n\t * as a {@code Map} of String keys and arbitrary Object values.\n\t */",
            "\t/**\n\t * 以字符串键与任意 Object 值的 {@code Map} 形式设置\n\t * 构造 {@code JMXConnector} 所用的环境属性。\n\t */",
        ),
        (
            "\t/**\n\t * Set an MBeanServerForwarder to be applied to the {@code JMXConnectorServer}.\n\t */",
            "\t/**\n\t * 设置要应用于 {@code JMXConnectorServer} 的 MBeanServerForwarder。\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@code ObjectName} used to register the {@code JMXConnectorServer}\n\t * itself with the {@code MBeanServer}, as {@code ObjectName} instance\n\t * or as {@code String}.\n\t * @throws MalformedObjectNameException if the {@code ObjectName} is malformed\n\t */",
            "\t/**\n\t * 设置用于将 {@code JMXConnectorServer} 自身注册到 {@code MBeanServer} 的\n\t * {@code ObjectName}，可为 {@code ObjectName} 实例或 {@code String}。\n\t * @throws MalformedObjectNameException {@code ObjectName} 格式错误时\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the {@code JMXConnectorServer} should be started in a separate thread.\n\t */",
            "\t/**\n\t * 设置 {@code JMXConnectorServer} 是否应在独立线程中启动。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether any threads started for the {@code JMXConnectorServer} should be\n\t * started as daemon threads.\n\t */",
            "\t/**\n\t * 设置为 {@code JMXConnectorServer} 启动的线程是否应作为守护线程启动。\n\t */",
        ),
        (
            "\t/**\n\t * Start the connector server. If the {@code threaded} flag is set to {@code true},\n\t * the {@code JMXConnectorServer} will be started in a separate thread.\n\t * If the {@code daemon} flag is set to {@code true}, that thread will be\n\t * started as a daemon thread.\n\t * @throws JMException if a problem occurred when registering the connector server\n\t * with the {@code MBeanServer}\n\t * @throws IOException if there is a problem starting the connector server\n\t */",
            "\t/**\n\t * 启动连接器服务器。若 {@code threaded} 标志为 {@code true}，\n\t * {@code JMXConnectorServer} 将在独立线程中启动。\n\t * 若 {@code daemon} 标志为 {@code true}，该线程将作为守护线程启动。\n\t * @throws JMException 向 {@code MBeanServer} 注册连接器服务器时发生问题时\n\t * @throws IOException 启动连接器服务器时发生问题时\n\t */",
        ),
        (
            "\t/**\n\t * Stop the {@code JMXConnectorServer} managed by an instance of this class.\n\t * Automatically called on {@code ApplicationContext} shutdown.\n\t * @throws IOException if there is an error stopping the connector server\n\t */",
            "\t/**\n\t * 停止本类实例管理的 {@code JMXConnectorServer}。\n\t * 在 {@code ApplicationContext} 关闭时自动调用。\n\t * @throws IOException 停止连接器服务器时发生错误\n\t */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
