package com.taobao.arthas.mcp.server.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * JSON 与 Java 对象互转的工具类。
 * <p>
 * 优先使用 FastJSON2 序列化/反序列化，失败时回退到 Jackson；支持注册自定义 {@link ValueFilter}。
 */
public final class JsonParser {

	private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
	/** 全局共享的 Jackson ObjectMapper，已配置 JSR-310 与宽松反序列化。 */
	private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
	/** 线程安全的自定义 JSON 值过滤器列表。 */
	private static final List<ValueFilter> JSON_FILTERS = new CopyOnWriteArrayList<>();

	/**
	 * 注册自定义 JSON 值过滤器，序列化时生效。
	 * @param filter 过滤器实例，null 则忽略
	 */
	public static void registerFilter(ValueFilter filter) {
		if (filter != null) {
			JSON_FILTERS.add(filter);
		}
	}

	/** 清除所有已注册的 JSON 值过滤器。 */
	public static void clearFilters() {
		JSON_FILTERS.clear();
	}


	/** 创建并配置默认 ObjectMapper：忽略未知属性、支持 Java 8 时间类型。 */
	private static ObjectMapper createObjectMapper() {
		return JsonMapper.builder()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.addModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.build();
	}

	private JsonParser() {
	}

	/** 返回全局共享的 Jackson ObjectMapper 实例。 */
	public static ObjectMapper getObjectMapper() {
		return OBJECT_MAPPER;
	}

	/**
	 * 将 JSON 字符串反序列化为指定 Class 类型对象。
	 * @param json JSON 字符串
	 * @param type 目标类型
	 */
	public static <T> T fromJson(String json, Class<T> type) {
		Assert.notNull(json, "json cannot be null");
		Assert.notNull(type, "type cannot be null");

		try {
			return JSON.parseObject(json, type);
		}
		catch (Exception ex) {
			try {
				return OBJECT_MAPPER.readValue(json, type);
			} catch (JsonProcessingException jacksonEx) {
				throw new IllegalStateException("Conversion from JSON to " + type.getName() + " failed", ex);
			}
		}
	}

	/**
	 * 将 JSON 字符串反序列化为指定 {@link Type} 对象（支持泛型）。
	 * @param json JSON 字符串
	 * @param type 目标反射类型
	 */
	public static <T> T fromJson(String json, Type type) {
		Assert.notNull(json, "json cannot be null");
		Assert.notNull(type, "type cannot be null");

		try {
			return JSON.parseObject(json, type);
		}
		catch (Exception ex) {
			try {
				return OBJECT_MAPPER.readValue(json, OBJECT_MAPPER.constructType(type));
			} catch (JsonProcessingException jacksonEx) {
				throw new IllegalStateException("Conversion from JSON to " + type.getTypeName() + " failed", ex);
			}
		}
	}

	/**
	 * 通过 Jackson {@link TypeReference} 反序列化 JSON（适用于复杂泛型）。
	 * @param json JSON 字符串
	 * @param type 类型引用
	 */
	public static <T> T fromJson(String json, TypeReference<T> type) {
		Assert.notNull(json, "json cannot be null");
		Assert.notNull(type, "type cannot be null");

		try {
			return OBJECT_MAPPER.readValue(json, type);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalStateException("Conversion from JSON to " + type.getType().getTypeName() + " failed",
					ex);
		}
	}

	/**
	 * 将 Java 对象序列化为 JSON 字符串。
	 * <p>
	 * null 返回字面量 {@code "null"}；FastJSON2 失败时回退 Jackson。
	 */
	public static String toJson(Object object) {
		if (object == null) {
			return "null";
		}

		try {
			String result;
			if (JSON_FILTERS.isEmpty()) {
				result = JSON.toJSONString(object);
			} else {
				result = JSON.toJSONString(object, JSON_FILTERS.toArray(new ValueFilter[0]));
			}
			return (result != null) ? result : "{}";
		}
		catch (Exception ex) {
			logger.warn("FastJSON2 with MCP filter serialization failed for {}, falling back to Jackson: {}",
				object.getClass().getSimpleName(), ex.getMessage());
			try {
				String result = OBJECT_MAPPER.writeValueAsString(object);
				return (result != null) ? result : "{}";
			} catch (JsonProcessingException jacksonEx) {
				logger.error("Both FastJSON2 and Jackson serialization failed", ex);
				return "{\"error\":\"Serialization failed\"}";
			}
		}
	}

	/**
	 * 将任意值按目标类型进行强类型转换（基本类型、枚举或 JSON 往返）。
	 * @param value 原始值
	 * @param type 目标类型
	 */
	public static Object toTypedObject(Object value, Class<?> type) {
		if (value == null) {
			throw new IllegalArgumentException("value cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}

		Class<?> javaType = resolvePrimitiveIfNecessary(type);

		if (javaType == String.class) {
			return value.toString();
		}
		else if (javaType == Byte.class) {
			return Byte.parseByte(value.toString());
		}
		else if (javaType == Integer.class) {
			BigDecimal bigDecimal = new BigDecimal(value.toString());
			return bigDecimal.intValueExact();
		}
		else if (javaType == Short.class) {
			return Short.parseShort(value.toString());
		}
		else if (javaType == Long.class) {
			BigDecimal bigDecimal = new BigDecimal(value.toString());
			return bigDecimal.longValueExact();
		}
		else if (javaType == Double.class) {
			return Double.parseDouble(value.toString());
		}
		else if (javaType == Float.class) {
			return Float.parseFloat(value.toString());
		}
		else if (javaType == Boolean.class) {
			return Boolean.parseBoolean(value.toString());
		}
		else if (javaType == Character.class) {
			String s = value.toString();
			if (s.length() == 1) {
				return s.charAt(0);
			}
			throw new IllegalArgumentException("Cannot convert to char: " + value);
		}
		else if (javaType.isEnum()) {
			@SuppressWarnings("unchecked")
			Class<Enum> enumType = (Class<Enum>) javaType;
			return Enum.valueOf(enumType, value.toString());
		}


		// 复杂类型：先序列化为 JSON 再反序列化
		String json = JsonParser.toJson(value);
		return JsonParser.fromJson(json, javaType);
	}

	/**
	 * 若 type 为基本类型，返回对应的包装类；否则原样返回。
	 * @param type 待解析的类型
	 */
	public static Class<?> resolvePrimitiveIfNecessary(Class<?> type) {
		if (type.isPrimitive()) {
			return Utils.getWrapperClassForPrimitive(type);
		}
		return type;
	}

}
