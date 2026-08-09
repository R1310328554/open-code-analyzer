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

package org.springframework.beans;

import java.beans.ConstructorProperties;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.reflect.KClass;
import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;
import kotlin.reflect.full.KClasses;
import kotlin.reflect.jvm.KCallablesJvm;
import kotlin.reflect.jvm.ReflectJvmMapping;
import org.jspecify.annotations.Nullable;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * JavaBeans 相关的静态便捷方法：实例化 Bean、检查 Bean 属性类型、复制 Bean 属性等。
 *
 * <p>主要用于框架内部，但在一定程度上也对应用类有用。若需更全面的 Bean 工具，
 * 可考虑
 * <a href="https://commons.apache.org/proper/commons-beanutils/">Apache Commons BeanUtils</a>、
 * <a href="https://github.com/ExpediaGroup/bull">BULL - Bean Utils Light Library</a>
 * 或类似的第三方框架。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Sebastien Deleuze
 */
public abstract class BeanUtils {

	/** 已知不存在对应 Editor 的类型集合，避免重复查找。 */
	private static final Set<Class<?>> unknownEditorTypes =
			Collections.newSetFromMap(new ConcurrentReferenceHashMap<>(64));

	/** 基本类型在构造器参数缺省时的默认值映射。 */
	private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES = Map.of(
			boolean.class, false,
			byte.class, (byte) 0,
			short.class, (short) 0,
			int.class, 0,
			long.class, 0L,
			float.class, 0F,
			double.class, 0D,
			char.class, '\0');

	/** 运行时是否可用 Kotlin 反射（避免硬依赖）。 */
	private static final boolean KOTLIN_REFLECT_PRESENT = KotlinDetector.isKotlinReflectPresent();


	/**
	 * 使用无参构造器实例化类的便捷方法。
	 * @param clazz 待实例化的类
	 * @return 新实例
	 * @throws BeanInstantiationException 若 Bean 无法实例化
	 * @see Class#newInstance()
	 * @deprecated 随 JDK 9 中 {@link Class#newInstance()} 的弃用而弃用
	 */
	@Deprecated(since = "5.0")
	public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface()) {
			throw new BeanInstantiationException(clazz, "Specified class is an interface");
		}
		try {
			return clazz.newInstance();
		}
		catch (InstantiationException ex) {
			throw new BeanInstantiationException(clazz, "Is it an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
		}
	}

	/**
	 * 使用「主构造器」（Kotlin 类，可能声明了默认参数）或默认构造器
	 * （普通 Java 类，期望标准无参设置）实例化类。
	 * <p>若给定不可访问（即非 public）的构造器，本方法会尝试将其设为可访问。
	 * @param clazz 待实例化的类
	 * @return 新实例
	 * @throws BeanInstantiationException 若 Bean 无法实例化。
	 * 原因可能尤其为：未找到主/默认构造器时的 {@link NoSuchMethodException}；
	 * 类定义无法解析时的 {@link NoClassDefFoundError} 或其他 {@link LinkageError}
	 * （例如运行时缺少依赖）；或构造器调用本身抛出的异常。
	 * @see Constructor#newInstance
	 */
	public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface()) {
			throw new BeanInstantiationException(clazz, "Specified class is an interface");
		}
		Constructor<T> ctor;
		try {
			// 优先尝试无参声明构造器
			ctor = clazz.getDeclaredConstructor();
		}
		catch (NoSuchMethodException ex) {
			// 回退到 Kotlin 主构造器或 Record 规范构造器
			ctor = findPrimaryConstructor(clazz);
			if (ctor == null) {
				throw new BeanInstantiationException(clazz, "No default constructor found", ex);
			}
		}
		catch (LinkageError err) {
			throw new BeanInstantiationException(clazz, "Unresolvable class definition", err);
		}
		return instantiateClass(ctor);
	}

	/**
	 * 使用无参构造器实例化类，并以指定可赋值类型返回新实例。
	 * <p>适用于待实例化类（{@code clazz}）类型不可用、但期望类型（{@code assignableTo}）已知的情形。
	 * <p>若给定不可访问（即非 public）的构造器，本方法会尝试将其设为可访问。
	 * @param clazz 待实例化的类
	 * @param assignableTo {@code clazz} 必须可赋值给的目标类型
	 * @return 新实例
	 * @throws BeanInstantiationException 若 Bean 无法实例化
	 * @see Constructor#newInstance
	 */
	@SuppressWarnings("unchecked")
	public static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo) throws BeanInstantiationException {
		Assert.isAssignable(assignableTo, clazz);
		return (T) instantiateClass(clazz);
	}

	/**
	 * 使用给定构造器实例化类的便捷方法。
	 * <p>若给定不可访问（即非 public）的构造器，本方法会尝试将其设为可访问；
	 * 并支持带可选参数与默认值的 Kotlin 类。
	 * @param ctor 用于实例化的构造器
	 * @param args 构造器参数（未指定参数可传 {@code null}；支持 Kotlin 可选参数与 Java 基本类型）
	 * @return 新实例
	 * @throws BeanInstantiationException 若 Bean 无法实例化
	 * @see Constructor#newInstance
	 */
	public static <T> T instantiateClass(Constructor<T> ctor, @Nullable Object... args) throws BeanInstantiationException {
		Assert.notNull(ctor, "Constructor must not be null");
		try {
			ReflectionUtils.makeAccessible(ctor);
			if (KOTLIN_REFLECT_PRESENT && KotlinDetector.isKotlinType(ctor.getDeclaringClass())) {
				return KotlinDelegate.instantiateClass(ctor, args);
			}
			else {
				int parameterCount = ctor.getParameterCount();
				Assert.isTrue(args.length <= parameterCount, "Can't specify more arguments than constructor parameters");
				if (parameterCount == 0) {
					return ctor.newInstance();
				}
				Class<?>[] parameterTypes = ctor.getParameterTypes();
				@Nullable Object[] argsWithDefaultValues = new Object[args.length];
				// 将 null 基本类型参数替换为对应默认值
				for (int i = 0 ; i < args.length; i++) {
					if (args[i] == null) {
						Class<?> parameterType = parameterTypes[i];
						argsWithDefaultValues[i] = (parameterType.isPrimitive() ? DEFAULT_TYPE_VALUES.get(parameterType) : null);
					}
					else {
						argsWithDefaultValues[i] = args[i];
					}
				}
				return ctor.newInstance(argsWithDefaultValues);
			}
		}
		catch (InstantiationException ex) {
			throw new BeanInstantiationException(ctor, "Is it an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new BeanInstantiationException(ctor, "Is the constructor accessible?", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new BeanInstantiationException(ctor, "Illegal arguments for constructor", ex);
		}
		catch (InvocationTargetException ex) {
			throw new BeanInstantiationException(ctor, "Constructor threw exception", ex.getTargetException());
		}
	}

	/**
	 * 返回给定类可解析的构造器：主构造器、唯一的带参 public 构造器、
	 * 唯一的非 public 带参构造器，或简单的默认构造器。
	 * <p>调用方须准备好为返回构造器的参数（若有）解析实参。
	 * @param clazz 待检查的类
	 * @throws IllegalStateException 若完全找不到唯一构造器
	 * @since 5.3
	 * @see #findPrimaryConstructor
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getResolvableConstructor(Class<T> clazz) {
		Constructor<T> ctor = findPrimaryConstructor(clazz);
		if (ctor != null) {
			return ctor;
		}

		Constructor<?>[] ctors = clazz.getConstructors();
		if (ctors.length == 1) {
			// 唯一的 public 构造器
			return (Constructor<T>) ctors[0];
		}
		else if (ctors.length == 0) {
			// 无 public 构造器 → 检查非 public
			ctors = clazz.getDeclaredConstructors();
			if (ctors.length == 1) {
				// 唯一的非 public 构造器，例如非 public Record 类型
				return (Constructor<T>) ctors[0];
			}
		}

		// 多个构造器 → 尝试默认无参构造器
		try {
			return clazz.getDeclaredConstructor();
		}
		catch (NoSuchMethodException ex) {
			// 放弃
		}

		// 完全没有唯一构造器
		throw new IllegalStateException("No primary or single unique constructor found for " + clazz);
	}

	/**
	 * 返回给定类的主构造器。对 Kotlin 类，返回与 Kotlin 主构造器（按 Kotlin 规范定义）
	 * 对应的 Java 构造器；对 Java Record，返回规范构造器；否则返回 {@code null}。
	 * @param clazz 待检查的类
	 * @since 5.0
	 * @see <a href="https://kotlinlang.org/docs/reference/classes.html#constructors">Kotlin constructors</a>
	 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.10.4">Record constructor declarations</a>
	 */
	public static <T> @Nullable Constructor<T> findPrimaryConstructor(Class<T> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		if (KOTLIN_REFLECT_PRESENT && KotlinDetector.isKotlinType(clazz)) {
			return KotlinDelegate.findPrimaryConstructor(clazz);
		}
		if (clazz.isRecord()) {
			try {
				// 使用始终存在的规范构造器
				RecordComponent[] components = clazz.getRecordComponents();
				Class<?>[] paramTypes = new Class<?>[components.length];
				for (int i = 0; i < components.length; i++) {
					paramTypes[i] = components[i].getType();
				}
				return clazz.getDeclaredConstructor(paramTypes);
			}
			catch (NoSuchMethodException ignored) {
			}
		}
		return null;
	}

	/**
	 * 在给定类或其超类上查找具有指定方法名与参数类型的方法。
	 * 优先 public 方法，但也会返回 protected、包访问或 private 方法。
	 * <p>先调用 {@code Class.getMethod}，失败则回退到 {@code findDeclaredMethod}。
	 * 这样即使在 Java 安全设置受限的环境中也能无问题地找到 public 方法。
	 * @param clazz 待检查的类
	 * @param methodName 要查找的方法名
	 * @param paramTypes 要查找的方法的参数类型
	 * @return Method 对象，未找到则返回 {@code null}
	 * @see Class#getMethod
	 * @see #findDeclaredMethod
	 */
	public static @Nullable Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(methodName, paramTypes);
		}
		catch (NoSuchMethodException ex) {
			return findDeclaredMethod(clazz, methodName, paramTypes);
		}
	}

	/**
	 * 在给定类或其超类上查找具有指定方法名与参数类型的方法。
	 * 可返回 public、protected、包访问或 private 方法。
	 * <p>调用 {@code Class.getDeclaredMethod}，并向上遍历所有超类。
	 * @param clazz 待检查的类
	 * @param methodName 要查找的方法名
	 * @param paramTypes 要查找的方法的参数类型
	 * @return Method 对象，未找到则返回 {@code null}
	 * @see Class#getDeclaredMethod
	 */
	public static @Nullable Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			return clazz.getDeclaredMethod(methodName, paramTypes);
		}
		catch (NoSuchMethodException ex) {
			if (clazz.getSuperclass() != null) {
				return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
			}
			return null;
		}
	}

	/**
	 * 在给定类或其超类上查找具有指定方法名且参数最少（理想情况为无参）的方法。
	 * 优先 public 方法，但也会返回 protected、包访问或 private 方法。
	 * <p>先调用 {@code Class.getMethods}，失败则回退到
	 * {@code findDeclaredMethodWithMinimalParameters}。
	 * 这样即使在 Java 安全设置受限的环境中也能无问题地找到 public 方法。
	 * @param clazz 待检查的类
	 * @param methodName 要查找的方法名
	 * @return Method 对象，未找到则返回 {@code null}
	 * @throws IllegalArgumentException 若找到同名方法但无法解析为唯一的参数最少方法
	 * @see Class#getMethods
	 * @see #findDeclaredMethodWithMinimalParameters
	 */
	public static @Nullable Method findMethodWithMinimalParameters(Class<?> clazz, String methodName)
			throws IllegalArgumentException {

		Method targetMethod = findMethodWithMinimalParameters(clazz.getMethods(), methodName);
		if (targetMethod == null) {
			targetMethod = findDeclaredMethodWithMinimalParameters(clazz, methodName);
		}
		return targetMethod;
	}

	/**
	 * 在给定类或其超类上查找具有指定方法名且参数最少（理想情况为无参）的方法。
	 * 可返回 public、protected、包访问或 private 方法。
	 * <p>调用 {@code Class.getDeclaredMethods}，并向上遍历所有超类。
	 * @param clazz 待检查的类
	 * @param methodName 要查找的方法名
	 * @return Method 对象，未找到则返回 {@code null}
	 * @throws IllegalArgumentException 若找到同名方法但无法解析为唯一的参数最少方法
	 * @see Class#getDeclaredMethods
	 */
	public static @Nullable Method findDeclaredMethodWithMinimalParameters(Class<?> clazz, String methodName)
			throws IllegalArgumentException {

		Method targetMethod = findMethodWithMinimalParameters(clazz.getDeclaredMethods(), methodName);
		if (targetMethod == null && clazz.getSuperclass() != null) {
			targetMethod = findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
		}
		return targetMethod;
	}

	/**
	 * 在给定方法列表中查找具有指定方法名且参数最少（理想情况为无参）的方法。
	 * @param methods 待检查的方法数组
	 * @param methodName 要查找的方法名
	 * @return Method 对象，未找到则返回 {@code null}
	 * @throws IllegalArgumentException 若找到同名方法但无法解析为唯一的参数最少方法
	 */
	public static @Nullable Method findMethodWithMinimalParameters(Method[] methods, String methodName)
			throws IllegalArgumentException {

		Method targetMethod = null;
		int numMethodsFoundWithCurrentMinimumArgs = 0;
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				int numParams = method.getParameterCount();
				if (targetMethod == null || numParams < targetMethod.getParameterCount()) {
					targetMethod = method;
					numMethodsFoundWithCurrentMinimumArgs = 1;
				}
				else if (!method.isBridge() && targetMethod.getParameterCount() == numParams) {
					if (targetMethod.isBridge()) {
						// 优先普通方法而非桥接方法
						targetMethod = method;
					}
					else {
						// 参数个数相同的额外候选
						numMethodsFoundWithCurrentMinimumArgs++;
					}
				}
			}
		}
		if (numMethodsFoundWithCurrentMinimumArgs > 1) {
			throw new IllegalArgumentException("Cannot resolve method '" + methodName +
					"' to a unique method. Attempted to resolve to overloaded method with " +
					"the least number of parameters but there were " +
					numMethodsFoundWithCurrentMinimumArgs + " candidates.");
		}
		return targetMethod;
	}

	/**
	 * 解析形如 {@code methodName[([arg_list])]} 的方法签名，
	 * 其中 {@code arg_list} 为可选的、逗号分隔的全限定类型名列表，
	 * 并尝试在给定 {@code Class} 上解析该签名。
	 * <p>不提供参数列表（{@code methodName}）时，返回名称匹配且参数最少的方法。
	 * 提供参数类型列表时，仅返回名称与参数类型均匹配的方法。
	 * <p>注意：{@code methodName} 与 {@code methodName()} 的解析方式<strong>不同</strong>。
	 * 签名 {@code methodName} 表示参数最少的 {@code methodName} 方法；
	 * {@code methodName()} 表示恰好 0 个参数的 {@code methodName} 方法。
	 * <p>若找不到方法则返回 {@code null}。
	 * @param signature 方法签名的字符串表示
	 * @param clazz 用于解析方法签名的类
	 * @return 解析得到的 Method
	 * @see #findMethod
	 * @see #findMethodWithMinimalParameters
	 */
	public static @Nullable Method resolveSignature(String signature, Class<?> clazz) {
		Assert.hasText(signature, "'signature' must not be empty");
		Assert.notNull(clazz, "Class must not be null");
		int startParen = signature.indexOf('(');
		int endParen = signature.indexOf(')');
		if (startParen > -1 && endParen == -1) {
			throw new IllegalArgumentException("Invalid method signature '" + signature +
					"': expected closing ')' for args list");
		}
		else if (startParen == -1 && endParen > -1) {
			throw new IllegalArgumentException("Invalid method signature '" + signature +
					"': expected opening '(' for args list");
		}
		else if (startParen == -1) {
			return findMethodWithMinimalParameters(clazz, signature);
		}
		else {
			String methodName = signature.substring(0, startParen);
			String[] parameterTypeNames =
					StringUtils.commaDelimitedListToStringArray(signature.substring(startParen + 1, endParen));
			Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
			for (int i = 0; i < parameterTypeNames.length; i++) {
				String parameterTypeName = parameterTypeNames[i].trim();
				try {
					parameterTypes[i] = ClassUtils.forName(parameterTypeName, clazz.getClassLoader());
				}
				catch (Throwable ex) {
					throw new IllegalArgumentException("Invalid method signature: unable to resolve type [" +
							parameterTypeName + "] for argument " + i + ". Root cause: " + ex);
				}
			}
			return findMethod(clazz, methodName, parameterTypes);
		}
	}


	/**
	 * 获取给定类的 JavaBeans {@code PropertyDescriptor} 数组。
	 * @param clazz 要获取 PropertyDescriptor 的 Class
	 * @return 给定类的 {@code PropertyDescriptors} 数组
	 * @throws BeansException 若 PropertyDescriptor 查找失败
	 */
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
		return CachedIntrospectionResults.forClass(clazz).getPropertyDescriptors();
	}

	/**
	 * 获取给定属性的 JavaBeans {@code PropertyDescriptor}。
	 * @param clazz 要获取 PropertyDescriptor 的 Class
	 * @param propertyName 属性名
	 * @return 对应的 PropertyDescriptor，若无则返回 {@code null}
	 * @throws BeansException 若 PropertyDescriptor 查找失败
	 */
	public static @Nullable PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) throws BeansException {
		return CachedIntrospectionResults.forClass(clazz).getPropertyDescriptor(propertyName);
	}

	/**
	 * 为给定方法查找 JavaBeans {@code PropertyDescriptor}，
	 * 该方法须为该 Bean 属性的读方法或写方法。
	 * @param method 要查找对应 PropertyDescriptor 的方法，对其声明类进行内省
	 * @return 对应的 PropertyDescriptor，若无则返回 {@code null}
	 * @throws BeansException 若 PropertyDescriptor 查找失败
	 */
	public static @Nullable PropertyDescriptor findPropertyForMethod(Method method) throws BeansException {
		return findPropertyForMethod(method, method.getDeclaringClass());
	}

	/**
	 * 为给定方法查找 JavaBeans {@code PropertyDescriptor}，
	 * 该方法须为该 Bean 属性的读方法或写方法。
	 * @param method 要查找对应 PropertyDescriptor 的方法
	 * @param clazz 用于内省描述符的（最具体）类
	 * @return 对应的 PropertyDescriptor，若无则返回 {@code null}
	 * @throws BeansException 若 PropertyDescriptor 查找失败
	 * @since 3.2.13
	 */
	public static @Nullable PropertyDescriptor findPropertyForMethod(Method method, Class<?> clazz) throws BeansException {
		Assert.notNull(method, "Method must not be null");
		PropertyDescriptor[] pds = getPropertyDescriptors(clazz);
		for (PropertyDescriptor pd : pds) {
			if (method.equals(pd.getReadMethod()) || method.equals(pd.getWriteMethod())) {
				return pd;
			}
		}
		return null;
	}

	/**
	 * 按「Editor」后缀约定查找 JavaBeans PropertyEditor
	 * （例如 "mypackage.MyDomainClass" → "mypackage.MyDomainClassEditor"）。
	 * <p>与 {@link java.beans.PropertyEditorManager} 实现的
	 * 标准 JavaBeans 约定兼容，但与后者为基本类型注册的默认 Editor 隔离。
	 * @param targetType 要查找 Editor 的类型
	 * @return 对应的 Editor，未找到则返回 {@code null}
	 */
	public static @Nullable PropertyEditor findEditorByConvention(@Nullable Class<?> targetType) {
		if (targetType == null || targetType.isArray() || unknownEditorTypes.contains(targetType)) {
			return null;
		}

		ClassLoader cl = targetType.getClassLoader();
		if (cl == null) {
			try {
				cl = ClassLoader.getSystemClassLoader();
				if (cl == null) {
					return null;
				}
			}
			catch (Throwable ex) {
				// 例如 Google App Engine 上的 AccessControlException
				return null;
			}
		}

		String targetTypeName = targetType.getName();
		String editorName = targetTypeName + "Editor";
		try {
			Class<?> editorClass = cl.loadClass(editorName);
			if (editorClass != null) {
				if (!PropertyEditor.class.isAssignableFrom(editorClass)) {
					unknownEditorTypes.add(targetType);
					return null;
				}
				return (PropertyEditor) instantiateClass(editorClass);
			}
			// 行为异常的 ClassLoader 返回 null 而非 ClassNotFoundException
			// — 回退到下方未知 Editor 类型登记
		}
		catch (ClassNotFoundException ex) {
			// 忽略 — 回退到下方未知 Editor 类型登记
		}
		unknownEditorTypes.add(targetType);
		return null;
	}

	/**
	 * 从给定类/接口中确定指定属性的 Bean 属性类型（若可能）。
	 * @param propertyName Bean 属性名
	 * @param beanClasses 待检查的类
	 * @return 属性类型，回退为 {@code Object.class}
	 */
	public static Class<?> findPropertyType(String propertyName, Class<?> @Nullable ... beanClasses) {
		if (beanClasses != null) {
			for (Class<?> beanClass : beanClasses) {
				PropertyDescriptor pd = getPropertyDescriptor(beanClass, propertyName);
				if (pd != null) {
					return pd.getPropertyType();
				}
			}
		}
		return Object.class;
	}

	/**
	 * 判断指定属性是否具有唯一的写方法，
	 * 即可写且未声明重载 setter 方法。
	 * @param pd 属性的 PropertyDescriptor
	 * @return 可写且唯一时为 {@code true}，否则为 {@code false}
	 * @since 6.1.4
	 */
	public static boolean hasUniqueWriteMethod(PropertyDescriptor pd) {
		if (pd instanceof GenericTypeAwarePropertyDescriptor gpd) {
			return gpd.hasUniqueWriteMethod();
		}
		else {
			return (pd.getWriteMethod() != null);
		}
	}

	/**
	 * 为指定属性的写方法获取新的 MethodParameter 对象。
	 * @param pd 属性的 PropertyDescriptor
	 * @return 对应的 MethodParameter 对象
	 */
	public static MethodParameter getWriteMethodParameter(PropertyDescriptor pd) {
		if (pd instanceof GenericTypeAwarePropertyDescriptor gpd) {
			return new MethodParameter(gpd.getWriteMethodParameter());
		}
		else {
			Method writeMethod = pd.getWriteMethod();
			Assert.state(writeMethod != null, "No write method available");
			return new MethodParameter(writeMethod, 0);
		}
	}

	/**
	 * 确定给定构造器的必需参数名，
	 * 同时考虑 JavaBeans {@link ConstructorProperties} 注解
	 * 与 Spring 的 {@link DefaultParameterNameDiscoverer}。
	 * @param ctor 要查找参数名的构造器
	 * @return 参数名（与构造器参数个数匹配）
	 * @throws IllegalStateException 若参数名无法解析
	 * @since 5.3
	 * @see ConstructorProperties
	 * @see DefaultParameterNameDiscoverer
	 */
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public static @Nullable String[] getParameterNames(Constructor<?> ctor) {
		ConstructorProperties cp = ctor.getAnnotation(ConstructorProperties.class);
		@Nullable String[] paramNames = (cp != null ? cp.value() :
				DefaultParameterNameDiscoverer.getSharedInstance().getParameterNames(ctor));
		Assert.state(paramNames != null, () -> "Cannot resolve parameter names for constructor " + ctor);
		int parameterCount = (KOTLIN_REFLECT_PRESENT && KotlinDelegate.hasDefaultConstructorMarker(ctor) ?
				ctor.getParameterCount() - 1 : ctor.getParameterCount());
		Assert.state(paramNames.length == parameterCount,
				() -> "Invalid number of parameter names: " + paramNames.length + " for constructor " + ctor);
		return paramNames;
	}

	/**
	 * 检查给定类型是否表示「简单」属性：简单值类型或简单值类型数组。
	 * <p>参见 {@link #isSimpleValueType(Class)} 对<em>简单值类型</em>的定义。
	 * <p>用于确定在「简单」依赖检查中要检查的属性。
	 * @param type 待检查的类型
	 * @return 给定类型是否表示「简单」属性
	 * @see org.springframework.beans.factory.support.RootBeanDefinition#DEPENDENCY_CHECK_SIMPLE
	 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#checkDependencies
	 * @see #isSimpleValueType(Class)
	 */
	public static boolean isSimpleProperty(Class<?> type) {
		Assert.notNull(type, "'type' must not be null");
		return isSimpleValueType(type) || (type.isArray() && isSimpleValueType(type.componentType()));
	}

	/**
	 * 检查给定类型在 Bean 属性与数据绑定语境下是否表示「简单」值类型：
	 * 基本类型或包装类、{@code Enum}、{@code String} 或其他 {@code CharSequence}、
	 * {@code Number}、{@code Date}、{@code Temporal}、{@code UUID}、{@code URI}、
	 * {@code URL}、{@code Locale} 或 {@code Class}。
	 * <p>{@code Void} 与 {@code void} 不视为简单值类型。
	 * <p>自 6.1 起，本方法原样委托给 {@link ClassUtils#isSimpleValueType}，
	 * 但未来可能为 Bean 属性用途增加更多规则。
	 * @param type 待检查的类型
	 * @return 给定类型是否表示「简单」值类型
	 * @see #isSimpleProperty(Class)
	 * @see ClassUtils#isSimpleValueType(Class)
	 */
	public static boolean isSimpleValueType(Class<?> type) {
		return ClassUtils.isSimpleValueType(type);
	}


	/**
	 * 将源 Bean 的属性值复制到目标 Bean。
	 * <p>注意：源与目标类不必相同或存在继承关系，只要属性匹配即可。
	 * 源 Bean 暴露但目标 Bean 没有的属性会被静默忽略。
	 * <p>这只是便捷方法。更复杂的传输需求请考虑使用完整的 {@link BeanWrapper}。
	 * <p>自 Spring Framework 5.3 起，本方法在匹配源与目标对象属性时会尊重泛型类型信息。
	 * <p>下表给出源与目标属性类型可复制及不可复制的非穷尽示例。
	 * <table border="1">
	 * <tr><th>源属性类型</th><th>目标属性类型</th><th>是否支持复制</th></tr>
	 * <tr><td>{@code Integer}</td><td>{@code Integer}</td><td>是</td></tr>
	 * <tr><td>{@code Integer}</td><td>{@code Number}</td><td>是</td></tr>
	 * <tr><td>{@code List<Integer>}</td><td>{@code List<Integer>}</td><td>是</td></tr>
	 * <tr><td>{@code List<?>}</td><td>{@code List<?>}</td><td>是</td></tr>
	 * <tr><td>{@code List<Integer>}</td><td>{@code List<?>}</td><td>是</td></tr>
	 * <tr><td>{@code List<Integer>}</td><td>{@code List<? extends Number>}</td><td>是</td></tr>
	 * <tr><td>{@code String}</td><td>{@code Integer}</td><td>否</td></tr>
	 * <tr><td>{@code Number}</td><td>{@code Integer}</td><td>否</td></tr>
	 * <tr><td>{@code List<Integer>}</td><td>{@code List<Long>}</td><td>否</td></tr>
	 * <tr><td>{@code List<Integer>}</td><td>{@code List<Number>}</td><td>否</td></tr>
	 * </table>
	 * @param source 源 Bean
	 * @param target 目标 Bean
	 * @throws BeansException 若复制失败
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target) throws BeansException {
		copyProperties(source, target, null, (String[]) null);
	}

	/**
	 * 将源 Bean 的属性值复制到目标 Bean，仅设置给定「可编辑」类（或接口）中定义的属性。
	 * <p>注意：源与目标类不必相同或存在继承关系，只要属性匹配即可。
	 * 源 Bean 暴露但目标 Bean 没有的属性会被静默忽略。
	 * <p>这只是便捷方法。更复杂的传输需求请考虑使用完整的 {@link BeanWrapper}。
	 * <p>自 Spring Framework 5.3 起，本方法在匹配源与目标对象属性时会尊重泛型类型信息。
	 * 详见 {@link #copyProperties(Object, Object)} 的文档。
	 * @param source 源 Bean
	 * @param target 目标 Bean
	 * @param editable 限制属性设置范围的类（或接口）
	 * @throws BeansException 若复制失败
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target, Class<?> editable) throws BeansException {
		copyProperties(source, target, editable, (String[]) null);
	}

	/**
	 * 将源 Bean 的属性值复制到目标 Bean，忽略给定的 {@code ignoreProperties}。
	 * <p>注意：源与目标类不必相同或存在继承关系，只要属性匹配即可。
	 * 源 Bean 暴露但目标 Bean 没有的属性会被静默忽略。
	 * <p>这只是便捷方法。更复杂的传输需求请考虑使用完整的 {@link BeanWrapper}。
	 * <p>自 Spring Framework 5.3 起，本方法在匹配源与目标对象属性时会尊重泛型类型信息。
	 * 详见 {@link #copyProperties(Object, Object)} 的文档。
	 * @param source 源 Bean
	 * @param target 目标 Bean
	 * @param ignoreProperties 要忽略的属性名数组
	 * @throws BeansException 若复制失败
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {
		copyProperties(source, target, null, ignoreProperties);
	}

	/**
	 * 将源 Bean 的属性值复制到目标 Bean。
	 * <p>注意：源与目标类不必相同或存在继承关系，只要属性匹配即可。
	 * 源 Bean 暴露但目标 Bean 没有的属性会被静默忽略。
	 * <p>自 Spring Framework 5.3 起，本方法在匹配源与目标对象属性时会尊重泛型类型信息。
	 * 详见 {@link #copyProperties(Object, Object)} 的文档。
	 * @param source 源 Bean
	 * @param target 目标 Bean
	 * @param editable 限制属性设置范围的类（或接口）
	 * @param ignoreProperties 要忽略的属性名数组
	 * @throws BeansException 若复制失败
	 * @see BeanWrapper
	 */
	private static void copyProperties(Object source, Object target, @Nullable Class<?> editable,
			String @Nullable ... ignoreProperties) throws BeansException {

		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		Class<?> actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
						"] not assignable to editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		Set<String> ignoredProps = (ignoreProperties != null ? new HashSet<>(Arrays.asList(ignoreProperties)) : null);
		// 源与目标类型不同时，单独缓存源类的内省结果
		CachedIntrospectionResults sourceResults = (actualEditable != source.getClass() ?
				CachedIntrospectionResults.forClass(source.getClass()) : null);

		for (PropertyDescriptor targetPd : targetPds) {
			Method writeMethod = targetPd.getWriteMethod();
			if (writeMethod != null && (ignoredProps == null || !ignoredProps.contains(targetPd.getName()))) {
				PropertyDescriptor sourcePd = (sourceResults != null ?
						sourceResults.getPropertyDescriptor(targetPd.getName()) : targetPd);
				if (sourcePd != null) {
					Method readMethod = sourcePd.getReadMethod();
					if (readMethod != null) {
						if (isAssignable(writeMethod, readMethod, sourcePd, targetPd)) {
							try {
								ReflectionUtils.makeAccessible(readMethod);
								Object value = readMethod.invoke(source);
								ReflectionUtils.makeAccessible(writeMethod);
								writeMethod.invoke(target, value);
							}
							catch (Throwable ex) {
								throw new FatalBeanException(
										"Could not copy property '" + targetPd.getName() + "' from source to target", ex);
							}
						}
					}
				}
			}
		}
	}

	/** 判断读方法与写方法的类型是否可赋值（含泛型感知）。 */
	private static boolean isAssignable(Method writeMethod, Method readMethod,
			PropertyDescriptor sourcePd, PropertyDescriptor targetPd) {

		Type paramType = writeMethod.getGenericParameterTypes()[0];
		if (paramType instanceof Class<?> clazz) {
			return ClassUtils.isAssignable(clazz, readMethod.getReturnType());
		}
		else if (paramType.equals(readMethod.getGenericReturnType())) {
			return true;
		}
		else {
			ResolvableType sourceType = ((GenericTypeAwarePropertyDescriptor) sourcePd).getReadMethodType();
			ResolvableType targetType = ((GenericTypeAwarePropertyDescriptor) targetPd).getWriteMethodType();
			// 若任一侧 ResolvableType 含不可解析泛型，则忽略泛型仅做可赋值检查
			return (sourceType.hasUnresolvableGenerics() || targetType.hasUnresolvableGenerics() ?
					ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType()) :
					targetType.isAssignableFrom(sourceType));
		}
	}


	/**
	 * 内部类，避免运行时对 Kotlin 的硬依赖。
	 */
	private static class KotlinDelegate {

		/**
		 * 获取与 Kotlin 主构造器对应的 Java 构造器（若有）。
		 * @param clazz Kotlin 类的 {@link Class}
		 * @see <a href="https://kotlinlang.org/docs/reference/classes.html#constructors">
		 * https://kotlinlang.org/docs/reference/classes.html#constructors</a>
		 */
		@SuppressWarnings("unchecked")
		public static <T> @Nullable Constructor<T> findPrimaryConstructor(Class<T> clazz) {
			try {
				KClass<T> kClass = JvmClassMappingKt.getKotlinClass(clazz);
				KFunction<T> primaryCtor = KClasses.getPrimaryConstructor(kClass);
				if (primaryCtor == null) {
					return null;
				}
				if (KotlinDetector.isInlineClass(clazz)) {
					Constructor<?>[] constructors = clazz.getDeclaredConstructors();
					Assert.state(constructors.length == 1,
							"Kotlin value classes annotated with @JvmInline are expected to have a single JVM constructor");
					return (Constructor<T>) constructors[0];
				}
				Constructor<T> constructor = ReflectJvmMapping.getJavaConstructor(primaryCtor);
				if (constructor == null) {
					throw new IllegalStateException(
							"Failed to find Java constructor for Kotlin primary constructor: " + clazz.getName());
				}
				return constructor;
			}
			catch (UnsupportedOperationException ex) {
				return null;
			}
		}

		/**
		 * 使用给定构造器实例化 Kotlin 类。
		 * @param ctor 要实例化的 Kotlin 类构造器
		 * @param args 构造器参数（未指定参数可传 {@code null}）
		 */
		public static <T> T instantiateClass(Constructor<T> ctor, @Nullable Object... args)
				throws IllegalAccessException, InvocationTargetException, InstantiationException {

			KFunction<T> kotlinConstructor = ReflectJvmMapping.getKotlinFunction(ctor);
			if (kotlinConstructor == null) {
				return ctor.newInstance(args);
			}

			if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers()))) {
				KCallablesJvm.setAccessible(kotlinConstructor, true);
			}

			List<KParameter> parameters = kotlinConstructor.getParameters();

			Assert.isTrue(args.length <= parameters.size(),
					"Number of provided arguments must be less than or equal to the number of constructor parameters");
			if (parameters.isEmpty()) {
				return kotlinConstructor.call();
			}
			// 按参数映射调用，跳过可选且为 null 的参数
			Map<KParameter, Object> argParameters = CollectionUtils.newHashMap(parameters.size());
			for (int i = 0 ; i < args.length ; i++) {
				if (!(parameters.get(i).isOptional() && args[i] == null)) {
					argParameters.put(parameters.get(i), args[i]);
				}
			}
			return kotlinConstructor.callBy(argParameters);
		}

		/** 构造器是否带有 Kotlin 默认参数标记参数。 */
		public static boolean hasDefaultConstructorMarker(Constructor<?> ctor) {
			int parameterCount = ctor.getParameterCount();
			return parameterCount > 0 && ctor.getParameters()[parameterCount -1].getType() == DefaultConstructorMarker.class;
		}
	}

}
