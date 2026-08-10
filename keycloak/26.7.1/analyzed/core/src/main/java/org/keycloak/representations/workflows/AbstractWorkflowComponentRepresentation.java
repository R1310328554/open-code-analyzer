package org.keycloak.representations.workflows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.reflections.Reflections;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.keycloak.common.util.reflections.Reflections.isArrayType;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_WITH;

/**
 * 工作流组件表示的抽象基类，提供标识符与多值配置 Map 的通用读写能力。
 */
public abstract class AbstractWorkflowComponentRepresentation {

    /** 组件唯一标识。 */
    private String id;

    /** 组件配置项（键对应多值字符串列表）。 */
    @JsonProperty(CONFIG_WITH)
    private MultivaluedHashMap<String, String> config;

    /**
     * 以标识符与初始配置构造组件。
     *
     * @param id     组件标识
     * @param config 配置 Map
     */
    public AbstractWorkflowComponentRepresentation(String id, MultivaluedHashMap<String, String> config) {
        this.id = id;
        this.setConfig(config);
    }

    /** @return 组件标识 */
    public String getId() {
        return id;
    }

    /** @param id 组件标识 */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 配置 Map */
    public MultivaluedHashMap<String, String> getConfig() {
        return config;
    }

    /**
     * 合并写入完整配置 Map（与现有条目合并）。
     *
     * @param config 待合并的配置
     */
    public void setConfig(MultivaluedHashMap<String, String> config) {
        if (config != null) {
            if (this.config == null) {
                this.config = new MultivaluedHashMap<>();
            }
            this.config.putAll(config);
        }
    }

    /**
     * 设置单值配置项。
     *
     * @param key   配置键
     * @param value 配置值
     */
    public void setConfig(String key, String value) {
        setConfig(key, Collections.singletonList(value));
    }

    /**
     * 设置多值配置项；Jackson 反序列化时也会通过 {@link JsonAnySetter} 调用此方法。
     *
     * @param key    配置键
     * @param values 配置值列表
     */
    @JsonAnySetter
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public void setConfig(String key, List<String> values) {
        if (this.config == null) {
            this.config = new MultivaluedHashMap<>();
        }
        this.config.put(key, values);
    }

    /**
     * 读取并转换首个配置值为指定类型。
     *
     * @param key  配置键
     * @param type 目标类型
     * @param <T>  值类型
     * @return 转换后的值，不存在时返回 {@code null}
     */
    protected <T> T getConfigValue(String key, Class<T> type) {
        if (config == null) {
            return null;
        }

        return Reflections.convertValueToType(config.getFirst(key), type);
    }

    /**
     * 读取配置键对应的全部字符串值。
     *
     * @param key 配置键
     * @return 值列表，不存在时返回 {@code null}
     */
    protected List<String> getConfigValues(String key) {
        if (config == null) {
            return null;
        }

        try {
            return config.get(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 写入配置值，支持单值或数组形式。
     *
     * @param key    配置键
     * @param values 一个或多个值
     */
    protected void setConfigValue(String key, Object... values) {
        if (values == null || values.length == 0) {
            return;
        }

        if (this.config == null) {
            this.config = new MultivaluedHashMap<>();
        }

        if (isArrayType(values.getClass())) {
            this.config.put(key, Arrays.stream(values).filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList()));
        } else {
            this.config.putSingle(key, values[0].toString());
        }
    }

    /**
     * 写入多值配置项。
     *
     * @param key    配置键
     * @param values 值列表
     */
    protected void setConfigValue(String key, List<String> values) {
        if (values == null) {
            return;
        }
        if (this.config == null) {
            this.config = new MultivaluedHashMap<>();
        }
        this.config.put(key, values);
    }

    /**
     * 向配置键追加一个值（不覆盖已有条目）。
     *
     * @param key   配置键
     * @param value 追加的值
     */
    protected void addConfigValue(String key, String value) {
        if (value == null) {
            return;
        }
        if (this.config == null) {
            this.config = new MultivaluedHashMap<>();
        }

        this.config.add(key, value);
    }
}
