package org.keycloak.db.compatibility.verifier;

import java.util.Collection;

/**
 * 兼容性快照 JSON 文件的根结构，同时承载 Liquibase {@link ChangeSet} 与 Java {@link Migration} 两类条目。
 *
 * @param changeSets  已记录的变更集集合
 * @param migrations  已记录的迁移类集合
 */
record JsonParent(Collection<ChangeSet> changeSets, Collection<Migration> migrations) {}
