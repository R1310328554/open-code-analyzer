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
// timer.go — RAG 流水线分阶段耗时计时器：跟踪 LLM 检查、检索、答案生成等各 Phase 的累计与嵌套耗时。

//

package common

import (
	"encoding/json"
	"fmt"
	"strings"
	"sync"
	"time"
)

// Phase RAG 流水线中的命名计时阶段。
type Phase string

// 各 RAG 阶段常量，与 Markdown 报告顺序一致。
const (
	// PhaseCheckLLM LLM 可用性检查阶段。
	PhaseCheckLLM        Phase = "check_llm"
	// PhaseCheckLangfuse Langfuse 追踪器检查阶段。
	PhaseCheckLangfuse   Phase = "check_langfuse"
	// PhaseBindModels 模型绑定与加载阶段。
	PhaseBindModels      Phase = "bind_models"
	// PhaseQueryRefinement 查询改写（LLM）阶段。
	PhaseQueryRefinement Phase = "query_refinement"
	// PhaseRetrieval 向量/混合检索阶段。
	PhaseRetrieval       Phase = "retrieval"
	// PhaseGenerateAnswer 答案生成（LLM）阶段。
	PhaseGenerateAnswer  Phase = "generate_answer"
)

// allPhases Markdown 输出时的阶段顺序。
var allPhases = []Phase{
	PhaseCheckLLM,
	PhaseCheckLangfuse,
	PhaseBindModels,
	PhaseQueryRefinement,
	PhaseRetrieval,
	PhaseGenerateAnswer,
}

// Timer 按 Phase 累计墙钟耗时；Enter/Exit 可重入，内层时长累加到外层。
type Timer struct {
	mu      sync.Mutex
	start   time.Time
	phases  map[Phase]time.Duration
	entries map[Phase][]time.Time
}

// NewTimer 构造空计时器，预分配各阶段 map。
func NewTimer() *Timer {
	return &Timer{
		phases:  make(map[Phase]time.Duration, len(allPhases)),
		entries: make(map[Phase][]time.Time, len(allPhases)),
	}
}

// Start 重置并锚定计时起点；重复调用会清空全部阶段数据。
func (t *Timer) Start() {
	t.mu.Lock()
	defer t.mu.Unlock()
	t.start = time.Now()
	t.phases = make(map[Phase]time.Duration, len(allPhases))
	t.entries = make(map[Phase][]time.Time, len(allPhases))
}

// Enter 进入阶段 p，重入时压栈新锚点。
func (t *Timer) Enter(p Phase) {
	t.mu.Lock()
	defer t.mu.Unlock()
	t.entries[p] = append(t.entries[p], time.Now())
}

// Exit 弹出最近 Enter 并累加耗时；无 Enter 时为 no-op。
func (t *Timer) Exit(p Phase) {
	t.mu.Lock()
	defer t.mu.Unlock()
	stack := t.entries[p]
	if len(stack) == 0 {
		return
	}
	open := stack[len(stack)-1]
	t.entries[p] = stack[:len(stack)-1]
	t.phases[p] += time.Since(open)
}

// Phase 返回阶段 p 的累计耗时。
func (t *Timer) Phase(p Phase) time.Duration {
	t.mu.Lock()
	defer t.mu.Unlock()
	return t.phases[p]
}

// Total 返回自 Start 以来的总耗时。
func (t *Timer) Total() time.Duration {
	t.mu.Lock()
	defer t.mu.Unlock()
	if t.start.IsZero() {
		return 0
	}
	return time.Since(t.start)
}

// PhaseReport 计时器 JSON 快照：各阶段毫秒数与总毫秒数。
type PhaseReport struct {
	PhasesMs map[string]float64 `json:"phases_ms"`
	TotalMs  float64            `json:"total_ms"`
}

// Report 生成微秒精度的 JSON 可序列化快照。
func (t *Timer) Report() *PhaseReport {
	t.mu.Lock()
	defer t.mu.Unlock()
	phases := make(map[string]float64, len(allPhases))
	for _, p := range allPhases {
		phases[string(p)] = float64(t.phases[p].Microseconds()) / 1000.0
	}
	var totalMs float64
	if !t.start.IsZero() {
		totalMs = float64(time.Since(t.start).Microseconds()) / 1000.0
	}
	return &PhaseReport{PhasesMs: phases, TotalMs: totalMs}
}

// MarshalJSON 使 Timer 可直接 json.Marshal 为 PhaseReport。
func (t *Timer) MarshalJSON() ([]byte, error) {
	return json.Marshal(t.Report())
}

// Markdown 渲染 "## Time elapsed:" 块，供 LLM 回复附加耗时信息。
func (t *Timer) Markdown() string {
	r := t.Report()
	var b strings.Builder
	b.WriteString("\n## Time elapsed:\n")
	b.WriteString(fmt.Sprintf("  - Total: %.1fms\n", r.TotalMs))
	for _, p := range allPhases {
		ms := r.PhasesMs[string(p)]
		b.WriteString(fmt.Sprintf("  - %s: %.1fms\n", displayName(p), ms))
	}
	b.WriteString("\n")
	return b.String()
}

// displayName 将 Phase 枚举转为人类可读英文标签。
func displayName(p Phase) string {
	switch p {
	case PhaseCheckLLM:
		return "Check LLM"
	case PhaseCheckLangfuse:
		return "Check Langfuse tracer"
	case PhaseBindModels:
		return "Bind models"
	case PhaseQueryRefinement:
		return "Query refinement(LLM)"
	case PhaseRetrieval:
		return "Retrieval"
	case PhaseGenerateAnswer:
		return "Generate answer"
	default:
		return string(p)
	}
}
