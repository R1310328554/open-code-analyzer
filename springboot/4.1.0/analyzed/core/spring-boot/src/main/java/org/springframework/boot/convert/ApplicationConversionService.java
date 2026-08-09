/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.convert;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * {@link FormattingConversionService} 的特化实现，默认注册适用于大多数 Spring Boot 应用的
 * 转换器与格式化器。
 * <p>
 * 可直接实例化，也提供静态工具方法 {@link #addApplicationConverters} 与
 * {@link #addApplicationFormatters(FormatterRegistry)}，便于向已有注册表追加配置。
 *
 * @author Phillip Webb
 * @author Shixiong Guo
 * @since 2.0.0
 */
public class ApplicationConversionService extends FormattingConversionService {

	private static final ResolvableType STRING = ResolvableType.forClass(String.class);

	private static volatile @Nullable ApplicationConversionService sharedInstance;

	private final boolean unmodifiable;

	public ApplicationConversionService() {
		this(null);
	}

	public ApplicationConversionService(@Nullable StringValueResolver embeddedValueResolver) {
		this(embeddedValueResolver, false);
	}

	private ApplicationConversionService(@Nullable StringValueResolver embeddedValueResolver, boolean unmodifiable) {
		if (embeddedValueResolver != null) {
			setEmbeddedValueResolver(embeddedValueResolver);
		}
		configure(this);
		this.unmodifiable = unmodifiable;
	}

	@Override
	public void addPrinter(Printer<?> printer) {
		assertModifiable();
		super.addPrinter(printer);
	}

	@Override
	public void addParser(Parser<?> parser) {
		assertModifiable();
		super.addParser(parser);
	}

	@Override
	public void addFormatter(Formatter<?> formatter) {
		assertModifiable();
		super.addFormatter(formatter);
	}

	@Override
	public void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter) {
		assertModifiable();
		super.addFormatterForFieldType(fieldType, formatter);
	}

	@Override
	public void addConverter(Converter<?, ?> converter) {
		assertModifiable();
		super.addConverter(converter);
	}

	@Override
	public void addFormatterForFieldType(Class<?> fieldType, Printer<?> printer, Parser<?> parser) {
		assertModifiable();
		super.addFormatterForFieldType(fieldType, printer, parser);
	}

	@Override
	public void addFormatterForFieldAnnotation(
			AnnotationFormatterFactory<? extends Annotation> annotationFormatterFactory) {
		assertModifiable();
		super.addFormatterForFieldAnnotation(annotationFormatterFactory);
	}

	@Override
	public <S, T> void addConverter(Class<S> sourceType, Class<T> targetType,
			Converter<? super S, ? extends T> converter) {
		assertModifiable();
		super.addConverter(sourceType, targetType, converter);
	}

	@Override
	public void addConverter(GenericConverter converter) {
		assertModifiable();
		super.addConverter(converter);
	}

	@Override
	public void addConverterFactory(ConverterFactory<?, ?> factory) {
		assertModifiable();
		super.addConverterFactory(factory);
	}

	@Override
	public void removeConvertible(Class<?> sourceType, Class<?> targetType) {
		assertModifiable();
		super.removeConvertible(sourceType, targetType);
	}

	private void assertModifiable() {
		if (this.unmodifiable) {
			throw new UnsupportedOperationException("This ApplicationConversionService cannot be modified");
		}
	}

	/**
	 * 若 {@code sourceType} 可通过以 {@code Object.class} 为源类型的 {@code ObjectTo...} 转换器
	 * 转为 {@code targetType}，则返回 {@code true}。
	 *
	 * @param sourceType the source type to test 待测源类型
	 * @param targetType the target type to test 待测目标类型
	 * @return if conversion happens through an {@code ObjectTo...} converter 是否经由 ObjectTo 转换器
	 * @since 2.4.3
	 */
	public boolean isConvertViaObjectSourceType(TypeDescriptor sourceType, TypeDescriptor targetType) {
		GenericConverter converter = getConverter(sourceType, targetType);
		Set<ConvertiblePair> pairs = (converter != null) ? converter.getConvertibleTypes() : null;
		if (pairs != null) {
			for (ConvertiblePair pair : pairs) {
				if (Object.class.equals(pair.getSourceType())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 返回共享的默认应用 {@code ConversionService} 实例，按需懒加载构建。
	 * <p>
	 * 注意：实际返回 {@link ApplicationConversionService} 实例；
	 * 为保持二进制兼容，方法签名仍为 {@code ConversionService}。
	 *
	 * @return the shared {@code ApplicationConversionService} instance (never
	 * {@code null}) 共享实例（永不为 {@code null}）
	 */
	public static ConversionService getSharedInstance() {
		ApplicationConversionService sharedInstance = ApplicationConversionService.sharedInstance;
		if (sharedInstance == null) {
			synchronized (ApplicationConversionService.class) {
				sharedInstance = ApplicationConversionService.sharedInstance;
				if (sharedInstance == null) {
					sharedInstance = new ApplicationConversionService(null, true);
					ApplicationConversionService.sharedInstance = sharedInstance;
				}
			}
		}
		return sharedInstance;
	}

	/**
	 * 为给定 {@link FormatterRegistry} 配置适用于大多数 Spring Boot 应用的格式化器与转换器。
	 *
	 * @param registry the registry of converters to add to (must also be castable to
	 * ConversionService, e.g. being a {@link ConfigurableConversionService}) 转换器注册表（须可转为 {@link ConversionService}）
	 * @throws ClassCastException if the given FormatterRegistry could not be cast to a
	 * ConversionService 无法转为 {@link ConversionService} 时
	 */
	public static void configure(FormatterRegistry registry) {
		DefaultConversionService.addDefaultConverters(registry);
		DefaultFormattingConversionService.addDefaultFormatters(registry);
		addApplicationFormatters(registry);
		addApplicationConverters(registry);
	}

	/**
	 * 添加适用于大多数 Spring Boot 应用的转换器。
	 *
	 * @param registry the registry of converters to add to (must also be castable to
	 * ConversionService, e.g. being a {@link ConfigurableConversionService}) 转换器注册表
	 * @throws ClassCastException if the given ConverterRegistry could not be cast to a
	 * ConversionService 无法转为 {@link ConversionService} 时
	 */
	public static void addApplicationConverters(ConverterRegistry registry) {
		addDelimitedStringConverters(registry);
		registry.addConverter(new StringToDurationConverter());
		registry.addConverter(new DurationToStringConverter());
		registry.addConverter(new NumberToDurationConverter());
		registry.addConverter(new DurationToNumberConverter());
		registry.addConverter(new StringToPeriodConverter());
		registry.addConverter(new PeriodToStringConverter());
		registry.addConverter(new NumberToPeriodConverter());
		registry.addConverter(new StringToDataSizeConverter());
		registry.addConverter(new NumberToDataSizeConverter());
		registry.addConverter(new StringToFileConverter());
		registry.addConverter(new InputStreamSourceToByteArrayConverter());
		registry.addConverterFactory(new LenientStringToEnumConverterFactory());
		registry.addConverterFactory(new LenientBooleanToEnumConverterFactory());
		if (registry instanceof ConversionService conversionService) {
			addApplicationConverters(registry, conversionService);
		}
	}

	private static void addApplicationConverters(ConverterRegistry registry, ConversionService conversionService) {
		registry.addConverter(new CharSequenceToObjectConverter(conversionService));
	}

	/**
	 * 添加支持分隔字符串的转换器。
	 *
	 * @param registry the registry of converters to add to (must also be castable to
	 * ConversionService, e.g. being a {@link ConfigurableConversionService}) 转换器注册表
	 * @throws ClassCastException if the given ConverterRegistry could not be cast to a
	 * ConversionService 无法转为 {@link ConversionService} 时
	 */
	public static void addDelimitedStringConverters(ConverterRegistry registry) {
		ConversionService service = (ConversionService) registry;
		registry.addConverter(new ArrayToDelimitedStringConverter(service));
		registry.addConverter(new CollectionToDelimitedStringConverter(service));
		registry.addConverter(new DelimitedStringToArrayConverter(service));
		registry.addConverter(new DelimitedStringToCollectionConverter(service));
	}

	/**
	 * 添加适用于大多数 Spring Boot 应用的格式化器。
	 *
	 * @param registry the service to register default formatters with 格式化器注册表
	 */
	public static void addApplicationFormatters(FormatterRegistry registry) {
		registry.addFormatter(new CharArrayFormatter());
		registry.addFormatter(new InetAddressFormatter());
		registry.addFormatter(new IsoOffsetFormatter());
	}

	/**
	 * 从指定 Bean 工厂注册 {@link Printer}、{@link Parser}、{@link Formatter}、
	 * {@link Converter}、{@link ConverterFactory}、{@link GenericConverter} 等 Bean。
	 *
	 * @param registry the service to register beans with 注册目标
	 * @param beanFactory the bean factory to get the beans from Bean 工厂
	 * @since 2.2.0
	 */
	public static void addBeans(FormatterRegistry registry, ListableBeanFactory beanFactory) {
		addBeans(registry, beanFactory, null);
	}

	/**
	 * 从指定 Bean 工厂注册转换/格式化相关 Bean，可按限定符筛选。
	 *
	 * @param registry the service to register beans with 注册目标
	 * @param beanFactory the bean factory to get the beans from Bean 工厂
	 * @param qualifier the qualifier required on the beans or {@code null} Bean 限定符（可为 {@code null}）
	 * @return the beans that were added 已添加的 Bean
	 * @since 3.5.0
	 */
	public static Map<String, Object> addBeans(FormatterRegistry registry, ListableBeanFactory beanFactory,
			@Nullable String qualifier) {
		ConfigurableListableBeanFactory configurableBeanFactory = getConfigurableListableBeanFactory(beanFactory);
		Map<String, Object> beans = getBeans(beanFactory, qualifier);
		beans.forEach((beanName, bean) -> {
			BeanDefinition beanDefinition = (configurableBeanFactory != null)
					? configurableBeanFactory.getMergedBeanDefinition(beanName) : null;
			ResolvableType type = (beanDefinition != null) ? beanDefinition.getResolvableType() : null;
			addBean(registry, bean, type);
		});
		return beans;
	}

	private static @Nullable ConfigurableListableBeanFactory getConfigurableListableBeanFactory(
			ListableBeanFactory beanFactory) {
		if (beanFactory instanceof ConfigurableApplicationContext applicationContext) {
			return applicationContext.getBeanFactory();
		}
		if (beanFactory instanceof ConfigurableListableBeanFactory configurableListableBeanFactory) {
			return configurableListableBeanFactory;
		}
		return null;
	}

	private static Map<String, Object> getBeans(ListableBeanFactory beanFactory, @Nullable String qualifier) {
		Map<String, Object> beans = new LinkedHashMap<>();
		beans.putAll(getBeans(beanFactory, Printer.class, qualifier));
		beans.putAll(getBeans(beanFactory, Parser.class, qualifier));
		beans.putAll(getBeans(beanFactory, Formatter.class, qualifier));
		beans.putAll(getBeans(beanFactory, Converter.class, qualifier));
		beans.putAll(getBeans(beanFactory, ConverterFactory.class, qualifier));
		beans.putAll(getBeans(beanFactory, GenericConverter.class, qualifier));
		return beans;
	}

	private static <T> Map<String, T> getBeans(ListableBeanFactory beanFactory, Class<T> type,
			@Nullable String qualifier) {
		return (!StringUtils.hasLength(qualifier)) ? beanFactory.getBeansOfType(type)
				: BeanFactoryAnnotationUtils.qualifiedBeansOfType(beanFactory, type, qualifier);
	}

	static void addBean(FormatterRegistry registry, Object bean, @Nullable ResolvableType beanType) {
		if (bean instanceof GenericConverter converterBean) {
			addBean(registry, converterBean, beanType, GenericConverter.class, registry::addConverter, (Runnable) null);
		}
		else if (bean instanceof Converter<?, ?> converterBean) {
			Assert.state(beanType != null, "beanType is missing");
			addBeanWithType(registry, converterBean, beanType, Converter.class, registry::addConverter,
					ConverterBeanAdapter::new);
		}
		else if (bean instanceof ConverterFactory<?, ?> converterBean) {
			Assert.state(beanType != null, "beanType is missing");
			addBeanWithType(registry, converterBean, beanType, ConverterFactory.class, registry::addConverterFactory,
					ConverterFactoryBeanAdapter::new);
		}
		else if (bean instanceof Formatter<?> formatterBean) {
			addBean(registry, formatterBean, beanType, Formatter.class, registry::addFormatter, () -> {
				Assert.state(beanType != null, "beanType is missing");
				registry.addConverter(new PrinterBeanAdapter(formatterBean, beanType));
				registry.addConverter(new ParserBeanAdapter(formatterBean, beanType));
			});
		}
		else if (bean instanceof Printer<?> printerBean) {
			Assert.state(beanType != null, "beanType is missing");
			addBeanWithType(registry, printerBean, beanType, Printer.class, registry::addPrinter,
					PrinterBeanAdapter::new);
		}
		else if (bean instanceof Parser<?> parserBean) {
			Assert.state(beanType != null, "beanType is missing");
			addBeanWithType(registry, parserBean, beanType, Parser.class, registry::addParser, ParserBeanAdapter::new);
		}
	}

	private static <B, T> void addBeanWithType(FormatterRegistry registry, B bean, ResolvableType beanType,
			Class<T> type, Consumer<B> standardRegistrar,
			BiFunction<B, ResolvableType, BeanAdapter<?>> beanAdapterFactory) {
		addBean(registry, bean, beanType, type, standardRegistrar,
				() -> registry.addConverter(beanAdapterFactory.apply(bean, beanType)));
	}

	private static <B, T> void addBean(FormatterRegistry registry, B bean, @Nullable ResolvableType beanType,
			Class<T> type, Consumer<B> standardRegistrar, @Nullable Runnable beanAdapterRegistrar) {
		if (beanType != null && beanAdapterRegistrar != null
				&& ResolvableType.forInstance(bean).as(type).hasUnresolvableGenerics()) {
			beanAdapterRegistrar.run();
			return;
		}
		standardRegistrar.accept(bean);
	}

	/**
	 * 将 Bean 适配为 {@link GenericConverter} 的适配器基类。
	 *
	 * @param <B> the base type of the bean Bean 基类型
	 */
	abstract static class BeanAdapter<B> implements ConditionalGenericConverter {

		private final B bean;

		private final ResolvableTypePair types;

		BeanAdapter(B bean, ResolvableType beanType) {
			Assert.isInstanceOf(beanType.toClass(), bean);
			ResolvableType type = ResolvableType.forClass(getClass()).as(BeanAdapter.class).getGeneric();
			ResolvableType[] generics = beanType.as(type.toClass()).getGenerics();
			this.bean = bean;
			this.types = getResolvableTypePair(generics);
		}

		protected ResolvableTypePair getResolvableTypePair(ResolvableType[] generics) {
			return new ResolvableTypePair(generics[0], generics[1]);
		}

		protected B bean() {
			return this.bean;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Set.of(new ConvertiblePair(this.types.source().toClass(), this.types.target().toClass()));
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			return (this.types.target().toClass() == targetType.getObjectType()
					&& matchesTargetType(targetType.getResolvableType()));
		}

		private boolean matchesTargetType(ResolvableType targetType) {
			ResolvableType ours = this.types.target();
			return targetType.getType() instanceof Class || targetType.isAssignableFrom(ours)
					|| this.types.target().hasUnresolvableGenerics();
		}

		protected final boolean conditionalConverterCandidateMatches(Object conditionalConverterCandidate,
				TypeDescriptor sourceType, TypeDescriptor targetType) {
			return (conditionalConverterCandidate instanceof ConditionalConverter conditionalConverter)
					? conditionalConverter.matches(sourceType, targetType) : true;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected final @Nullable Object convert(@Nullable Object source, TypeDescriptor targetType,
				Converter<?, ?> converter) {
			return (source != null) ? ((Converter) converter).convert(source) : convertNull(targetType);
		}

		private @Nullable Object convertNull(TypeDescriptor targetType) {
			return (targetType.getObjectType() != Optional.class) ? null : Optional.empty();
		}

		@Override
		public String toString() {
			return this.types + " : " + this.bean;
		}

	}

	/**
	 * 将 {@link Printer} Bean 适配为 {@link GenericConverter}。
	 */
	static class PrinterBeanAdapter extends BeanAdapter<Printer<?>> {

		PrinterBeanAdapter(Printer<?> bean, ResolvableType beanType) {
			super(bean, beanType);
		}

		@Override
		protected ResolvableTypePair getResolvableTypePair(ResolvableType[] generics) {
			return new ResolvableTypePair(generics[0], STRING);
		}

		@Override
		public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			return (source != null) ? print(source) : "";
		}

		@SuppressWarnings("unchecked")
		private String print(Object object) {
			return ((Printer<Object>) bean()).print(object, LocaleContextHolder.getLocale());
		}

	}

	/**
	 * 将 {@link Parser} Bean 适配为 {@link GenericConverter}。
	 */
	static class ParserBeanAdapter extends BeanAdapter<Parser<?>> {

		ParserBeanAdapter(Parser<?> bean, ResolvableType beanType) {
			super(bean, beanType);
		}

		@Override
		protected ResolvableTypePair getResolvableTypePair(ResolvableType[] generics) {
			return new ResolvableTypePair(STRING, generics[0]);
		}

		@Override
		public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			String text = (String) source;
			return (!StringUtils.hasText(text)) ? null : parse(text);
		}

		private Object parse(String text) {
			try {
				return bean().parse(text, LocaleContextHolder.getLocale());
			}
			catch (IllegalArgumentException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Parse attempt failed for value [" + text + "]", ex);
			}
		}

	}

	/**
	 * 将 {@link Converter} Bean 适配为 {@link GenericConverter}。
	 */
	static final class ConverterBeanAdapter extends BeanAdapter<Converter<?, ?>> {

		ConverterBeanAdapter(Converter<?, ?> bean, ResolvableType beanType) {
			super(bean, beanType);
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			return super.matches(sourceType, targetType)
					&& conditionalConverterCandidateMatches(bean(), sourceType, targetType);
		}

		@Override
		public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			return convert(source, targetType, bean());
		}

	}

	/**
	 * 将 {@link ConverterFactory} Bean 适配为 {@link GenericConverter}。
	 */
	private static final class ConverterFactoryBeanAdapter extends BeanAdapter<ConverterFactory<?, ?>> {

		ConverterFactoryBeanAdapter(ConverterFactory<?, ?> bean, ResolvableType beanType) {
			super(bean, beanType);
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			return super.matches(sourceType, targetType)
					&& conditionalConverterCandidateMatches(bean(), sourceType, targetType)
					&& conditionalConverterCandidateMatches(getConverter(targetType::getType), sourceType, targetType);
		}

		@Override
		public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			return convert(source, targetType, getConverter(targetType::getObjectType));
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Converter<Object, ?> getConverter(Supplier<Class<?>> typeSupplier) {
			return ((ConverterFactory) bean()).getConverter(typeSupplier.get());
		}

	}

	/**
	 * 从 Bean 泛型提取的可转换类型信息。
	 *
	 * @param source the source type 源类型
	 * @param target the target type 目标类型
	 */
	record ResolvableTypePair(ResolvableType source, ResolvableType target) {

		ResolvableTypePair {
			Assert.notNull(source.resolve(), "'source' cannot be resolved");
			Assert.notNull(target.resolve(), "'target' cannot be resolved");
		}

		@Override
		public final String toString() {
			return source() + " -> " + target();
		}

	}

}
