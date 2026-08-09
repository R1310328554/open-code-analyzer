/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * 为通知与结果注入 {@code _meta.relatedTask} 元数据的工具类。
 *
 * @author Yeaury
 */
public final class TaskMetadataUtils {

    private TaskMetadataUtils() {}

    /**
     * 向通知对象注入 {@code _meta.relatedTask.taskId}。
     * {@link McpSchema.TaskStatusNotification} 已自带 taskId，原样返回。
     */
    @SuppressWarnings("unchecked")
    public static Object addRelatedTaskMetadata(String taskId, Object notification) {
        if (notification == null || taskId == null) {
            return notification;
        }
        if (notification instanceof McpSchema.TaskStatusNotification) {
            return notification;
        }
        if (notification instanceof Map) {
            Map<String, Object> notifMap = new HashMap<>((Map<String, Object>) notification);
            Map<String, Object> meta = notifMap.containsKey("_meta") && notifMap.get("_meta") instanceof Map
                    ? new HashMap<>((Map<String, Object>) notifMap.get("_meta"))
                    : new HashMap<>();
            Map<String, Object> relatedTask = new HashMap<>();
            relatedTask.put("taskId", taskId);
            meta.put(McpSchema.RELATED_TASK_META_KEY, relatedTask);
            notifMap.put("_meta", meta);
            return notifMap;
        }
        return notification;
    }

    /** 将 {@code relatedTask: {taskId}} 合并到新 metadata 映射，覆盖同名字段。 */
    public static Map<String, Object> mergeRelatedTaskMetadata(String taskId, Map<String, Object> existingMeta) {
        Map<String, Object> taskIdMap = new HashMap<>();
        taskIdMap.put("taskId", taskId);
        return mergeRelatedTaskMetadata((Object) taskIdMap, existingMeta);
    }

    /** 将 {@code relatedTask: relatedTaskValue} 合并到新 metadata 映射，覆盖同名字段。 */
    public static Map<String, Object> mergeRelatedTaskMetadata(Object relatedTaskValue,
                                                                 Map<String, Object> existingMeta) {
        Map<String, Object> newMeta = new HashMap<>();
        newMeta.put(McpSchema.RELATED_TASK_META_KEY, relatedTaskValue);
        if (existingMeta != null) {
            newMeta.putAll(existingMeta);
        }
        return newMeta;
    }
}
