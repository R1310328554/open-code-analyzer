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

// ComponentsService — Go 流水线 Phase 4：从 runtime 注册表投影组件目录。
// Reads from runtime.DefaultRegistry (the single source of truth) and
// projects each registered component into a JSON-friendly descriptor
// for the GET /api/v1/components endpoint.
//
// Per plan §4 Phase 0 task 1, the runtime registry now REJECTS empty
// metadata at registration time (Version + Inputs + Outputs all
// unset). Every component the catalog can list therefore carries
// real metadata; the legacy fallback (empty inputs/outputs maps
// from the pre-Phase-0 adapter) is no longer reachable. The
// descriptor projection below normalises nil maps to empty maps
// purely for the JSON shape — the underlying component always
// has real metadata.
package service

import (
	"sort"

	"ragflow/internal/agent/runtime"
)

// ComponentDescriptor GET /api/v1/components 返回的静态组件描述。
// GET /api/v1/components. The shape is the union of registration
// metadata; it does NOT depend on having a working factory — listing
// must succeed even if every component's factory is broken.
type ComponentDescriptor struct {
	Name     string            `json:"name"`
	Category string            `json:"category"`
	Inputs   map[string]string `json:"inputs"`
	Outputs  map[string]string `json:"outputs"`
}

// ComponentsService 无状态投影层，数据源为 runtime.DefaultRegistry。
// It holds no state; construction is a constant pointer to allow callers
// (handler, tests) to swap the underlying registry in the future.
type ComponentsService struct{}

// NewComponentsService 返回绑定进程级 DefaultRegistry 的服务实例。
// process-wide runtime.DefaultRegistry.
func NewComponentsService() *ComponentsService {
	return &ComponentsService{}
}

// List 列出已注册组件，可按 category 过滤；结果按 Name 排序。
// or more runtime.Category values. An empty/nil categories slice means
// "all categories"; this matches the plan §4 task 1 contract
// (GET /api/v1/components with no filter returns every registered
// component). Duplicate category values in the input are silently
// de-duplicated (e.g. ?category=agent,agent returns each agent
// component exactly once). Output is sorted by Name for stable UI
// rendering.
func (s *ComponentsService) List(categories ...runtime.Category) ([]ComponentDescriptor, error) {
	seen := make(map[string]struct{})
	var out []ComponentDescriptor
	add := func(n string, cat runtime.Category, meta runtime.Metadata) {
		key := string(cat) + "\x00" + n
		if _, dup := seen[key]; dup {
			return
		}
		seen[key] = struct{}{}
		out = append(out, descriptor(n, cat, meta))
	}
	if len(categories) == 0 {
		for _, n := range runtime.DefaultRegistry.Names() {
			_, cat, meta, ok := runtime.DefaultRegistry.Lookup(n)
			if !ok {
				continue
			}
			add(n, cat, meta)
		}
	} else {
		for _, cat := range dedupeCategories(categories) {
			for _, n := range runtime.DefaultRegistry.NamesByCategory(cat) {
				_, _, meta, ok := runtime.DefaultRegistry.Lookup(n)
				if !ok {
					continue
				}
				add(n, cat, meta)
			}
		}
	}
	sort.Slice(out, func(i, j int) bool { return out[i].Name < out[j].Name })
	return out, nil
}

// dedupeCategories 对 category 切片去重并保持首次出现顺序。
// preserving first-occurrence order. Returns nil if `in` is empty.
func dedupeCategories(in []runtime.Category) []runtime.Category {
	if len(in) == 0 {
		return nil
	}
	seen := make(map[runtime.Category]struct{}, len(in))
	out := make([]runtime.Category, 0, len(in))
	for _, c := range in {
		if _, dup := seen[c]; dup {
			continue
		}
		seen[c] = struct{}{}
		out = append(out, c)
	}
	return out
}

// descriptor 将注册元数据投影为 JSON 友好的 ComponentDescriptor。
// the JSON-friendly ComponentDescriptor shape. nil maps become empty
// maps so the JSON payload always carries a non-null `inputs` /
// `outputs` field — easier for frontends than distinguishing nil from
// empty.
func descriptor(name string, cat runtime.Category, meta runtime.Metadata) ComponentDescriptor {
	inputs := meta.Inputs
	if inputs == nil {
		inputs = map[string]string{}
	}
	outputs := meta.Outputs
	if outputs == nil {
		outputs = map[string]string{}
	}
	return ComponentDescriptor{
		Name:     name,
		Category: string(cat),
		Inputs:   inputs,
		Outputs:  outputs,
	}
}
// components_service.go — Agent 组件目录投影，供 GET /api/v1/components 使用。
