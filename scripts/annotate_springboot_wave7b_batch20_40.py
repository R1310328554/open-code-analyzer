#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-7b batch files [20:40]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][20:40]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "BindMethod.java": [
        (
            "/**\n * Configuration property binding methods.\n *\n * @author Andy Wilkinson\n * @since 3.0.8\n */",
            "/**\n * 配置属性绑定方式。\n *\n * @author Andy Wilkinson\n * @since 3.0.8\n */",
        ),
        (
            "\t/**\n\t * Java Bean using getter/setter binding.\n\t */",
            "\t/**\n\t * 使用 getter/setter 绑定的 Java Bean。\n\t */",
        ),
        (
            "\t/**\n\t * Value object using constructor binding.\n\t */",
            "\t/**\n\t * 使用构造器绑定的值对象。\n\t */",
        ),
    ],
    "BindResult.java": [
        (
            "/**\n * A container object to return the result of a {@link Binder} bind operation. May contain\n * either a successfully bound object or an empty result.\n *\n * @param <T> the result type\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 用于返回 {@link Binder} 绑定操作结果的容器对象。可能包含成功绑定的对象，也可能为空结果。\n *\n * @param <T> 结果类型\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "\t/**\n\t * Return the object that was bound or throw a {@link NoSuchElementException} if no\n\t * value was bound.\n\t * @return the bound value (never {@code null})\n\t * @throws NoSuchElementException if no value was bound\n\t * @see #isBound()\n\t */",
            "\t/**\n\t * 返回已绑定的对象；若未绑定任何值则抛出 {@link NoSuchElementException}。\n\t *\n\t * @return 绑定的值（永不为 {@code null}）\n\t * @throws NoSuchElementException 未绑定任何值时抛出\n\t * @see #isBound()\n\t */",
        ),
        (
            "\t/**\n\t * Returns {@code true} if a result was bound.\n\t * @return if a result was bound\n\t */",
            "\t/**\n\t * 若已绑定结果则返回 {@code true}。\n\t *\n\t * @return 是否已绑定结果\n\t */",
        ),
        (
            "\t/**\n\t * Invoke the specified consumer with the bound value, or do nothing if no value has\n\t * been bound.\n\t * @param consumer block to execute if a value has been bound\n\t */",
            "\t/**\n\t * 使用绑定的值调用指定 consumer；若未绑定任何值则不执行任何操作。\n\t *\n\t * @param consumer 已绑定值时执行的代码块\n\t */",
        ),
        (
            "\t/**\n\t * Apply the provided mapping function to the bound value, or return an updated\n\t * unbound result if no value has been bound.\n\t * @param <U> the type of the result of the mapping function\n\t * @param mapper a mapping function to apply to the bound value. The mapper will not\n\t * be invoked if no value has been bound.\n\t * @return an {@code BindResult} describing the result of applying a mapping function\n\t * to the value of this {@code BindResult}.\n\t */",
            "\t/**\n\t * 对绑定的值应用提供的映射函数；若未绑定任何值则返回更新后的未绑定结果。\n\t *\n\t * @param <U> 映射函数的结果类型\n\t * @param mapper 应用于绑定值的映射函数；未绑定任何值时不会调用\n\t * @return 描述对此 {@code BindResult} 的值应用映射函数后的 {@code BindResult}\n\t */",
        ),
        (
            "\t/**\n\t * Return the object that was bound, or {@code other} if no value has been bound.\n\t * @param other the value to be returned if there is no bound value (may be\n\t * {@code null})\n\t * @return the value, if bound, otherwise {@code other}\n\t */",
            "\t/**\n\t * 返回已绑定的对象；若未绑定任何值则返回 {@code other}。\n\t *\n\t * @param other 无绑定值时返回的值（可为 {@code null}）\n\t * @return 已绑定则返回值，否则返回 {@code other}\n\t */",
        ),
        (
            "\t/**\n\t * Return the object that was bound, or the result of invoking {@code other} if no\n\t * value has been bound.\n\t * @param other a {@link Supplier} of the value to be returned if there is no bound\n\t * value\n\t * @return the value, if bound, otherwise the supplied {@code other}\n\t */",
            "\t/**\n\t * 返回已绑定的对象；若未绑定任何值则调用 {@code other} 并返回其结果。\n\t *\n\t * @param other 无绑定值时提供值的 {@link Supplier}\n\t * @return 已绑定则返回值，否则返回 {@code other} 提供的值\n\t */",
        ),
        (
            "\t/**\n\t * Return the object that was bound, or throw an exception to be created by the\n\t * provided supplier if no value has been bound.\n\t * @param <X> the type of the exception to be thrown\n\t * @param exceptionSupplier the supplier which will return the exception to be thrown\n\t * @return the present value\n\t * @throws X if there is no value present\n\t */",
            "\t/**\n\t * 返回已绑定的对象；若未绑定任何值则通过提供的 supplier 创建并抛出异常。\n\t *\n\t * @param <X> 要抛出的异常类型\n\t * @param exceptionSupplier 提供待抛出异常的 supplier\n\t * @return 当前值\n\t * @throws X 无值时抛出\n\t */",
        ),
    ],
    "Bindable.java": [
        (
            "/**\n * Source that can be bound by a {@link Binder}.\n *\n * @param <T> the source type\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see Bindable#of(Class)\n * @see Bindable#of(ResolvableType)\n */",
            "/**\n * 可由 {@link Binder} 绑定的源对象。\n *\n * @param <T> 源类型\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see Bindable#of(Class)\n * @see Bindable#of(ResolvableType)\n */",
        ),
        (
            "\t/**\n\t * Return the type of the item to bind.\n\t * @return the type being bound\n\t */",
            "\t/**\n\t * 返回待绑定项的类型。\n\t *\n\t * @return 正在绑定的类型\n\t */",
        ),
        (
            "\t/**\n\t * Return the boxed type of the item to bind.\n\t * @return the boxed type for the item being bound\n\t */",
            "\t/**\n\t * 返回待绑定项的装箱类型。\n\t *\n\t * @return 待绑定项的装箱类型\n\t */",
        ),
        (
            "\t/**\n\t * Return a supplier that provides the object value or {@code null}.\n\t * @return the value or {@code null}\n\t */",
            "\t/**\n\t * 返回提供对象值的 supplier，或 {@code null}。\n\t *\n\t * @return 值或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return any associated annotations that could affect binding.\n\t * @return the associated annotations\n\t */",
            "\t/**\n\t * 返回可能影响绑定的关联注解。\n\t *\n\t * @return 关联注解\n\t */",
        ),
        (
            "\t/**\n\t * Return a single associated annotations that could affect binding.\n\t * @param <A> the annotation type\n\t * @param type annotation type\n\t * @return the associated annotation or {@code null}\n\t */",
            "\t/**\n\t * 返回单个可能影响绑定的关联注解。\n\t *\n\t * @param <A> 注解类型\n\t * @param type 注解类型\n\t * @return 关联注解或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Returns {@code true} if the specified bind restriction has been added.\n\t * @param bindRestriction the bind restriction to check\n\t * @return if the bind restriction has been added\n\t * @since 2.5.0\n\t */",
            "\t/**\n\t * 若已添加指定的绑定限制则返回 {@code true}。\n\t *\n\t * @param bindRestriction 待检查的绑定限制\n\t * @return 是否已添加该绑定限制\n\t * @since 2.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Returns the {@link BindMethod method} to be used to bind this bindable, or\n\t * {@code null} if no specific binding method is required.\n\t * @return the bind method or {@code null}\n\t * @since 3.0.8\n\t */",
            "\t/**\n\t * 返回用于绑定此 bindable 的 {@link BindMethod 方法}；若无需特定绑定方式则为 {@code null}。\n\t *\n\t * @return 绑定方法或 {@code null}\n\t * @since 3.0.8\n\t */",
        ),
        (
            "\t/**\n\t * Create an updated {@link Bindable} instance with the specified annotations.\n\t * @param annotations the annotations\n\t * @return an updated {@link Bindable}\n\t */",
            "\t/**\n\t * 创建带有指定注解的更新后 {@link Bindable} 实例。\n\t *\n\t * @param annotations 注解\n\t * @return 更新后的 {@link Bindable}\n\t */",
        ),
        (
            "\t/**\n\t * Create an updated {@link Bindable} instance with an existing value. Implies that\n\t * Java Bean binding will be used.\n\t * @param existingValue the existing value\n\t * @return an updated {@link Bindable}\n\t */",
            "\t/**\n\t * 创建带有已有值的更新后 {@link Bindable} 实例。隐含将使用 Java Bean 绑定。\n\t *\n\t * @param existingValue 已有值\n\t * @return 更新后的 {@link Bindable}\n\t */",
        ),
        (
            "\t/**\n\t * Create an updated {@link Bindable} instance with a value supplier.\n\t * @param suppliedValue the supplier for the value\n\t * @return an updated {@link Bindable}\n\t */",
            "\t/**\n\t * 创建带有值 supplier 的更新后 {@link Bindable} 实例。\n\t *\n\t * @param suppliedValue 值的 supplier\n\t * @return 更新后的 {@link Bindable}\n\t */",
        ),
        (
            "\t/**\n\t * Create an updated {@link Bindable} instance with additional bind restrictions.\n\t * @param additionalRestrictions any additional restrictions to apply\n\t * @return an updated {@link Bindable}\n\t * @since 2.5.0\n\t */",
            "\t/**\n\t * 创建带有额外绑定限制的更新后 {@link Bindable} 实例。\n\t *\n\t * @param additionalRestrictions 要应用的额外限制\n\t * @return 更新后的 {@link Bindable}\n\t * @since 2.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Create an updated {@link Bindable} instance with a specific bind method. To use\n\t * {@link BindMethod#VALUE_OBJECT value object binding}, the current instance must not\n\t * have an existing or supplied value.\n\t * @param bindMethod the method to use to bind the bindable\n\t * @return an updated {@link Bindable}\n\t * @since 3.0.8\n\t */",
            "\t/**\n\t * 创建带有特定绑定方法的更新后 {@link Bindable} 实例。要使用\n\t * {@link BindMethod#VALUE_OBJECT 值对象绑定}，当前实例不得有已有值或 supplier 提供的值。\n\t *\n\t * @param bindMethod 用于绑定 bindable 的方法\n\t * @return 更新后的 {@link Bindable}\n\t * @since 3.0.8\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link Bindable} of the type of the specified instance with an\n\t * existing value equal to the instance.\n\t * @param <T> the source type\n\t * @param instance the instance (must not be {@code null})\n\t * @return a {@link Bindable} instance\n\t * @see #of(ResolvableType)\n\t * @see #withExistingValue(Object)\n\t */",
            "\t/**\n\t * 创建与指定实例类型相同、且已有值等于该实例的新 {@link Bindable}。\n\t *\n\t * @param <T> 源类型\n\t * @param instance 实例（不得为 {@code null}）\n\t * @return {@link Bindable} 实例\n\t * @see #of(ResolvableType)\n\t * @see #withExistingValue(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link Bindable} of the specified type.\n\t * @param <T> the source type\n\t * @param type the type (must not be {@code null})\n\t * @return a {@link Bindable} instance\n\t * @see #of(ResolvableType)\n\t */",
            "\t/**\n\t * 创建指定类型的新 {@link Bindable}。\n\t *\n\t * @param <T> 源类型\n\t * @param type 类型（不得为 {@code null}）\n\t * @return {@link Bindable} 实例\n\t * @see #of(ResolvableType)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link Bindable} {@link List} of the specified element type.\n\t * @param <E> the element type\n\t * @param elementType the list element type\n\t * @return a {@link Bindable} instance\n\t */",
            "\t/**\n\t * 创建指定元素类型的新 {@link Bindable} {@link List}。\n\t *\n\t * @param <E> 元素类型\n\t * @param elementType 列表元素类型\n\t * @return {@link Bindable} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link Bindable} {@link Set} of the specified element type.\n\t * @param <E> the element type\n\t * @param elementType the set element type\n\t * @return a {@link Bindable} instance\n\t */",
            "\t/**\n\t * 创建指定元素类型的新 {@link Bindable} {@link Set}。\n\t *\n\t * @param <E> 元素类型\n\t * @param elementType 集合元素类型\n\t * @return {@link Bindable} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link Bindable} {@link Map} of the specified key and value type.\n\t * @param <K> the key type\n\t * @param <V> the value type\n\t * @param keyType the map key type\n\t * @param valueType the map value type\n\t * @return a {@link Bindable} instance\n\t */",
            "\t/**\n\t * 创建指定键值类型的新 {@link Bindable} {@link Map}。\n\t *\n\t * @param <K> 键类型\n\t * @param <V> 值类型\n\t * @param keyType Map 键类型\n\t * @param valueType Map 值类型\n\t * @return {@link Bindable} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link Bindable} of the specified type.\n\t * @param <T> the source type\n\t * @param type the type (must not be {@code null})\n\t * @return a {@link Bindable} instance\n\t * @see #of(Class)\n\t */",
            "\t/**\n\t * 创建指定类型的新 {@link Bindable}。\n\t *\n\t * @param <T> 源类型\n\t * @param type 类型（不得为 {@code null}）\n\t * @return {@link Bindable} 实例\n\t * @see #of(Class)\n\t */",
        ),
        (
            "\t/**\n\t * Restrictions that can be applied when binding values.\n\t *\n\t * @since 2.5.0\n\t */",
            "\t/**\n\t * 绑定值时可应用的限制。\n\t *\n\t * @since 2.5.0\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Do not bind direct {@link ConfigurationProperty} matches.\n\t\t */",
            "\t\t/**\n\t\t * 不绑定直接的 {@link ConfigurationProperty} 匹配项。\n\t\t */",
        ),
    ],
    "BindableRuntimeHintsRegistrar.java": [
        (
            "/**\n * {@link RuntimeHintsRegistrar} that can be used to register {@link ReflectionHints} for\n * {@link Bindable} types, discovering any nested type it may expose through a property.\n * <p>\n * This class can be used as a base-class, or instantiated using the {@code forTypes} and\n * {@code forBindables} factory methods.\n *\n * @author Andy Wilkinson\n * @author Moritz Halbritter\n * @author Sebastien Deleuze\n * @author Phillip Webb\n * @since 3.0.0\n */",
            "/**\n * 可用于为 {@link Bindable} 类型注册 {@link ReflectionHints} 的 {@link RuntimeHintsRegistrar}，\n * 并发现其通过属性可能暴露的嵌套类型。\n * <p>\n * 此类可作为基类使用，或通过 {@code forTypes} 与 {@code forBindables} 工厂方法实例化。\n *\n * @author Andy Wilkinson\n * @author Moritz Halbritter\n * @author Sebastien Deleuze\n * @author Phillip Webb\n * @since 3.0.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link BindableRuntimeHintsRegistrar} for the specified types.\n\t * @param types the types to process\n\t */",
            "\t/**\n\t * 为指定类型创建新的 {@link BindableRuntimeHintsRegistrar}。\n\t *\n\t * @param types 待处理的类型\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link BindableRuntimeHintsRegistrar} for the specified bindables.\n\t * @param bindables the bindables to process\n\t * @since 3.0.8\n\t */",
            "\t/**\n\t * 为指定 bindable 创建新的 {@link BindableRuntimeHintsRegistrar}。\n\t *\n\t * @param bindables 待处理的 bindable\n\t * @since 3.0.8\n\t */",
        ),
        (
            "\t/**\n\t * Contribute hints to the given {@link RuntimeHints} instance.\n\t * @param hints the hints contributed so far for the deployment unit\n\t */",
            "\t/**\n\t * 向给定的 {@link RuntimeHints} 实例贡献提示信息。\n\t *\n\t * @param hints 部署单元目前已贡献的提示\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link BindableRuntimeHintsRegistrar} for the specified types.\n\t * @param types the types to process\n\t * @return a new {@link BindableRuntimeHintsRegistrar} instance\n\t */",
            "\t/**\n\t * 为指定类型创建新的 {@link BindableRuntimeHintsRegistrar}。\n\t *\n\t * @param types 待处理的类型\n\t * @return 新的 {@link BindableRuntimeHintsRegistrar} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link BindableRuntimeHintsRegistrar} for the specified types.\n\t * @param types the types to process\n\t * @return a new {@link BindableRuntimeHintsRegistrar} instance\n\t */",
            "\t/**\n\t * 为指定类型创建新的 {@link BindableRuntimeHintsRegistrar}。\n\t *\n\t * @param types 待处理的类型\n\t * @return 新的 {@link BindableRuntimeHintsRegistrar} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link BindableRuntimeHintsRegistrar} for the specified bindables.\n\t * @param bindables the bindables to process\n\t * @return a new {@link BindableRuntimeHintsRegistrar} instance\n\t * @since 3.0.8\n\t */",
            "\t/**\n\t * 为指定 bindable 创建新的 {@link BindableRuntimeHintsRegistrar}。\n\t *\n\t * @param bindables 待处理的 bindable\n\t * @return 新的 {@link BindableRuntimeHintsRegistrar} 实例\n\t * @since 3.0.8\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link BindableRuntimeHintsRegistrar} for the specified bindables.\n\t * @param bindables the bindables to process\n\t * @return a new {@link BindableRuntimeHintsRegistrar} instance\n\t * @since 3.0.8\n\t */",
            "\t/**\n\t * 为指定 bindable 创建新的 {@link BindableRuntimeHintsRegistrar}。\n\t *\n\t * @param bindables 待处理的 bindable\n\t * @return 新的 {@link BindableRuntimeHintsRegistrar} 实例\n\t * @since 3.0.8\n\t */",
        ),
        (
            "\t/**\n\t * Processor used to register the hints.\n\t */",
            "\t/**\n\t * 用于注册提示信息的处理器。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Specify whether the specified property refer to a nested type. A nested type\n\t\t * represents a sub-namespace that need to be fully resolved. Nested types are\n\t\t * either inner classes or annotated with {@link NestedConfigurationProperty}.\n\t\t * @param propertyName the name of the property\n\t\t * @param propertyType the type of the property\n\t\t * @return whether the specified {@code propertyType} is a nested type\n\t\t */",
            "\t\t/**\n\t\t * 指定给定属性是否指向嵌套类型。嵌套类型表示需要完全解析的子命名空间。\n\t\t * 嵌套类型可以是内部类，或标注了 {@link NestedConfigurationProperty} 的类型。\n\t\t *\n\t\t * @param propertyName 属性名\n\t\t * @param propertyType 属性类型\n\t\t * @return 指定 {@code propertyType} 是否为嵌套类型\n\t\t */",
        ),
        (
            "\t/**\n\t * Inner class to avoid a hard dependency on Kotlin at runtime.\n\t */",
            "\t/**\n\t * 内部类，避免在运行时对 Kotlin 产生硬依赖。\n\t */",
        ),
    ],
    "BoundPropertiesTrackingBindHandler.java": [
        (
            "/**\n * {@link BindHandler} that can be used to track bound configuration properties.\n *\n * @author Madhura Bhave\n * @since 2.3.0\n */",
            "/**\n * 可用于跟踪已绑定配置属性的 {@link BindHandler}。\n *\n * @author Madhura Bhave\n * @since 2.3.0\n */",
        ),
    ],
    "CollectionBinder.java": [
        (
            "/**\n * {@link AggregateBinder} for collections.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 用于集合的 {@link AggregateBinder}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "ConstructorBinding.java": [
        (
            "/**\n * Annotation that can be used to indicate which constructor to use when binding\n * configuration properties using constructor arguments rather than by calling setters. A\n * single parameterized constructor implicitly indicates that constructor binding should\n * be used unless the constructor is annotated with {@code @Autowired}.\n *\n * @author Phillip Webb\n * @since 3.0.0\n */",
            "/**\n * 用于指示绑定配置属性时使用哪个构造器的注解。通过构造器参数而非 setter 进行绑定时使用。\n * 单个带参构造器隐式表示应使用构造器绑定，除非该构造器标注了 {@code @Autowired}。\n *\n * @author Phillip Webb\n * @since 3.0.0\n */",
        ),
    ],
    "DataObjectBinder.java": [
        (
            "/**\n * Internal strategy used by {@link Binder} to bind data objects. A data object is an\n * object composed itself of recursively bound properties.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see JavaBeanBinder\n * @see ValueObjectBinder\n */",
            "/**\n * {@link Binder} 用于绑定数据对象的内部策略。数据对象本身由递归绑定的属性组成。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @see JavaBeanBinder\n * @see ValueObjectBinder\n */",
        ),
        (
            "\t/**\n\t * Return a bound instance or {@code null} if the {@link DataObjectBinder} does not\n\t * support the specified {@link Bindable}.\n\t * @param <T> the source type\n\t * @param name the name being bound\n\t * @param target the bindable to bind\n\t * @param context the bind context\n\t * @param propertyBinder property binder\n\t * @param fallbackToDefaultValue if an attempt should be made to return a new default\n\t * value when no values are bound\n\t * @return a bound instance or {@code null}\n\t */",
            "\t/**\n\t * 返回绑定后的实例；若 {@link DataObjectBinder} 不支持指定 {@link Bindable} 则返回 {@code null}。\n\t *\n\t * @param <T> 源类型\n\t * @param name 正在绑定的名称\n\t * @param target 待绑定的 bindable\n\t * @param context 绑定上下文\n\t * @param propertyBinder 属性绑定器\n\t * @param fallbackToDefaultValue 未绑定任何值时是否尝试返回新的默认值\n\t * @return 绑定后的实例或 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return a newly created instance or {@code null} if the {@link DataObjectBinder}\n\t * does not support the specified {@link Bindable}.\n\t * @param <T> the source type\n\t * @param target the bindable to create\n\t * @param context the bind context\n\t * @return the created instance\n\t */",
            "\t/**\n\t * 返回新创建的实例；若 {@link DataObjectBinder} 不支持指定 {@link Bindable} 则返回 {@code null}。\n\t *\n\t * @param <T> 源类型\n\t * @param target 待创建的 bindable\n\t * @param context 绑定上下文\n\t * @return 创建的实例\n\t */",
        ),
        (
            "\t/**\n\t * Callback that can be used to add additional suppressed exceptions when an instance\n\t * cannot be created.\n\t * @param <T> the source type\n\t * @param target the bindable that was being created\n\t * @param context the bind context\n\t * @param exception the exception about to be thrown\n\t */",
            "\t/**\n\t * 无法创建实例时，可用于添加额外被抑制异常的回调。\n\t *\n\t * @param <T> 源类型\n\t * @param target 正在创建的 bindable\n\t * @param context 绑定上下文\n\t * @param exception 即将抛出的异常\n\t */",
        ),
    ],
    "DataObjectPropertyBinder.java": [
        (
            "/**\n * Binder that can be used by {@link DataObjectBinder} implementations to bind the data\n * object properties.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * {@link DataObjectBinder} 实现可用于绑定数据对象属性的绑定器。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "\t/**\n\t * Bind the given property.\n\t * @param propertyName the property name (in lowercase dashed form, e.g.\n\t * {@code first-name})\n\t * @param target the target bindable\n\t * @return the bound value or {@code null}\n\t */",
            "\t/**\n\t * 绑定给定属性。\n\t *\n\t * @param propertyName 属性名（小写短横线形式，例如 {@code first-name}）\n\t * @param target 目标 bindable\n\t * @return 绑定的值或 {@code null}\n\t */",
        ),
    ],
    "DataObjectPropertyName.java": [
        (
            "/**\n * Internal utility to help when dealing with data object property names.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.2.3\n * @see DataObjectBinder\n */",
            "/**\n * 处理数据对象属性名时的内部工具类。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.2.3\n * @see DataObjectBinder\n */",
        ),
        (
            "\t/**\n\t * Return the specified Java Bean property name in dashed form.\n\t * @param name the source name\n\t * @return the dashed from\n\t */",
            "\t/**\n\t * 将指定的 Java Bean 属性名转换为短横线形式。\n\t *\n\t * @param name 源名称\n\t * @return 短横线形式\n\t */",
        ),
    ],
    "DefaultBindConstructorProvider.java": [
        (
            "/**\n * Default {@link BindConstructorProvider} implementation.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n */",
            "/**\n * 默认的 {@link BindConstructorProvider} 实现。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n */",
        ),
        (
            "\t/**\n\t * Data holder for autowired and bind constructors.\n\t */",
            "\t/**\n\t * 用于存放 autowired 与 bind 构造器的数据容器。\n\t */",
        ),
    ],
    "DefaultValue.java": [
        (
            "/**\n * Annotation that can be used to specify the default value when binding to an immutable\n * property. This annotation can also be used with nested properties to indicate that a\n * value should always be bound (rather than binding {@code null}). The value from this\n * annotation will only be used if the property is not found in the property sources used\n * by the {@link Binder}. For example, if the property is present in the\n * {@link org.springframework.core.env.Environment} when binding to\n * {@link org.springframework.boot.context.properties.ConfigurationProperties @ConfigurationProperties},\n * the default value for the property will not be used even if the property value is\n * empty.\n * <p>\n * NOTE: This annotation does not support property placeholder resolution and the value\n * must be constant.\n *\n * @author Madhura Bhave\n * @author Pavel Anisimov\n * @since 2.2.0\n */",
            "/**\n * 用于指定绑定不可变属性时的默认值的注解。也可用于嵌套属性，表示应始终绑定值（而非绑定 {@code null}）。\n * 仅当 {@link Binder} 使用的属性源中找不到该属性时，才会使用此注解中的值。例如，绑定\n * {@link org.springframework.boot.context.properties.ConfigurationProperties @ConfigurationProperties}\n * 时若属性存在于 {@link org.springframework.core.env.Environment} 中，即使属性值为空也不会使用默认值。\n * <p>\n * 注意：此注解不支持属性占位符解析，值必须为常量。\n *\n * @author Madhura Bhave\n * @author Pavel Anisimov\n * @since 2.2.0\n */",
        ),
        (
            "\t/**\n\t * The default value of the property. Can be an array of values for collection or\n\t * array-based properties.\n\t * @return the default value of the property.\n\t */",
            "\t/**\n\t * 属性的默认值。集合或数组类属性可为值数组。\n\t *\n\t * @return 属性的默认值\n\t */",
        ),
    ],
    "IndexedElementsBinder.java": [
        (
            "/**\n * Base class for {@link AggregateBinder AggregateBinders} that read a sequential run of\n * indexed items.\n *\n * @param <T> the type being bound\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 读取连续索引项的 {@link AggregateBinder AggregateBinder} 基类。\n *\n * @param <T> 正在绑定的类型\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "\t/**\n\t * Bind indexed elements to the supplied collection.\n\t * @param name the name of the property to bind\n\t * @param target the target bindable\n\t * @param elementBinder the binder to use for elements\n\t * @param aggregateType the aggregate type, may be a collection or an array\n\t * @param elementType the element type\n\t * @param result the destination for results\n\t */",
            "\t/**\n\t * 将索引元素绑定到提供的集合。\n\t *\n\t * @param name 待绑定属性名\n\t * @param target 目标 bindable\n\t * @param elementBinder 用于元素的绑定器\n\t * @param aggregateType 聚合类型，可为集合或数组\n\t * @param elementType 元素类型\n\t * @param result 结果存放处\n\t */",
        ),
        (
            "\t/**\n\t * {@link AggregateBinder.AggregateSupplier AggregateSupplier} for an indexed\n\t * collection.\n\t */",
            "\t/**\n\t * 用于索引集合的 {@link AggregateBinder.AggregateSupplier AggregateSupplier}。\n\t */",
        ),
    ],
    "JavaBeanBinder.java": [
        (
            "/**\n * {@link DataObjectBinder} for mutable Java Beans.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Lasse Wulff\n */",
            "/**\n * 用于可变 Java Bean 的 {@link DataObjectBinder}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Lasse Wulff\n */",
        ),
        (
            "\t/**\n\t * The properties of a bean that may be bound.\n\t */",
            "\t/**\n\t * 可被绑定的 Bean 属性。\n\t */",
        ),
        (
            "\t/**\n\t * The bean being bound.\n\t *\n\t * @param <T> the bean type\n\t */",
            "\t/**\n\t * 正在绑定的 Bean。\n\t *\n\t * @param <T> Bean 类型\n\t */",
        ),
        (
            "\t/**\n\t * A bean property being bound.\n\t */",
            "\t/**\n\t * 正在绑定的 Bean 属性。\n\t */",
        ),
    ],
    "MapBinder.java": [
        (
            "/**\n * {@link AggregateBinder} for Maps.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 用于 Map 的 {@link AggregateBinder}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
    ],
    "Name.java": [
        (
            "/**\n * Annotation that can be used to specify the name when binding to a property. This\n * annotation may be required when binding to names that clash with reserved language\n * keywords.\n * <p>\n * When naming a JavaBean-based property, annotate the field. When naming a\n * constructor-bound property, annotate the constructor parameter or record component.\n *\n * @author Phillip Webb\n * @author Lasse Wulff\n * @since 2.4.0\n */",
            "/**\n * 用于指定绑定属性时使用的名称的注解。绑定与语言保留关键字冲突的名称时可能需要此注解。\n * <p>\n * 命名基于 JavaBean 的属性时，标注在字段上；命名构造器绑定的属性时，标注在构造器参数或 record 组件上。\n *\n * @author Phillip Webb\n * @author Lasse Wulff\n * @since 2.4.0\n */",
        ),
        (
            "\t/**\n\t * The name of the property to use for binding.\n\t * @return the property name\n\t */",
            "\t/**\n\t * 绑定时要使用的属性名。\n\t *\n\t * @return 属性名\n\t */",
        ),
    ],
    "Nested.java": [
        (
            "/**\n * Meta-annotation that should be added to annotations that indicate a field is a nested\n * type. Used to ensure that correct reflection hints are registered.\n *\n * @author Phillip Webb\n * @since 3.0.0\n * @see BindableRuntimeHintsRegistrar\n */",
            "/**\n * 应添加到“字段为嵌套类型”指示注解上的元注解。用于确保注册正确的反射提示。\n *\n * @author Phillip Webb\n * @since 3.0.0\n * @see BindableRuntimeHintsRegistrar\n */",
        ),
    ],
    "PlaceholdersResolver.java": [
        (
            "/**\n * Optional strategy that used by a {@link Binder} to resolve property placeholders.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see PropertySourcesPlaceholdersResolver\n */",
            "/**\n * {@link Binder} 用于解析属性占位符的可选策略。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see PropertySourcesPlaceholdersResolver\n */",
        ),
        (
            "\t/**\n\t * No-op {@link PropertyResolver}.\n\t */",
            "\t/**\n\t * 空操作的 {@link PropertyResolver}。\n\t */",
        ),
        (
            "\t/**\n\t * Called to resolve any placeholders in the given value.\n\t * @param value the source value\n\t * @return a value with placeholders resolved\n\t */",
            "\t/**\n\t * 解析给定值中的占位符。\n\t *\n\t * @param value 源值\n\t * @return 占位符已解析的值\n\t */",
        ),
    ],
    "PropertySourcesPlaceholdersResolver.java": [
        (
            "/**\n * {@link PlaceholdersResolver} to resolve placeholders from {@link PropertySources}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 从 {@link PropertySources} 解析占位符的 {@link PlaceholdersResolver}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
    ],
    "UnboundConfigurationPropertiesException.java": [
        (
            "/**\n * {@link BindException} thrown when {@link ConfigurationPropertySource} elements were\n * left unbound.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 当 {@link ConfigurationPropertySource} 元素未被绑定时抛出的 {@link BindException}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:160]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
        if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
            shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            if has_chinese(text):
                cn_lines = len(re.findall(r"[\u4e00-\u9fff]", text))
                if cn_lines > 20:
                    ok += 1
                    print(f"SKIP(already CN) {rel}")
                    continue
            text = apply_replacements(text, reps)
            if not has_chinese(text):
                failures.append(f"NO_CHINESE_AFTER: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if not failures:
        mark_queue_done(BATCH_FILES)
        print("Marked 20 files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
