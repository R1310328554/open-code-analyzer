// Copyright 2019 Drone IO, Inc.
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

package runner

import (
	"strings"

	"github.com/drone/drone-runtime/engine"
	"github.com/drone/drone-runtime/runtime"
	"github.com/drone/drone/core"
)

// convertVolumes 将 "host:container" 格式的卷挂载字符串列表转为映射表。
func convertVolumes(from []string) map[string]string {
	to := map[string]string{}
	for _, s := range from {
		parts := strings.Split(s, ":")
		if len(parts) != 2 {
			continue
		}
		key := parts[0]
		val := parts[1]
		to[key] = val
	}
	return to
}

// convertSecrets 将 core.Secret 切片转为名称到明文的映射。
func convertSecrets(from []*core.Secret) map[string]string {
	to := map[string]string{}
	for _, secret := range from {
		to[secret.Name] = secret.Data
	}
	return to
}

// convertRegistry 将 Drone 镜像仓库凭据转换为 runtime 引擎所需的 DockerAuth 列表。
func convertRegistry(from []*core.Registry) []*engine.DockerAuth {
	var to []*engine.DockerAuth
	for _, registry := range from {
		to = append(to, &engine.DockerAuth{
			Address:  registry.Address,
			Username: registry.Username,
			Password: registry.Password,
		})
	}
	return to
}

// convertLines 批量将 runtime 日志行转换为 core.Line 结构。
func convertLines(from []*runtime.Line) []*core.Line {
	var to []*core.Line
	for _, v := range from {
		to = append(to, &core.Line{
			Number:    v.Number,
			Message:   v.Message,
			Timestamp: v.Timestamp,
		})
	}
	return to
}

// convertLine 将单条 runtime 日志行转换为 core.Line。
func convertLine(from *runtime.Line) *core.Line {
	return &core.Line{
		Number:    from.Number,
		Message:   from.Message,
		Timestamp: from.Timestamp,
	}
}
