// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package config

import (
	"bytes"
	"context"
	"strings"

	"github.com/drone/drone/core"

	"github.com/google/go-jsonnet"
)

// Jsonnet 创建 Jsonnet 配置服务：从 SCM 读取 .jsonnet 文件并求值转换为 YAML。
func Jsonnet(service core.FileService, enabled bool) core.ConfigService {
	return &jsonnetPlugin{
		enabled: enabled,
		repos:   &repo{files: service},
	}
}

// jsonnetPlugin 在仓库配置路径为 .jsonnet 时执行求值并合并多文档 YAML 输出。
type jsonnetPlugin struct {
	enabled bool
	repos   *repo
}

// Find 若插件启用且仓库配置为 .jsonnet 后缀，则从 SCM 拉取文件并通过 Jsonnet VM 转为 YAML。
func (p *jsonnetPlugin) Find(ctx context.Context, req *core.ConfigArgs) (*core.Config, error) {
	if p.enabled == false {
		return nil, nil
	}

	// 非 .jsonnet 扩展名时跳过本插件，返回零值。
	if strings.HasSuffix(req.Repo.Config, ".jsonnet") == false {
		return nil, nil
	}

	// 从 SCM 获取 Jsonnet 源文件内容。
	config, err := p.repos.Find(ctx, req)
	if err != nil {
		return nil, err
	}

	// TODO(bradrydzewski) temporarily disable file imports
	// TODO(bradrydzewski) handle object vs array output

	// 创建 Jsonnet 虚拟机并配置栈深与错误格式。
	vm := jsonnet.MakeVM()
	vm.MaxStack = 500
	vm.StringOutput = false
	vm.ErrorFormatter.SetMaxStackTraceSize(20)

	// 将 Jsonnet 片段求值为 YAML 文档流。
	buf := new(bytes.Buffer)
	docs, err := vm.EvaluateSnippetStream(req.Repo.Config, config.Data)
	if err != nil {
		return nil, err
	}

	// Jsonnet VM 可能输出多个 YAML 文档，需用 --- 分隔符合并为单一配置字符串。
	for _, doc := range docs {
		buf.WriteString("---")
		buf.WriteString("
")
		buf.WriteString(doc)
	}

	config.Data = buf.String()
	return config, nil
}
