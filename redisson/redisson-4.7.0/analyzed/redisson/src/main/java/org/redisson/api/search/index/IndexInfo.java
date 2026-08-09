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

import java.util.List;
import java.util.Map;

/**
 * RediSearch {@code FT.INFO} 命令返回的索引元数据与统计信息。
 * <p>
 * 包含索引定义、字段属性、内存占用、文档数量及索引进度等运行时指标。
 *
 * @author Nikita Koksharov
 *
 */
public class IndexInfo {

    /** 索引名称。 */
    private String name;

    /** 索引创建选项。 */
    private Map<String, Object> options;

    /** 索引定义（文档类型、前缀等）。 */
    private Map<String, Object> definition;

    /** 各字段属性列表。 */
    private List<Map<String, Object>> attributes;

    /** 垃圾回收统计。 */
    private Map<String, Object> gcStats;

    /** 游标统计。 */
    private Map<String, Object> cursorStats;

    /** 查询方言使用统计。 */
    private Map<String, Object> dialectStats;

    /** 已索引文档数。 */
    private Double docs;

    /** 最大文档 ID。 */
    private Double maxDocId;

    /** 索引词项总数。 */
    private Double terms;

    /** 倒排索引记录数。 */
    private Double records;

    /** 倒排索引占用字节数。 */
    private Double invertedSize;

    /** 向量索引占用字节数。 */
    private Double vectorIndexSize;

    /** 倒排索引块总数。 */
    private Double totalInvertedIndexBlocks;

    /** 偏移向量占用字节数。 */
    private Double offsetVectorsSize;

    /** 文档表占用字节数。 */
    private Double docTableSize;

    /** 可排序值占用字节数。 */
    private Double sortableValuesSize;

    /** 键表占用字节数。 */
    private Double keyTableSize;

    /** 每文档平均记录数。 */
    private Double recordsPerDocAverage;

    /** 每条记录平均字节数。 */
    private Double bytesPerRecordAverage;

    /** 每个词项平均偏移数。 */
    private Double offsetsPerTermAverage;
    /** 每条记录平均偏移位数。 */
    private Double offsetBitsPerRecordAverage;

    /** Hash 索引失败次数。 */
    private Long hashIndexingFailures;

    /** 累计索引耗时（毫秒）。 */
    private Double totalIndexingTime;

    /** 当前正在索引的文档数。 */
    private Long indexing;

    /** 索引进度百分比。 */
    private Double percentIndexed;

    /** 索引被查询使用的次数。 */
    private Long numberOfUses;

    /** 返回索引名称。 */
    public String getName() {
        return name;
    }

    /** 设置索引名称。 */
    public IndexInfo setName(String name) {
        this.name = name;
        return this;
    }

    /** 返回索引创建选项。 */
    public Map<String, Object> getOptions() {
        return options;
    }

    /** 设置索引创建选项。 */
    public IndexInfo setOptions(Map<String, Object> options) {
        this.options = options;
        return this;
    }

    /** 返回索引定义。 */
    public Map<String, Object> getDefinition() {
        return definition;
    }

    /** 设置索引定义。 */
    public IndexInfo setDefinition(Map<String, Object> definition) {
        this.definition = definition;
        return this;
    }

    /** 返回字段属性列表。 */
    public List<Map<String, Object>> getAttributes() {
        return attributes;
    }

    /** 设置字段属性列表。 */
    public IndexInfo setAttributes(List<Map<String, Object>> attributes) {
        this.attributes = attributes;
        return this;
    }

    /** 返回垃圾回收统计。 */
    public Map<String, Object> getGcStats() {
        return gcStats;
    }

    /** 设置垃圾回收统计。 */
    public IndexInfo setGcStats(Map<String, Object> gcStats) {
        this.gcStats = gcStats;
        return this;
    }

    /** 返回游标统计。 */
    public Map<String, Object> getCursorStats() {
        return cursorStats;
    }

    /** 设置游标统计。 */
    public IndexInfo setCursorStats(Map<String, Object> cursorStats) {
        this.cursorStats = cursorStats;
        return this;
    }

    /** 返回查询方言统计。 */
    public Map<String, Object> getDialectStats() {
        return dialectStats;
    }

    /** 设置查询方言统计。 */
    public IndexInfo setDialectStats(Map<String, Object> dialectStats) {
        this.dialectStats = dialectStats;
        return this;
    }

    /** 返回已索引文档数。 */
    public Double getDocs() {
        return docs;
    }

    /** 设置已索引文档数。 */
    public IndexInfo setDocs(Double docs) {
        this.docs = docs;
        return this;
    }

    /** 返回最大文档 ID。 */
    public Double getMaxDocId() {
        return maxDocId;
    }

    /** 设置最大文档 ID。 */
    public IndexInfo setMaxDocId(Double maxDocId) {
        this.maxDocId = maxDocId;
        return this;
    }

    /** 返回索引词项总数。 */
    public Double getTerms() {
        return terms;
    }

    /** 设置索引词项总数。 */
    public IndexInfo setTerms(Double terms) {
        this.terms = terms;
        return this;
    }

    /** 返回倒排索引记录数。 */
    public Double getRecords() {
        return records;
    }

    /** 设置倒排索引记录数。 */
    public IndexInfo setRecords(Double records) {
        this.records = records;
        return this;
    }

    /** 返回倒排索引占用字节数。 */
    public Double getInvertedSize() {
        return invertedSize;
    }

    /** 设置倒排索引占用字节数。 */
    public IndexInfo setInvertedSize(Double invertedSize) {
        this.invertedSize = invertedSize;
        return this;
    }

    /** 返回向量索引占用字节数。 */
    public Double getVectorIndexSize() {
        return vectorIndexSize;
    }

    /** 设置向量索引占用字节数。 */
    public IndexInfo setVectorIndexSize(Double vectorIndexSize) {
        this.vectorIndexSize = vectorIndexSize;
        return this;
    }

    /** 返回倒排索引块总数。 */
    public Double getTotalInvertedIndexBlocks() {
        return totalInvertedIndexBlocks;
    }

    /** 设置倒排索引块总数。 */
    public IndexInfo setTotalInvertedIndexBlocks(Double totalInvertedIndexBlocks) {
        this.totalInvertedIndexBlocks = totalInvertedIndexBlocks;
        return this;
    }

    /** 返回偏移向量占用字节数。 */
    public Double getOffsetVectorsSize() {
        return offsetVectorsSize;
    }

    /** 设置偏移向量占用字节数。 */
    public IndexInfo setOffsetVectorsSize(Double offsetVectorsSize) {
        this.offsetVectorsSize = offsetVectorsSize;
        return this;
    }

    /** 返回文档表占用字节数。 */
    public Double getDocTableSize() {
        return docTableSize;
    }

    /** 设置文档表占用字节数。 */
    public IndexInfo setDocTableSize(Double docTableSize) {
        this.docTableSize = docTableSize;
        return this;
    }

    /** 返回可排序值占用字节数。 */
    public Double getSortableValuesSize() {
        return sortableValuesSize;
    }

    /** 设置可排序值占用字节数。 */
    public IndexInfo setSortableValuesSize(Double sortableValuesSize) {
        this.sortableValuesSize = sortableValuesSize;
        return this;
    }

    /** 返回键表占用字节数。 */
    public Double getKeyTableSize() {
        return keyTableSize;
    }

    /** 设置键表占用字节数。 */
    public IndexInfo setKeyTableSize(Double keyTableSize) {
        this.keyTableSize = keyTableSize;
        return this;
    }

    /** 返回每文档平均记录数。 */
    public Double getRecordsPerDocAverage() {
        return recordsPerDocAverage;
    }

    /** 设置每文档平均记录数。 */
    public IndexInfo setRecordsPerDocAverage(Double recordsPerDocAverage) {
        this.recordsPerDocAverage = recordsPerDocAverage;
        return this;
    }

    /** 返回每条记录平均字节数。 */
    public Double getBytesPerRecordAverage() {
        return bytesPerRecordAverage;
    }

    /** 设置每条记录平均字节数。 */
    public IndexInfo setBytesPerRecordAverage(Double bytesPerRecordAverage) {
        this.bytesPerRecordAverage = bytesPerRecordAverage;
        return this;
    }

    /** 返回每个词项平均偏移数。 */
    public Double getOffsetsPerTermAverage() {
        return offsetsPerTermAverage;
    }

    /** 设置每个词项平均偏移数。 */
    public IndexInfo setOffsetsPerTermAverage(Double offsetsPerTermAverage) {
        this.offsetsPerTermAverage = offsetsPerTermAverage;
        return this;
    }

    /** 返回每条记录平均偏移位数。 */
    public Double getOffsetBitsPerRecordAverage() {
        return offsetBitsPerRecordAverage;
    }

    /** 设置每条记录平均偏移位数。 */
    public IndexInfo setOffsetBitsPerRecordAverage(Double offsetBitsPerRecordAverage) {
        this.offsetBitsPerRecordAverage = offsetBitsPerRecordAverage;
        return this;
    }

    /** 返回 Hash 索引失败次数。 */
    public Long getHashIndexingFailures() {
        return hashIndexingFailures;
    }

    /** 设置 Hash 索引失败次数。 */
    public IndexInfo setHashIndexingFailures(Long hashIndexingFailures) {
        this.hashIndexingFailures = hashIndexingFailures;
        return this;
    }

    /** 返回累计索引耗时（毫秒）。 */
    public Double getTotalIndexingTime() {
        return totalIndexingTime;
    }

    /** 设置累计索引耗时（毫秒）。 */
    public IndexInfo setTotalIndexingTime(Double totalIndexingTime) {
        this.totalIndexingTime = totalIndexingTime;
        return this;
    }

    /** 返回当前正在索引的文档数。 */
    public Long getIndexing() {
        return indexing;
    }

    /** 设置当前正在索引的文档数。 */
    public IndexInfo setIndexing(Long indexing) {
        this.indexing = indexing;
        return this;
    }

    /** 返回索引进度百分比。 */
    public Double getPercentIndexed() {
        return percentIndexed;
    }

    /** 设置索引进度百分比。 */
    public IndexInfo setPercentIndexed(Double percentIndexed) {
        this.percentIndexed = percentIndexed;
        return this;
    }

    /** 返回索引被查询使用的次数。 */
    public Long getNumberOfUses() {
        return numberOfUses;
    }

    /** 设置索引被查询使用的次数。 */
    public IndexInfo setNumberOfUses(Long numberOfUses) {
        this.numberOfUses = numberOfUses;
        return this;
    }

    @Override
    public String toString() {
        return "IndexInfo{" +
                "name='" + name + '\'' +
                ", options=" + options +
                ", definition=" + definition +
                ", attributes=" + attributes +
                ", gcStats=" + gcStats +
                ", cursorStats=" + cursorStats +
                ", dialectStats=" + dialectStats +
                ", docs=" + docs +
                ", maxDocId=" + maxDocId +
                ", terms=" + terms +
                ", records=" + records +
                ", invertedSize=" + invertedSize +
                ", vectorIndexSize=" + vectorIndexSize +
                ", totalInvertedIndexBlocks=" + totalInvertedIndexBlocks +
                ", offsetVectorsSize=" + offsetVectorsSize +
                ", docTableSize=" + docTableSize +
                ", sortableValuesSize=" + sortableValuesSize +
                ", keyTableSize=" + keyTableSize +
                ", recordsPerDocAverage=" + recordsPerDocAverage +
                ", bytesPerRecordAverage=" + bytesPerRecordAverage +
                ", offsetsPerTermAverage=" + offsetsPerTermAverage +
                ", offsetBitsPerRecordAverage=" + offsetBitsPerRecordAverage +
                ", hashIndexingFailures=" + hashIndexingFailures +
                ", totalIndexingTime=" + totalIndexingTime +
                ", indexing=" + indexing +
                ", percentIndexed=" + percentIndexed +
                ", numberOfUses=" + numberOfUses +
                '}';
    }
}
