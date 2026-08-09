/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.search.index;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * 创建 RediSearch 索引时的全局选项，对应 {@code FT.CREATE} 命令参数。
 * <p>
 * 通过 {@link #defaults()} 获取默认配置，再链式设置文档类型、键前缀、语言与停用词等。
 *
 * @author Nikita Koksharov
 *
 */
public final class IndexOptions {

    private List<String> prefix = Collections.emptyList();
    private String filter;
    private Double score;
    private String language;
    private Long temporary;
    private boolean noOffsets;
    private String languageField;
    private boolean maxTextFields;
    private boolean noFields;
    private String scoreField;
    private boolean noHL;
    private boolean noFreqs;
    private List<String> stopwords;
    private boolean skipInitialScan;
    private IndexType on;
    private byte[] payloadField;

    private IndexOptions() {
    }

    /** 返回默认索引选项实例。 */
    public static IndexOptions defaults() {
        return new IndexOptions();
    }

    /** 指定索引的 Redis 文档类型（Hash 或 JSON）。 */
    public IndexOptions on(IndexType dataType) {
        this.on = dataType;
        return this;
    }

    /** 设置参与索引的键名前缀（可变参数）。 */
    public IndexOptions prefix(String... prefix) {
        return prefix(Arrays.asList(prefix));
    }

    /** 设置参与索引的键名前缀列表。 */
    public IndexOptions prefix(List<String> prefix) {
        this.prefix = prefix;
        return this;
    }

    /** 设置索引文档过滤器表达式。 */
    public IndexOptions filter(String filter) {
        this.filter = filter;
        return this;
    }

    /** 设置默认文档语言。 */
    public IndexOptions language(String language) {
        this.language = language;
        return this;
    }

    /** 指定存放文档语言的字段名。 */
    public IndexOptions languageField(String languageField) {
        this.languageField = languageField;
        return this;
    }

    /** 设置默认文档评分。 */
    public IndexOptions score(Double score) {
        this.score = score;
        return this;
    }

    /** 指定存放文档评分的字段名。 */
    public IndexOptions scoreField(String scoreField) {
        this.scoreField = scoreField;
        return this;
    }

    /** 是否允许索引超过 32 个 TEXT 字段。 */
    public IndexOptions maxTextFields(boolean maxTextFields) {
        this.maxTextFields = maxTextFields;
        return this;
    }

    /** 是否不在倒排索引中存储词项偏移量。 */
    public IndexOptions noOffsets(boolean noOffsets) {
        this.noOffsets = noOffsets;
        return this;
    }

    /** 设置索引的临时存活时间（秒），到期后自动删除。 */
    public IndexOptions temporary(Long temporary) {
        this.temporary = temporary;
        return this;
    }

    /** 是否禁用高亮所需的字段内容存储。 */
    public IndexOptions noHL(boolean noHL) {
        this.noHL = noHL;
        return this;
    }

    /** 是否不在索引中存储各字段名。 */
    public IndexOptions noFields(boolean noFields) {
        this.noFields = noFields;
        return this;
    }

    /** 是否不在索引中存储词频信息。 */
    public IndexOptions noFreqs(boolean noFreqs) {
        this.noFreqs = noFreqs;
        return this;
    }

    /** 设置自定义停用词列表。 */
    public IndexOptions stopwords(List<String> stopwords) {
        this.stopwords = stopwords;
        return this;
    }

    /** 是否跳过创建索引时的初始全量扫描。 */
    public IndexOptions skipInitialScan(boolean skipInitialScan) {
        this.skipInitialScan = skipInitialScan;
        return this;
    }

    /** 指定存放文档 payload 的字段。 */
    public IndexOptions payloadField(byte[] payloadField) {
        this.payloadField = payloadField;
        return this;
    }

    /** 返回键名前缀列表。 */
    public List<String> getPrefix() {
        return prefix;
    }

    /** 返回文档过滤器表达式。 */
    public String getFilter() {
        return filter;
    }

    /** 返回默认文档评分。 */
    public Double getScore() {
        return score;
    }

    /** 返回默认文档语言。 */
    public String getLanguage() {
        return language;
    }

    /** 返回临时索引存活时间（秒）。 */
    public Long getTemporary() {
        return temporary;
    }

    /** 是否禁用偏移量存储。 */
    public boolean isNoOffsets() {
        return noOffsets;
    }

    /** 返回语言字段名。 */
    public String getLanguageField() {
        return languageField;
    }

    /** 是否允许超过 32 个 TEXT 字段。 */
    public boolean isMaxTextFields() {
        return maxTextFields;
    }

    /** 是否不存储字段名。 */
    public boolean isNoFields() {
        return noFields;
    }

    /** 返回评分字段名。 */
    public String getScoreField() {
        return scoreField;
    }

    /** 是否禁用高亮存储。 */
    public boolean isNoHL() {
        return noHL;
    }

    /** 是否不存储词频。 */
    public boolean isNoFreqs() {
        return noFreqs;
    }

    /** 返回停用词列表。 */
    public List<String> getStopwords() {
        return stopwords;
    }

    /** 是否跳过初始扫描。 */
    public boolean isSkipInitialScan() {
        return skipInitialScan;
    }

    /** 返回索引的文档类型。 */
    public IndexType getOn() {
        return on;
    }

    /** 返回 payload 字段内容。 */
    public byte[] getPayloadField() {
        return payloadField;
    }
}
