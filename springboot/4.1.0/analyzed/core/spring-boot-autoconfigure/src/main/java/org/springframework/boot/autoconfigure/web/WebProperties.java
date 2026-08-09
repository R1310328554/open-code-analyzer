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

package org.springframework.boot.autoconfigure.web;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.http.CacheControl;

/**
 * 通用 Web 相关事项的 {@link ConfigurationProperties 配置属性}。
 *
 * @author Andy Wilkinson
 * @since 2.4.0
 */
@ConfigurationProperties("spring.web")
public class WebProperties {

	/**
	 * 要使用的区域设置。默认情况下会被 {@code Accept-Language} 请求头覆盖。
	 */
	private @Nullable Locale locale;

	/**
	 * 定义应如何解析区域设置。
	 */
	private LocaleResolver localeResolver = LocaleResolver.ACCEPT_HEADER;

	private final Resources resources = new Resources();

	@NestedConfigurationProperty
	private final ErrorProperties error = new ErrorProperties();

	public @Nullable Locale getLocale() {
		return this.locale;
	}

	public void setLocale(@Nullable Locale locale) {
		this.locale = locale;
	}

	public LocaleResolver getLocaleResolver() {
		return this.localeResolver;
	}

	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	public ErrorProperties getError() {
		return this.error;
	}

	public Resources getResources() {
		return this.resources;
	}

	public enum LocaleResolver {

		/**
		 * 始终使用配置的区域设置。
		 */
		FIXED,

		/**
		 * 使用 {@code Accept-Language} 请求头；若未设置请求头则使用配置的区域设置。
		 */
		ACCEPT_HEADER

	}

	public static class Resources {

		private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/resources/",
				"classpath:/resources/", "classpath:/static/", "classpath:/public/" };

		/**
		 * 静态资源位置。默认为 classpath:[/META-INF/resources/、/resources/、/static/、/public/]。
		 */
		private String[] staticLocations = CLASSPATH_RESOURCE_LOCATIONS;

		/**
		 * 是否启用默认资源处理。
		 */
		private boolean addMappings = true;

		private boolean customized;

		private final Chain chain = new Chain();

		private final Cache cache = new Cache();

		public String[] getStaticLocations() {
			return this.staticLocations;
		}

		public void setStaticLocations(String[] staticLocations) {
			this.staticLocations = appendSlashIfNecessary(staticLocations);
			this.customized = true;
		}

		private String[] appendSlashIfNecessary(String[] staticLocations) {
			String[] normalized = new String[staticLocations.length];
			for (int i = 0; i < staticLocations.length; i++) {
				String location = staticLocations[i];
				normalized[i] = location.endsWith("/") ? location : location + "/";
			}
			return normalized;
		}

		public boolean isAddMappings() {
			return this.addMappings;
		}

		public void setAddMappings(boolean addMappings) {
			this.customized = true;
			this.addMappings = addMappings;
		}

		public Chain getChain() {
			return this.chain;
		}

		public Cache getCache() {
			return this.cache;
		}

		public boolean hasBeenCustomized() {
			return this.customized || getChain().hasBeenCustomized() || getCache().hasBeenCustomized();
		}

		/**
		 * Spring 资源处理链的配置。
		 */
		public static class Chain {

			boolean customized;

			/**
			 * 是否启用 Spring 资源处理链。默认禁用，除非至少启用一种策略。
			 */
			private @Nullable Boolean enabled;

			/**
			 * 是否在资源链中启用缓存。
			 */
			private boolean cache = true;

			/**
			 * 是否启用对已压缩资源（gzip、brotli）的解析。
			 * 检查带有 {@code .gz} 或 {@code .br} 扩展名的资源名。
			 */
			private boolean compressed;

			private final Strategy strategy = new Strategy();

			/**
			 * 返回资源链是否启用。若无特定设置则返回 {@code null}。
			 * @return 资源链是否启用；无指定设置时返回 {@code null}
			 */
			public @Nullable Boolean getEnabled() {
				return getEnabled(getStrategy().getFixed().isEnabled(), getStrategy().getContent().isEnabled(),
						this.enabled);
			}

			private boolean hasBeenCustomized() {
				return this.customized || getStrategy().hasBeenCustomized();
			}

			public void setEnabled(Boolean enabled) {
				this.enabled = enabled;
				this.customized = true;
			}

			public boolean isCache() {
				return this.cache;
			}

			public void setCache(boolean cache) {
				this.cache = cache;
				this.customized = true;
			}

			public Strategy getStrategy() {
				return this.strategy;
			}

			public boolean isCompressed() {
				return this.compressed;
			}

			public void setCompressed(boolean compressed) {
				this.compressed = compressed;
				this.customized = true;
			}

			static @Nullable Boolean getEnabled(boolean fixedEnabled, boolean contentEnabled,
					@Nullable Boolean chainEnabled) {
				return (fixedEnabled || contentEnabled) ? Boolean.TRUE : chainEnabled;
			}

			/**
			 * 在资源 URL 路径中提取并嵌入版本号的策略。
			 */
			public static class Strategy {

				private final Fixed fixed = new Fixed();

				private final Content content = new Content();

				public Fixed getFixed() {
					return this.fixed;
				}

				public Content getContent() {
					return this.content;
				}

				private boolean hasBeenCustomized() {
					return getFixed().hasBeenCustomized() || getContent().hasBeenCustomized();
				}

				/**
				 * 基于内容哈希的版本策略。
				 */
				public static class Content {

					private boolean customized;

					/**
					 * 是否启用内容版本策略。
					 */
					private boolean enabled;

					/**
					 * 应用于内容版本策略的模式列表。
					 */
					private String[] paths = new String[] { "/**" };

					public boolean isEnabled() {
						return this.enabled;
					}

					public void setEnabled(boolean enabled) {
						this.customized = true;
						this.enabled = enabled;
					}

					public String[] getPaths() {
						return this.paths;
					}

					public void setPaths(String[] paths) {
						this.customized = true;
						this.paths = paths;
					}

					private boolean hasBeenCustomized() {
						return this.customized;
					}

				}

				/**
				 * 基于固定版本字符串的版本策略。
				 */
				public static class Fixed {

					private boolean customized;

					/**
					 * 是否启用固定版本策略。
					 */
					private boolean enabled;

					/**
					 * 应用于固定版本策略的模式列表。
					 */
					private String[] paths = new String[] { "/**" };

					/**
					 * 固定版本策略使用的版本字符串。
					 */
					private @Nullable String version;

					public boolean isEnabled() {
						return this.enabled;
					}

					public void setEnabled(boolean enabled) {
						this.customized = true;
						this.enabled = enabled;
					}

					public String[] getPaths() {
						return this.paths;
					}

					public void setPaths(String[] paths) {
						this.customized = true;
						this.paths = paths;
					}

					public @Nullable String getVersion() {
						return this.version;
					}

					public void setVersion(@Nullable String version) {
						this.customized = true;
						this.version = version;
					}

					private boolean hasBeenCustomized() {
						return this.customized;
					}

				}

			}

		}

		/**
		 * 缓存配置。
		 */
		public static class Cache {

			private boolean customized;

			/**
			 * 资源处理器所提供资源的缓存周期。未指定持续时间后缀时使用秒。
			 * 可被 {@code spring.web.resources.cache.cachecontrol} 属性覆盖。
			 */
			@DurationUnit(ChronoUnit.SECONDS)
			private @Nullable Duration period;

			/**
			 * 缓存控制 HTTP 头，仅允许有效的指令组合。
			 * 覆盖 {@code spring.web.resources.cache.period} 属性。
			 */
			private final Cachecontrol cachecontrol = new Cachecontrol();

			/**
			 * 是否在 HTTP 缓存头中使用文件的 {@code lastModified} 元数据。
			 */
			private boolean useLastModified = true;

			public @Nullable Duration getPeriod() {
				return this.period;
			}

			public void setPeriod(@Nullable Duration period) {
				this.customized = true;
				this.period = period;
			}

			public Cachecontrol getCachecontrol() {
				return this.cachecontrol;
			}

			public boolean isUseLastModified() {
				return this.useLastModified;
			}

			public void setUseLastModified(boolean useLastModified) {
				this.useLastModified = useLastModified;
			}

			private boolean hasBeenCustomized() {
				return this.customized || getCachecontrol().hasBeenCustomized();
			}

			/**
			 * 缓存控制 HTTP 头配置。
			 */
			public static class Cachecontrol {

				private boolean customized;

				/**
				 * 响应应被缓存的最长时间；未指定持续时间后缀时使用秒。
				 */
				@DurationUnit(ChronoUnit.SECONDS)
				private @Nullable Duration maxAge;

				/**
				 * 指示缓存响应仅在与服务器重新验证后才可复用。
				 */
				private @Nullable Boolean noCache;

				/**
				 * 指示在任何情况下都不缓存响应。
				 */
				private @Nullable Boolean noStore;

				/**
				 * 指示缓存过期后，未经与服务器重新验证不得使用该响应。
				 */
				private @Nullable Boolean mustRevalidate;

				/**
				 * 指示中间节点（缓存等）不得转换响应内容。
				 */
				private @Nullable Boolean noTransform;

				/**
				 * 指示任何缓存均可存储该响应。
				 */
				private @Nullable Boolean cachePublic;

				/**
				 * 指示响应消息仅供单个用户使用，共享缓存不得存储。
				 */
				private @Nullable Boolean cachePrivate;

				/**
				 * 与 {@code must-revalidate} 指令含义相同，但不适用于私有缓存。
				 */
				private @Nullable Boolean proxyRevalidate;

				/**
				 * 响应过期后仍可提供的最大时间；未指定持续时间后缀时使用秒。
				 */
				@DurationUnit(ChronoUnit.SECONDS)
				private @Nullable Duration staleWhileRevalidate;

				/**
				 * 遇到错误时响应仍可使用的最大时间；未指定持续时间后缀时使用秒。
				 */
				@DurationUnit(ChronoUnit.SECONDS)
				private @Nullable Duration staleIfError;

				/**
				 * 共享缓存应缓存响应的最长时间；未指定持续时间后缀时使用秒。
				 */
				@DurationUnit(ChronoUnit.SECONDS)
				private @Nullable Duration sMaxAge;

				public @Nullable Duration getMaxAge() {
					return this.maxAge;
				}

				public void setMaxAge(@Nullable Duration maxAge) {
					this.customized = true;
					this.maxAge = maxAge;
				}

				public @Nullable Boolean getNoCache() {
					return this.noCache;
				}

				public void setNoCache(@Nullable Boolean noCache) {
					this.customized = true;
					this.noCache = noCache;
				}

				public @Nullable Boolean getNoStore() {
					return this.noStore;
				}

				public void setNoStore(@Nullable Boolean noStore) {
					this.customized = true;
					this.noStore = noStore;
				}

				public @Nullable Boolean getMustRevalidate() {
					return this.mustRevalidate;
				}

				public void setMustRevalidate(@Nullable Boolean mustRevalidate) {
					this.customized = true;
					this.mustRevalidate = mustRevalidate;
				}

				public @Nullable Boolean getNoTransform() {
					return this.noTransform;
				}

				public void setNoTransform(@Nullable Boolean noTransform) {
					this.customized = true;
					this.noTransform = noTransform;
				}

				public @Nullable Boolean getCachePublic() {
					return this.cachePublic;
				}

				public void setCachePublic(@Nullable Boolean cachePublic) {
					this.customized = true;
					this.cachePublic = cachePublic;
				}

				public @Nullable Boolean getCachePrivate() {
					return this.cachePrivate;
				}

				public void setCachePrivate(@Nullable Boolean cachePrivate) {
					this.customized = true;
					this.cachePrivate = cachePrivate;
				}

				public @Nullable Boolean getProxyRevalidate() {
					return this.proxyRevalidate;
				}

				public void setProxyRevalidate(@Nullable Boolean proxyRevalidate) {
					this.customized = true;
					this.proxyRevalidate = proxyRevalidate;
				}

				public @Nullable Duration getStaleWhileRevalidate() {
					return this.staleWhileRevalidate;
				}

				public void setStaleWhileRevalidate(@Nullable Duration staleWhileRevalidate) {
					this.customized = true;
					this.staleWhileRevalidate = staleWhileRevalidate;
				}

				public @Nullable Duration getStaleIfError() {
					return this.staleIfError;
				}

				public void setStaleIfError(@Nullable Duration staleIfError) {
					this.customized = true;
					this.staleIfError = staleIfError;
				}

				public @Nullable Duration getSMaxAge() {
					return this.sMaxAge;
				}

				public void setSMaxAge(@Nullable Duration sMaxAge) {
					this.customized = true;
					this.sMaxAge = sMaxAge;
				}

				public @Nullable CacheControl toHttpCacheControl() {
					PropertyMapper map = PropertyMapper.get();
					CacheControl control = createCacheControl();
					map.from(this::getMustRevalidate).whenTrue().toCall(control::mustRevalidate);
					map.from(this::getNoTransform).whenTrue().toCall(control::noTransform);
					map.from(this::getCachePublic).whenTrue().toCall(control::cachePublic);
					map.from(this::getCachePrivate).whenTrue().toCall(control::cachePrivate);
					map.from(this::getProxyRevalidate).whenTrue().toCall(control::proxyRevalidate);
					map.from(this::getStaleWhileRevalidate)
						.to((duration) -> control.staleWhileRevalidate(duration.getSeconds(), TimeUnit.SECONDS));
					map.from(this::getStaleIfError)
						.to((duration) -> control.staleIfError(duration.getSeconds(), TimeUnit.SECONDS));
					map.from(this::getSMaxAge)
						.to((duration) -> control.sMaxAge(duration.getSeconds(), TimeUnit.SECONDS));
					// check if cacheControl remained untouched
					if (control.getHeaderValue() == null) {
						return null;
					}
					return control;
				}

				private CacheControl createCacheControl() {
					if (Boolean.TRUE.equals(this.noStore)) {
						return CacheControl.noStore();
					}
					if (Boolean.TRUE.equals(this.noCache)) {
						return CacheControl.noCache();
					}
					if (this.maxAge != null) {
						return CacheControl.maxAge(this.maxAge.getSeconds(), TimeUnit.SECONDS);
					}
					return CacheControl.empty();
				}

				private boolean hasBeenCustomized() {
					return this.customized;
				}

			}

		}

	}

}
