/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.partialimport;

import java.util.HashSet;
import java.util.Set;

/**
 * 聚合全部 {@link PartialImportResult}，供管理控制台展示及生成管理事件。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class PartialImportResults {

    // 以下字段仅用于 admin client 从 JSON 反序列化，运行时不会直接赋值
    // they are never directly set
    private int overwritten;
    private int added;
    private int skipped;

    private String errorMessage;

    private final Set<PartialImportResult> importResults = new HashSet<>();

    /** 追加单条导入结果。 */
    public void addResult(PartialImportResult result) {
        importResults.add(result);
    }

    /** 合并另一组导入结果。 */
    public void addAllResults(PartialImportResults results) {
        importResults.addAll(results.getResults());
    }

    /** @return 新增资源数量 */
    public int getAdded() {
        int added = 0;
        for (PartialImportResult result : importResults) {
            if (result.getAction() == Action.ADDED) added++;
        }

        return added;
    }

    /** @return 覆盖资源数量 */
    public int getOverwritten() {
        int overwritten = 0;
        for (PartialImportResult result : importResults) {
            if (result.getAction() == Action.OVERWRITTEN) overwritten++;
        }

        return overwritten;
    }

    /** @return 跳过资源数量 */
    public int getSkipped() {
        int skipped = 0;
        for (PartialImportResult result : importResults) {
            if (result.getAction() == Action.SKIPPED) skipped++;
        }

        return skipped;
    }

    /** @return 全部导入结果集合 */
    public Set<PartialImportResult> getResults() {
        return importResults;
    }

    /** @return 导入失败时的错误信息 */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** 设置导入错误信息。 */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
