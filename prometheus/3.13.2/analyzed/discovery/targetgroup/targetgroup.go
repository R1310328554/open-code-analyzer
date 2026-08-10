// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// targetgroup 包定义服务发现输出的目标组结构：同一 Source 下共享标签的一组 LabelSet target，支持 YAML/JSON 序列化。

// targetgroup 包定义服务发现输出的目标组结构：同一 Source 下共享标签的一组 LabelSet target，支持 YAML/JSON 序列化。

package targetgroup

import (
	"bytes"
	"encoding/json"

	"github.com/prometheus/common/model"
)

// Group 表示具有共同标签集合的一组抓取 target（如 prod/test 环境）。
// Group is a set of targets with a common label set(production , test, staging etc.).
type Group struct {
	// Targets is a list of targets identified by a label set. Each target is
	// uniquely identifiable in the group by its address label.
	// Targets 为带完整标签集的 target 列表，__address__ 唯一标识组内成员。
	Targets []model.LabelSet
	// Labels is a set of labels that is common across all targets in the group.
	// Labels 为该组所有 target 共享的公共标签。
	Labels model.LabelSet

	// Source is an identifier that describes a group of targets.
	// Source 标识该 target 组的来源（如 SD 机制名或 API 端点）。
	Source string
}

func (tg Group) String() string {
	return tg.Source
}

// YAML 反序列化：将 targets 字符串列表转为 __address__ LabelSet。
// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (tg *Group) UnmarshalYAML(unmarshal func(any) error) error {
	g := struct {
		Targets []string       `yaml:"targets"`
		Labels  model.LabelSet `yaml:"labels"`
	}{}
	if err := unmarshal(&g); err != nil {
		return err
	}
	tg.Targets = make([]model.LabelSet, 0, len(g.Targets))
	for _, t := range g.Targets {
		tg.Targets = append(tg.Targets, model.LabelSet{
			model.AddressLabel: model.LabelValue(t),
		})
	}
	tg.Labels = g.Labels
	return nil
}

// YAML 序列化：从 LabelSet 提取 __address__ 写入 targets 数组。
// MarshalYAML implements the yaml.Marshaler interface.
func (tg Group) MarshalYAML() (any, error) {
	g := &struct {
		Targets []string       `yaml:"targets"`
		Labels  model.LabelSet `yaml:"labels,omitempty"`
	}{
		Targets: make([]string, 0, len(tg.Targets)),
		Labels:  tg.Labels,
	}
	for _, t := range tg.Targets {
		g.Targets = append(g.Targets, string(t[model.AddressLabel]))
	}
	return g, nil
}

// UnmarshalJSON implements the json.Unmarshaler interface.
func (tg *Group) UnmarshalJSON(b []byte) error {
	g := struct {
		Targets []string       `json:"targets"`
		Labels  model.LabelSet `json:"labels"`
	}{}

	dec := json.NewDecoder(bytes.NewReader(b))
	dec.DisallowUnknownFields()
	if err := dec.Decode(&g); err != nil {
		return err
	}
	tg.Targets = make([]model.LabelSet, 0, len(g.Targets))
	for _, t := range g.Targets {
		tg.Targets = append(tg.Targets, model.LabelSet{
			model.AddressLabel: model.LabelValue(t),
		})
	}
	tg.Labels = g.Labels
	return nil
}

// MarshalJSON implements the json.Marshaler interface.
func (tg Group) MarshalJSON() ([]byte, error) {
	g := &struct {
		Targets []string       `json:"targets"`
		Labels  model.LabelSet `json:"labels,omitempty"`
	}{
		Targets: make([]string, 0, len(tg.Targets)),
		Labels:  tg.Labels,
	}
	for _, t := range tg.Targets {
		g.Targets = append(g.Targets, string(t[model.AddressLabel]))
	}
	return json.Marshal(g)
}
