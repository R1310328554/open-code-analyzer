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

package org.springframework.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 公共委托：解析外部管理的构造器与方法上可自动装配的参数。
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 5.2
 * @see #isAutowirable
 * @see #resolveDependency
 */
public final class ParameterResolutionDelegate {

	/** 空注解数组常量 */
	private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

	/** 不携带任何注解的空 {@code AnnotatedElement} */
	private static final AnnotatedElement EMPTY_ANNOTATED_ELEMENT = new AnnotatedElement() {
		@Override
		public <T extends Annotation> @Nullable T getAnnotation(Class<T> annotationClass) {
			return null;
		}
		@Override
		public Annotation[] getAnnotations() {
			return EMPTY_ANNOTATION_ARRAY;
		}
		@Override
		public Annotation[] getDeclaredAnnotations() {
			return EMPTY_ANNOTATION_ARRAY;
		}
	};


	private ParameterResolutionDelegate() {
	}


	/**
	 * 判断给定 {@link Parameter} 是否<em>有可能</em>从
	 * {@link AutowireCapableBeanFactory} 自动装配。
	 * <p>若该参数标注或元标注了 {@link Autowired @Autowired}、
	 * {@link Qualifier @Qualifier} 或 {@link Value @Value}，则返回 {@code true}。
	 * <p>注意：即便本方法返回 {@code false}，{@link #resolveDependency} 仍可能解析出该参数的依赖。
	 * @param parameter 要自动装配依赖的参数（不得为 {@code null}）
	 * @param parameterIndex 参数在声明它的构造器或方法中的索引
	 * @see #resolveDependency
	 */
	public static boolean isAutowirable(Parameter parameter, int parameterIndex) {
		Assert.notNull(parameter, "Parameter must not be null");
		AnnotatedElement annotatedParameter = getEffectiveAnnotatedParameter(parameter, parameterIndex);
		return (AnnotatedElementUtils.hasAnnotation(annotatedParameter, Autowired.class) ||
				AnnotatedElementUtils.hasAnnotation(annotatedParameter, Qualifier.class) ||
				AnnotatedElementUtils.hasAnnotation(annotatedParameter, Value.class));
	}

	/**
	 * 从给定 {@link AutowireCapableBeanFactory} 解析给定 {@link Parameter} 的依赖。
	 * <p>为单个方法参数提供与 Spring 对自动装配字段/方法同等完善的自动装配支持，
	 * 包括 {@link Autowired @Autowired}、{@link Qualifier @Qualifier} 与
	 * {@link Value @Value}，以及 {@code @Value} 声明中的属性占位符与 SpEL 表达式。
	 * <p>除非参数标注或元标注了 {@link Autowired @Autowired} 且
	 * {@link Autowired#required required} 为 {@code false}，否则依赖视为必需。
	 * <p>若未显式声明<em>限定符</em>，则使用参数名作为限定符以消歧。
	 * @param parameter 要解析依赖的参数（不得为 {@code null}）
	 * @param parameterIndex 参数在声明它的构造器或方法中的索引
	 * @param containingClass 包含该参数的具体类；可能与声明参数的类不同
	 * （例如为其子类，并可能替换类型变量）（不得为 {@code null}）
	 * @param beanFactory 用于解析依赖的 {@code AutowireCapableBeanFactory}（不得为 {@code null}）
	 * @return 解析得到的对象；找不到则为 {@code null}
	 * @throws BeansException 若依赖解析失败
	 * @see #isAutowirable
	 * @see Autowired#required
	 * @see SynthesizingMethodParameter#forExecutable(Executable, int)
	 * @see AutowireCapableBeanFactory#resolveDependency(DependencyDescriptor, String)
	 */
	public static @Nullable Object resolveDependency(
			Parameter parameter, int parameterIndex, Class<?> containingClass, AutowireCapableBeanFactory beanFactory)
			throws BeansException {

		Assert.notNull(parameter, "Parameter must not be null");
		Assert.notNull(containingClass, "Containing class must not be null");
		Assert.notNull(beanFactory, "AutowireCapableBeanFactory must not be null");

		AnnotatedElement annotatedParameter = getEffectiveAnnotatedParameter(parameter, parameterIndex);
		Autowired autowired = AnnotatedElementUtils.findMergedAnnotation(annotatedParameter, Autowired.class);
		boolean required = (autowired == null || autowired.required());

		MethodParameter methodParameter = SynthesizingMethodParameter.forExecutable(
				parameter.getDeclaringExecutable(), parameterIndex);
		DependencyDescriptor descriptor = new DependencyDescriptor(methodParameter, required);
		descriptor.setContainingClass(containingClass);
		return beanFactory.resolveDependency(descriptor, null);
	}

	/**
	 * 由于 JDK 9 之前 {@code javac} 的缺陷，直接在 {@link Parameter} 上查找注解时，
	 * 对内部类构造器会失败。
	 * <p>说明：Spring 6 仍可能遇到用 {@code javac 8} 编译的用户代码，因此暂时保留该变通。
	 * <h4>JDK &lt; 9 中 javac 的缺陷</h4>
	 * <p>编译后字节码中的参数注解数组，会漏掉内部类构造器隐式的<em>外围实例</em>参数条目。
	 * <h4>变通做法</h4>
	 * <p>本方法通过允许调用方访问前一个 {@link Parameter}（即 {@code index - 1}）上的注解，
	 * 规避这一 off-by-one 错误。若给定 {@code index} 为 0，则返回空的 {@code AnnotatedElement}。
	 * <h4>警告</h4>
	 * <p>切勿把本方法返回的 {@code AnnotatedElement} 强转为 {@code Parameter} 并当参数使用：
	 * 其元数据（例如 {@link Parameter#getName()}、{@link Parameter#getType()} 等）
	 * 与内部类构造器在给定索引处声明的参数并不一致。
	 * @return 给定的 {@code parameter}；若上述缺陷生效，则返回<em>有效</em>的 {@code Parameter}
	 */
	private static AnnotatedElement getEffectiveAnnotatedParameter(Parameter parameter, int index) {
		Executable executable = parameter.getDeclaringExecutable();
		if (executable instanceof Constructor && ClassUtils.isInnerClass(executable.getDeclaringClass()) &&
				executable.getParameterAnnotations().length == executable.getParameterCount() - 1) {
			// JDK <9 javac 缺陷：内部类的注解数组不含外围实例参数，
			// 因此访问时需将实际参数索引减 1
			return (index == 0 ? EMPTY_ANNOTATED_ELEMENT : executable.getParameters()[index - 1]);
		}
		return parameter;
	}

}
