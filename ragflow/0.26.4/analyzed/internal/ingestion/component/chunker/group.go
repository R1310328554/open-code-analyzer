//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

// group.go 职责说明：
//
//   - 实现 GroupTitleChunker：在同标题节内合并相邻文本记录为多记录 chunk。
//
//   - Parallelism() 向外层执行器提示 fan-out；标题检测串行，分组本地完成。
//
//   - 镜像 Python _build_section_ids + GroupTitleChunker.build_chunks：
//     相同 sec_id 的记录在 MIN/MAX_GROUP_TOKENS 内合并后逐组输出。
//
//   - 不做 PDF 位置合并（与 TokenChunker 一致，待 deepdoc/parser 移植）。
package chunker

import (
	"context"
	"fmt"
	"strings"

	"ragflow/internal/agent/runtime"
	"ragflow/internal/ingestion/component/schema"
	"ragflow/internal/tokenizer"
)

const ComponentNameGroupTitleChunker = "GroupTitleChunker"

// minGroupTokens/maxGroupTokens 镜像 group_chunker.py:22-23，驱动相邻文本合并启发式。
const (
	minGroupTokens = 32
	maxGroupTokens = 1024
)

// resolveTargetLevel 镜像 common.py：从 levels 向量选第 n 小标题层级。
func resolveTargetLevel(levels []int, hierarchy int) int {
	tiers := make([]int, 0, len(levels))
	seen := make(map[int]bool)
	for _, l := range levels {
		if l > 0 && l < bodyLevel && !seen[l] {
			seen[l] = true
			tiers = append(tiers, l)
		}
	}
	if len(tiers) == 0 {
		return 0
	}
	// 升序排序
	for i := 1; i < len(tiers); i++ {
		for j := i; j > 0 && tiers[j-1] > tiers[j]; j-- {
			tiers[j-1], tiers[j] = tiers[j], tiers[j-1]
		}
	}
	if hierarchy < 1 {
		hierarchy = 1
	}
	if hierarchy > len(tiers) {
		hierarchy = len(tiers)
	}
	return tiers[hierarchy-1]
}

// buildSectionIDs 为每行计算所属 section id（sid）；
// 遇标题 sid+1，正文共享当前 sid。
func buildSectionIDs(levels []int, targetLevel int) []int {
	secIDs := make([]int, len(levels))
	sid := 0
	for i, lvl := range levels {
		if i > 0 && targetLevel > 0 && lvl <= targetLevel {
			sid++
		}
		secIDs[i] = sid
	}
	return secIDs
}

// invokeGroup 执行 GroupTitleChunker 策略：标题检测串行，分组合并本地完成。
func invokeGroup(_ context.Context, inputs map[string]any, p *titleChunkerParam) (map[string]any, error) {
	records := extractLineRecords(inputs)
	if len(records) == 0 {
		return emptyOutputs(), nil
	}
	lines := make([]string, len(records))
	for i, r := range records {
		lines[i] = r.text
	}
	ctx := newLevelContext(lines, p)
	levels := ctx.Levels()

	targetLevel := resolveTargetLevel(levels, hierarchyOr(p, ctx.mostLevel))
	secIDs := buildSectionIDs(levels, targetLevel)

	groups := groupRecords(records, secIDs, p)
	if p.RootChunkAsHeading && len(groups) > 1 {
		groups = applyRootAsHeading(groups)
	}
	chunks := make([]map[string]any, 0, len(groups))
	for _, g := range groups {
		chunks = append(chunks, map[string]any{"text": joinGroupText(g)})
	}
	if len(chunks) == 0 {
		return emptyOutputs(), nil
	}
	return map[string]any{
		"output_format": "chunks",
		"chunks":        chunks,
	}, nil
}

// groupRecords 镜像 GroupTitleChunker.build_chunks：
// 同 section 且满足 token 预算时合并相邻文本记录。
func groupRecords(records []lineRecord, secIDs []int, p *titleChunkerParam) [][]lineRecord {
	if len(records) == 0 {
		return nil
	}
	var recordGroups [][]lineRecord
	var currentGroup []lineRecord
	tkCnt := 0
	lastSID := -2

	for i, rec := range records {
		secID := secIDs[i]
		if !rec.isText() {
			if len(currentGroup) > 0 {
				recordGroups = append(recordGroups, append([]lineRecord(nil), currentGroup...))
			}
			recordGroups = append(recordGroups, []lineRecord{rec})
			currentGroup = currentGroup[:0]
			tkCnt = 0
			lastSID = -2
			continue
		}
		text := trim(rec.text)
		if text == "" {
			continue
		}
		tokenCount := tokenizer.NumTokensFromString(text)
		shouldMerge := len(currentGroup) > 0 &&
			currentGroup[0].isText() &&
			(tkCnt < minGroupTokens || (tkCnt < maxGroupTokens && secID == lastSID))
		if shouldMerge {
			currentGroup = append(currentGroup, rec)
			tkCnt += tokenCount
		} else {
			if len(currentGroup) > 0 {
				recordGroups = append(recordGroups, append([]lineRecord(nil), currentGroup...))
			}
			currentGroup = []lineRecord{rec}
			tkCnt = tokenCount
		}
		lastSID = secID
	}
	if len(currentGroup) > 0 {
		recordGroups = append(recordGroups, currentGroup)
	}
	return recordGroups
}

// applyRootAsHeading 镜像 root_chunk_as_heading：
// 将 root 文本前置到后续 chunk 并丢弃 root chunk。
func applyRootAsHeading(groups [][]lineRecord) [][]lineRecord {
	if len(groups) < 2 {
		return groups
	}
	rootText := joinGroupText(groups[0])
	for i := 1; i < len(groups); i++ {
		groups[i] = prependJoin(groups[i], rootText)
	}
	return groups[1:]
}

func joinGroupText(g []lineRecord) string {
	var sb strings.Builder
	for _, r := range g {
		sb.WriteString(r.text)
		sb.WriteByte('\n')
	}
	return sb.String()
}

func prependJoin(g []lineRecord, prefix string) []lineRecord {
	if prefix == "" {
		return g
	}
	extra := lineRecord{text: prefix, docType: "text"}
	if len(g) == 0 {
		return []lineRecord{extra}
	}
	out := make([]lineRecord, 0, len(g)+1)
	out = append(out, extra)
	out = append(out, g...)
	return out
}

// extractLineRecords 按 Python BaseTitleChunker.extract_line_records 顺序读取输入：
//  1. 上游 chunks/JSON → 结构化归一化；
//  2. 否则 text/markdown/html 按行一条记录。
func extractLineRecords(inputs map[string]any) []lineRecord {
	if docs := chunksFromInputs(inputs); docs != nil {
		return recordsFromStructured(docs)
	}
	text, _ := stringFromInputs(inputs, "text", "content")
	if text == "" {
		return nil
	}
	return lineRecordsFromText(text)
}

func recordsFromStructured(items []schema.ChunkDoc) []lineRecord {
	out := make([]lineRecord, 0, len(items))
	for _, it := range items {
		text := itemTextOrFallback(it)
		if text == "" {
			continue
		}
		dt := it.DocType
		if dt == "" {
			dt = "text"
		}
		var imgID *string
		if it.ImgID != "" {
			img := it.ImgID
			imgID = &img
		}
		out = append(out, lineRecord{
			text:    text,
			docType: dt,
			imgID:   imgID,
			layout:  it.Layout,
		})
	}
	return out
}

// hierarchyOr 返回 param.hierarchy 或 level 频率统计的 mostLevel。
func hierarchyOr(p *titleChunkerParam, mostLevel int) int {
	if p.Hierarchy != nil && *p.Hierarchy > 0 {
		return *p.Hierarchy
	}
	return mostLevel
}

// GroupTitleChunkerComponent 独立变体入口，canvas 可直接选用。
type GroupTitleChunkerComponent struct {
	name  string
	param titleChunkerParam
}

// NewGroupTitleChunker 构造 method=group 的变体组件。
func NewGroupTitleChunker(params map[string]any) (runtime.Component, error) {
	conf := map[string]any{"method": "group"}
	for k, v := range params {
		conf[k] = v
	}
	p := defaultsTitle()
	p.Update(conf)
	if err := p.TitleChunkerParam.Validate(); err != nil {
		return nil, fmt.Errorf("GroupTitleChunker: %w", err)
	}
	return &GroupTitleChunkerComponent{
		name:  ComponentNameGroupTitleChunker,
		param: p,
	}, nil
}

func (c *GroupTitleChunkerComponent) Parallelism() int { return 2 }
func (c *GroupTitleChunkerComponent) Inputs() map[string]string {
	return ChunkerInputs
}
func (c *GroupTitleChunkerComponent) Outputs() map[string]string {
	return ChunkerOutputs
}

func (c *GroupTitleChunkerComponent) Invoke(ctx context.Context, inputs map[string]any) (map[string]any, error) {
	return runtime.TrackElapsed(ComponentNameGroupTitleChunker, func() (map[string]any, error) {
		if inputs == nil {
			return emptyOutputs(), nil
		}
		if _, ok := inputs["name"].(string); !ok {
			return map[string]any{
				"output_format": "chunks",
				"chunks":        []map[string]any{},
				"_ERROR":        "GroupTitleChunker: missing required upstream field \"name\"",
			}, nil
		}
		return invokeGroup(ctx, inputs, &c.param)
	})
}

// init 在 CategoryIngestion 下注册 GroupTitleChunker。
func init() {
	MustRegisterChunker(ComponentNameGroupTitleChunker)
}
