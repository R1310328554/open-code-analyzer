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

package org.springframework.context.expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.SpringProperties;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanExpressionResolver} 接口的标准实现，
 * 使用 Spring 表达式模块解析并求值 Spring EL。
 *
 * <p>容器 {@code BeanFactory} 中的所有 Bean 均以其常用 Bean 名称作为预定义变量可用，
 * 包括 "environment"、"systemProperties"、"systemEnvironment" 等标准上下文 Bean。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see BeanExpressionContext#getBeanFactory()
 * @see org.springframework.expression.ExpressionParser
 * @see org.springframework.expression.spel.standard.SpelExpressionParser
 * @see org.springframework.expression.spel.support.StandardEvaluationContext
 */
public class StandardBeanExpressionResolver implements BeanExpressionResolver {

	/**
	 * 用于配置 SpEL 表达式最大长度的系统属性：{@value}。
	 * <p>也可通过 {@link SpringProperties} 机制配置。
	 * @since 6.1.3
	 * @see SpelParserConfiguration#getMaximumExpressionLength()
	 */
	public static final String MAX_SPEL_EXPRESSION_LENGTH_PROPERTY_NAME = "spring.context.expression.maxLength";

	/** 默认表达式前缀："{#"。 */
	public static final String DEFAULT_EXPRESSION_PREFIX = "#{";

	/** 默认表达式后缀："}" 。 */
	public static final String DEFAULT_EXPRESSION_SUFFIX = "}";


	private String expressionPrefix = DEFAULT_EXPRESSION_PREFIX;

	private String expressionSuffix = DEFAULT_EXPRESSION_SUFFIX;

	private ExpressionParser expressionParser;

	private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>(256);

	private final Map<BeanExpressionContext, StandardEvaluationContext> evaluationCache = new ConcurrentHashMap<>(8);

	private final ParserContext beanExpressionParserContext = new ParserContext() {
		@Override
		public boolean isTemplate() {
			return true;
		}
		@Override
		public String getExpressionPrefix() {
			return expressionPrefix;
		}
		@Override
		public String getExpressionSuffix() {
			return expressionSuffix;
		}
	};


	/**
	 * 使用默认设置创建新的 {@code StandardBeanExpressionResolver}。
	 * <p>自 Spring Framework 6.1.3 起，可通过
	 * {@link #MAX_SPEL_EXPRESSION_LENGTH_PROPERTY_NAME} 属性配置 SpEL 表达式最大长度。
	 */
	public StandardBeanExpressionResolver() {
		this(null);
	}

	/**
	 * 使用给定 Bean 类加载器创建新的 {@code StandardBeanExpressionResolver}，
	 * 并以其作为表达式编译的基础。
	 * <p>自 Spring Framework 6.1.3 起，可通过
	 * {@link #MAX_SPEL_EXPRESSION_LENGTH_PROPERTY_NAME} 属性配置 SpEL 表达式最大长度。
	 * @param beanClassLoader 工厂的 Bean 类加载器
	 */
	public StandardBeanExpressionResolver(@Nullable ClassLoader beanClassLoader) {
		SpelParserConfiguration parserConfig = new SpelParserConfiguration(
				null, beanClassLoader, false, false, Integer.MAX_VALUE, retrieveMaxExpressionLength());
		this.expressionParser = new SpelExpressionParser(parserConfig);
	}


	/**
	 * 设置表达式字符串的前缀。
	 * 默认为 "#{" 。
	 * @see #DEFAULT_EXPRESSION_PREFIX
	 */
	public void setExpressionPrefix(String expressionPrefix) {
		Assert.hasText(expressionPrefix, "Expression prefix must not be empty");
		this.expressionPrefix = expressionPrefix;
	}

	/**
	 * 设置表达式字符串的后缀。
	 * 默认为 "}" 。
	 * @see #DEFAULT_EXPRESSION_SUFFIX
	 */
	public void setExpressionSuffix(String expressionSuffix) {
		Assert.hasText(expressionSuffix, "Expression suffix must not be empty");
		this.expressionSuffix = expressionSuffix;
	}

	/**
	 * 指定用于表达式解析的 EL 解析器。
	 * <p>默认为 {@link org.springframework.expression.spel.standard.SpelExpressionParser}，
	 * 兼容标准 Unified EL 风格的表达式语法。
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		Assert.notNull(expressionParser, "ExpressionParser must not be null");
		this.expressionParser = expressionParser;
	}


	@Override
	public @Nullable Object evaluate(@Nullable String value, BeanExpressionContext beanExpressionContext) throws BeansException {
		if (!StringUtils.hasLength(value)) {
			return value;
		}
		try {
			Expression expr = this.expressionCache.computeIfAbsent(value, expression ->
					this.expressionParser.parseExpression(expression, this.beanExpressionParserContext));
			EvaluationContext evalContext = this.evaluationCache.computeIfAbsent(beanExpressionContext, bec -> {
					ConfigurableBeanFactory beanFactory = bec.getBeanFactory();
					StandardEvaluationContext sec = new StandardEvaluationContext(bec);
					sec.addPropertyAccessor(new BeanExpressionContextAccessor());
					sec.addPropertyAccessor(new BeanFactoryAccessor());
					sec.addPropertyAccessor(new org.springframework.expression.spel.support.MapAccessor());
					sec.addPropertyAccessor(new EnvironmentAccessor());
					sec.setBeanResolver(new BeanFactoryResolver(beanFactory));
					sec.setTypeLocator(new StandardTypeLocator(beanFactory.getBeanClassLoader()));
					sec.setTypeConverter(new StandardTypeConverter(() -> {
						ConversionService cs = beanFactory.getConversionService();
						return (cs != null ? cs : DefaultConversionService.getSharedInstance());
					}));
					customizeEvaluationContext(sec);
					return sec;
				});
			return expr.getValue(evalContext);
		}
		catch (Throwable ex) {
			throw new BeanExpressionException("Expression parsing failed", ex);
		}
	}

	/**
	 * 用于自定义表达式求值上下文的模板方法。
	 * <p>默认实现为空。
	 */
	protected void customizeEvaluationContext(StandardEvaluationContext evalContext) {
	}

	private static int retrieveMaxExpressionLength() {
		String value = SpringProperties.getProperty(MAX_SPEL_EXPRESSION_LENGTH_PROPERTY_NAME);
		if (!StringUtils.hasText(value)) {
			return SpelParserConfiguration.DEFAULT_MAX_EXPRESSION_LENGTH;
		}

		try {
			int maxLength = Integer.parseInt(value.trim());
			Assert.isTrue(maxLength > 0, () -> "Value [" + maxLength + "] for system property [" +
					MAX_SPEL_EXPRESSION_LENGTH_PROPERTY_NAME + "] must be positive");
			return maxLength;
		}
		catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Failed to parse value for system property [" +
					MAX_SPEL_EXPRESSION_LENGTH_PROPERTY_NAME + "]: " + ex.getMessage(), ex);
		}
	}

}
