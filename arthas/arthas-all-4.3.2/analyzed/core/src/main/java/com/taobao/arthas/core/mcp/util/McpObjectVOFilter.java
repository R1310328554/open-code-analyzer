package com.taobao.arthas.core.mcp.util;

import com.alibaba.fastjson2.filter.ValueFilter;
import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.view.ObjectView;
import com.taobao.arthas.mcp.server.util.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP 场景下 {@link ObjectVO} 的 Fastjson2 序列化过滤器。
 * <p>
 * 命令结果经 {@link JsonParser} 输出 JSON 时，将 ObjectVO 展开为文本或 JSON 视图，
 * 避免 MCP 客户端收到不可读的对象包装结构。
 */
public class McpObjectVOFilter implements ValueFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(McpObjectVOFilter.class);
    
    private static final McpObjectVOFilter INSTANCE = new McpObjectVOFilter();
    private static volatile boolean registered = false;
    
    /**
     * 向全局 {@link JsonParser} 注册本过滤器（双重检查锁，仅注册一次）。
     */
    public static void register() {
        if (!registered) {
            synchronized (McpObjectVOFilter.class) {
                if (!registered) {
                    JsonParser.registerFilter(INSTANCE);
                    registered = true;
                    logger.debug("McpObjectVOFilter registered to JsonParser");
                }
            }
        }
    }
    
    @Override
    public Object apply(Object object, String name, Object value) {
        if (value == null) {
            return null;
        }
        
        // 直接 instanceof 判断，避免反射开销
        if (value instanceof ObjectVO) {
            return handleObjectVO((ObjectVO) value);
        }
        
        return value;
    }

    /** 根据 expand 标志与 {@link GlobalOptions#isUsingJson} 选择展开或 toString */
    private Object handleObjectVO(ObjectVO objectVO) {
        try {
            Object innerObject = objectVO.getObject();
            Integer expand = objectVO.getExpand();
            
            if (innerObject == null) {
                return "null";
            }

            if (objectVO.needExpand()) {
                // 根据 GlobalOptions.isUsingJson 配置决定输出格式
                if (GlobalOptions.isUsingJson) {
                    return drawJsonView(innerObject);
                } else {
                    return drawObjectView(objectVO);
                }
            } else {
                return objectToString(innerObject);
            }
        } catch (Exception e) {
            logger.warn("Failed to handle ObjectVO: {}", e.getMessage());
            return "{\"error\":\"ObjectVO serialization failed\"}";
        }
    }

    /**
     * 使用 ObjectView 输出对象结构
     */
    private String drawObjectView(ObjectVO objectVO) {
        try {
            ObjectView objectView = new ObjectView(objectVO);
            return objectView.draw();
        } catch (Exception e) {
            logger.debug("ObjectView serialization failed, using toString: {}", e.getMessage());
            return objectToString(objectVO.getObject());
        }
    }

    /**
     * 使用 JSON 格式输出对象
     */
    private String drawJsonView(Object object) {
        try {
            return ObjectView.toJsonString(object);
        } catch (Exception e) {
            logger.debug("ObjectView-style serialization failed, using toString: {}", e.getMessage());
            return objectToString(object);
        }
    }

    /** 安全 toString；异常时返回 类名@hashCode 占位 */
    private String objectToString(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            return object.toString();
        } catch (Exception e) {
            return object.getClass().getSimpleName() + "@" + Integer.toHexString(object.hashCode());
        }
    }
}
