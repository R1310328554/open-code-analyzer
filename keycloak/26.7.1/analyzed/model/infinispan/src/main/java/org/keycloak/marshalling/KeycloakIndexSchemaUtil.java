/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.marshalling;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.infinispan.api.annotations.indexing.model.Values;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.commons.internal.InternalCacheNames;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.config.Configuration;
import org.infinispan.protostream.descriptors.AnnotationElement;
import org.infinispan.protostream.descriptors.Descriptor;
import org.infinispan.protostream.descriptors.FieldDescriptor;
import org.infinispan.protostream.descriptors.FileDescriptor;
import org.infinispan.protostream.impl.AnnotatedDescriptorImpl;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.jboss.logging.Logger;

/**
 * Keycloak ProtoStream 索引模式工具类。
 * <p>
 * 负责将 GeneratedSchema 上传到 Infinispan 集群、检测索引注解变更并触发缓存重建索引。
 */
public class KeycloakIndexSchemaUtil {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    // Basic 注解相关常量
    private static final String BASIC_ANNOTATION = "Basic";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String SEARCHABLE_ATTRIBUTE = "searchable";
    private static final String PROJECTABLE_ATTRIBUTE = "projectable";
    private static final String AGGREGABLE_ATTRIBUTE = "aggregable";
    private static final String SORTABLE_ATTRIBUTE = "sortable";
    private static final String INDEX_NULL_AS_ATTRIBUTE = "indexNullAs";

    // 当前仅使用 Basic 注解，后续可能扩展其他索引注解
    private static final List<String> INDEX_ANNOTATION = List.of(BASIC_ANNOTATION);

    /**
     * 将 {@link GeneratedSchema} 上传到 Infinispan 集群。
     * <p>
     * 若 schema 中包含已启用索引的实体，可传入实体及其所在缓存列表；
     * 方法会更新索引 schema 并对受影响缓存执行重建索引（可能代价较高）。
     *
     * @param remoteCacheManager 已连接 Infinispan 服务器的 {@link RemoteCacheManager}
     * @param schema             待上传的 {@link GeneratedSchema} 实例
     * @param indexedEntities    索引实体及其缓存列表（同一实体可出现在多个缓存中）
     * @throws NullPointerException 当 {@code remoteCacheManager} 或 {@code schema} 为 null 时
     */
    public static void uploadAndReindexCaches(RemoteCacheManager remoteCacheManager, GeneratedSchema schema, List<IndexedEntity> indexedEntities) {
        var key = schema.getProtoFileName();
        var current = schema.getProtoFile();

        var protostreamMetadataCache = remoteCacheManager.<String, String>getCache(InternalCacheNames.PROTOBUF_METADATA_CACHE_NAME);
        var stored = protostreamMetadataCache.getWithMetadata(key);
        if (stored == null) {
            if (protostreamMetadataCache.putIfAbsent(key, current) == null) {
                logger.info("Infinispan ProtoStream schema uploaded for the first time.");
            } else {
                logger.info("Failed to update Infinispan ProtoStream schema. Assumed it was updated by other Keycloak server.");
            }
            checkForProtoSchemaErrors(protostreamMetadataCache);
            return;
        }
        if (Objects.equals(stored.getValue(), current)) {
            logger.info("Infinispan ProtoStream schema is up to date!");
            return;
        }
        if (protostreamMetadataCache.replaceWithVersion(key, current, stored.getVersion())) {
            logger.info("Infinispan ProtoStream schema successful updated.");
            reindexCaches(remoteCacheManager, stored.getValue(), current, indexedEntities);
        } else {
            logger.info("Failed to update Infinispan ProtoStream schema. Assumed it was updated by other Keycloak server.");
        }
        checkForProtoSchemaErrors(protostreamMetadataCache);
    }

    /** 检查 protobuf 元数据缓存中的 schema 解析错误并记录日志。 */
    private static void checkForProtoSchemaErrors(RemoteCache<String, String> protostreamMetadataCache) {
        var errors = protostreamMetadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
        if (errors == null) {
            return;
        }
        for (String errorFile : errors.split("\n")) {
            logger.errorf("%nThere was an error in proto file: %s%nError message: %s%nCurrent proto schema: %s%n",
                    errorFile,
                    protostreamMetadataCache.get(errorFile + ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX),
                    protostreamMetadataCache.get(errorFile));
        }
    }

    /** 对索引 schema 发生变更的实体所在缓存执行 schema 更新与重建索引。 */
    private static void reindexCaches(RemoteCacheManager remoteCacheManager, String oldSchema, String newSchema, List<IndexedEntity> indexedEntities) {
        if (indexedEntities == null || indexedEntities.isEmpty()) {
            return;
        }
        var oldPS = KeycloakModelSchema.parseProtoSchema(oldSchema);
        var newPS = KeycloakModelSchema.parseProtoSchema(newSchema);
        var admin = remoteCacheManager.administration();

        indexedEntities.stream()
                .filter(Objects::nonNull)
                .filter(indexedEntity -> isEntityChanged(oldPS, newPS, indexedEntity.entity()))
                .map(IndexedEntity::cache)
                .distinct()
                .forEach(cacheName -> updateSchemaAndReIndexCache(admin, cacheName));
    }

    /** 判断指定实体在新旧 schema 间是否存在索引相关差异。 */
    private static boolean isEntityChanged(FileDescriptor oldSchema, FileDescriptor newSchema, String entity) {
        var v1 = KeycloakModelSchema.findEntity(oldSchema, entity);
        var v2 = KeycloakModelSchema.findEntity(newSchema, entity);
        return v1.isPresent() && v2.isPresent() && KeycloakIndexSchemaUtil.isIndexSchemaChanged(v1.get(), v2.get());
    }

    /** 更新单个缓存的索引 schema 并触发重建索引。 */
    private static void updateSchemaAndReIndexCache(RemoteCacheManagerAdmin admin, String cacheName) {
        admin.updateIndexSchema(cacheName);
        try {
            admin.reindexCache(cacheName);
        } catch (Exception e) {
            logger.warnf(e, "Exception while waiting for the re-index of cache '%s'. While re-indexing is in progress, the query results may not be accurate.", cacheName);
        }
    }

    /**
     * 向 ProtoStream 解析器注册索引相关注解处理器配置。
     */
    public static void configureAnnotationProcessor(Configuration.Builder builder) {
        //TODO 未来可能移除？
        builder.annotationsConfig()
                .annotation(BASIC_ANNOTATION, AnnotationElement.AnnotationTarget.FIELD)
                .attribute(NAME_ATTRIBUTE)
                .type(AnnotationElement.AttributeType.STRING)
                .defaultValue("")
                .attribute(SEARCHABLE_ATTRIBUTE)
                .type(AnnotationElement.AttributeType.BOOLEAN)
                .defaultValue(true)
                .attribute(PROJECTABLE_ATTRIBUTE)
                .type(AnnotationElement.AttributeType.BOOLEAN)
                .defaultValue(false)
                .attribute(AGGREGABLE_ATTRIBUTE)
                .type(AnnotationElement.AttributeType.BOOLEAN)
                .defaultValue(false)
                .attribute(SORTABLE_ATTRIBUTE)
                .type(AnnotationElement.AttributeType.BOOLEAN)
                .defaultValue(false)
                .attribute(INDEX_NULL_AS_ATTRIBUTE)
                .type(AnnotationElement.AttributeType.STRING)
                .defaultValue(Values.DO_NOT_INDEX_NULL);
    }

    /**
     * 比较两个实体描述符，若索引相关注解发生增删改则返回 {@code true}。
     */
    public static boolean isIndexSchemaChanged(Descriptor oldDescriptor, Descriptor newDescriptor) {
        var allFields = Stream.concat(
                oldDescriptor.getFields().stream().map(AnnotatedDescriptorImpl::getName),
                newDescriptor.getFields().stream().map(AnnotatedDescriptorImpl::getName)
        ).collect(Collectors.toSet());
        for (var fieldName : allFields) {
            var oldField = oldDescriptor.findFieldByName(fieldName);
            var newField = newDescriptor.findFieldByName(fieldName);
            if (isNewFieldAdded(oldField, newField)) {
                if (isFieldIndexed(newField)) {
                    // 新增字段且已启用索引
                    return true;
                }
                continue;
            }
            if (isNewFieldRemoved(oldField, newField)) {
                if (isFieldIndexed(oldField)) {
                    // 已索引字段被删除
                    return true;
                }
                continue;
            }
            if (isFieldIndexed(oldField) != isFieldIndexed(newField)) {
                // 索引注解被添加或移除
                return true;
            }
            if (!isFieldIndexed(oldField) && !isFieldIndexed(newField)) {
                // 双方均未索引，无变化
                continue;
            }
            if (isAnnotationChanged(oldField, newField)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNewFieldAdded(FieldDescriptor oldField, FieldDescriptor newField) {
        return oldField == null && newField != null;
    }

    private static boolean isNewFieldRemoved(FieldDescriptor oldField, FieldDescriptor newField) {
        return oldField != null && newField == null;
    }

    /** 判断字段是否配置了索引注解。 */
    private static boolean isFieldIndexed(FieldDescriptor descriptor) {
        var annotations = descriptor.getAnnotations();
        return INDEX_ANNOTATION.stream().anyMatch(annotations::containsKey);
    }

    private static boolean isAnnotationChanged(FieldDescriptor oldField, FieldDescriptor newField) {
        return INDEX_ANNOTATION.stream().anyMatch(s -> {
            var oldAnnot = oldField.getAnnotations().get(s);
            var newAnnot = newField.getAnnotations().get(s);
            return isAnnotatedDifferent(oldAnnot, newAnnot);
        });
    }

    private static boolean isAnnotatedDifferent(AnnotationElement.Annotation oldAnnot, AnnotationElement.Annotation newAnnot) {
        if (oldAnnot == null && newAnnot == null) {
            // 两侧均无该注解
            return false;
        }
        if (oldAnnot != null && newAnnot == null) {
            // 注解仅存在于旧字段
            return true;
        }
        if (oldAnnot == null) {
            // 注解仅存在于新字段
            return true;
        }
        // 比较注解属性值是否变化
        return !Objects.equals(getAnnotationValues(oldAnnot), getAnnotationValues(newAnnot));

    }

    private static Map<String, Object> getAnnotationValues(AnnotationElement.Annotation annotation) {
        return annotation.getAttributes()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue().getValue()));
    }

    /** 索引实体与其所在缓存名的配对记录。 */
    public record IndexedEntity(String entity, String cache) {
        public IndexedEntity {
            Objects.requireNonNull(entity);
            Objects.requireNonNull(cache);
        }
    }
}
