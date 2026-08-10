// Copyright 2019 Drone IO, Inc.
// Copyright 2018 natessilva
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// dag 包提供有向无环图（DAG），用于流水线阶段依赖解析与环检测。
package dag

// Dag 表示阶段依赖的有向无环图。
type Dag struct {
	graph map[string]*Vertex
}

// Vertex 表示图中的一个节点，Skip 标记该阶段是否被跳过。
type Vertex struct {
	Name  string
	Skip  bool
	graph []string
}

// New 创建空 DAG，用于判定阶段间依赖关系。
func New() *Dag {
	return &Dag{
		graph: make(map[string]*Vertex),
	}
}

// Add 添加节点并建立 from 对 to 的依赖边。
func (d *Dag) Add(from string, to ...string) *Vertex {
	vertex := new(Vertex)
	vertex.Name = from
	vertex.Skip = false
	vertex.graph = to
	d.graph[from] = vertex
	return vertex
}

// Get 按名称返回图中节点。
func (d *Dag) Get(name string) (*Vertex, bool) {
	vertex, ok := d.graph[name]
	return vertex, ok
}

// Dependencies 返回有效直接依赖（跳过被标记 Skip 的节点并向上追溯）。
func (d *Dag) Dependencies(name string) []string {
	vertex := d.graph[name]
	return d.dependencies(vertex)
}

// Ancestors 返回节点的全部非 Skip 祖先节点。
func (d *Dag) Ancestors(name string) []*Vertex {
	vertex := d.graph[name]
	return d.ancestors(vertex)
}

// DetectCycles 检测图中是否存在依赖环。
func (d *Dag) DetectCycles() bool {
	visited := make(map[string]bool)
	recStack := make(map[string]bool)

	for vertex := range d.graph {
		if !visited[vertex] {
			if d.detectCycles(vertex, visited, recStack) {
				return true
			}
		}
	}
	return false
}

// ancestors 递归收集节点的非 Skip 祖先。
func (d *Dag) ancestors(parent *Vertex) []*Vertex {
	if parent == nil {
		return nil
	}
	var combined []*Vertex
	for _, name := range parent.graph {
		vertex, found := d.graph[name]
		if !found {
			continue
		}
		if !vertex.Skip {
			combined = append(combined, vertex)
		}
		combined = append(combined, d.ancestors(vertex)...)
	}
	return combined
}

// dependencies 递归解析有效依赖，跳过标记为 Skip 的中间节点。
func (d *Dag) dependencies(parent *Vertex) []string {
	if parent == nil {
		return nil
	}
	var combined []string
	for _, name := range parent.graph {
		vertex, found := d.graph[name]
		if !found {
			continue
		}
		if vertex.Skip {
			// 若节点被跳过，则沿图向上查找其祖先作为有效依赖。
			combined = append(combined, d.dependencies(vertex)...)
		} else {
			combined = append(combined, vertex.Name)
		}
	}
	return combined
}

// detectCycles 深度优先检测从 name 出发是否存在回边（环）。
func (d *Dag) detectCycles(name string, visited, recStack map[string]bool) bool {
	visited[name] = true
	recStack[name] = true

	vertex, ok := d.graph[name]
	if !ok {
		return false
	}
	for _, v := range vertex.graph {
		// 每个节点在 visited 中只完整 DFS 一次
		if !visited[v] {
			if d.detectCycles(v, visited, recStack) {
				return true
			}
			// 若在当前递归栈中再次访问，说明存在环
		} else if recStack[v] {
			return true
		}

	}
	recStack[name] = false
	return false
}
