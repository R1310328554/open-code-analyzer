// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// converter 包（非 OSS 构建）实现 Jsonnet 到 YAML 的流水线配置转换。
package converter

import (
	"context"
	"strings"

	"github.com/drone/drone/core"
	"github.com/drone/drone/plugin/converter/jsonnet"
)

// TODO(bradrydzewski) handle jsonnet imports
// TODO(bradrydzewski) handle jsonnet object vs array output

// Jsonnet 创建 Jsonnet 转换服务：当仓库配置为 .jsonnet 时将源文件求值为 YAML 字符串。
func Jsonnet(enabled bool, limit int, fileService core.FileService) core.ConvertService {
	return &jsonnetPlugin{
		enabled:     enabled,
		limit:       limit,
		fileService: fileService,
	}
}

// jsonnetPlugin 委托 jsonnet 子包解析并求值 Jsonnet 配置。
type jsonnetPlugin struct {
	enabled     bool
	limit       int
	fileService core.FileService
}

// Convert 若插件启用且配置路径以 .jsonnet 结尾，则调用 jsonnet.Parse 完成转换。
func (p *jsonnetPlugin) Convert(ctx context.Context, req *core.ConvertArgs) (*core.Config, error) {
	if p.enabled == false {
		return nil, nil
	}

	// 非 .jsonnet 扩展名时跳过本插件，返回零值。
	if strings.HasSuffix(req.Repo.Config, ".jsonnet") == false {
		return nil, nil
	}

	file, err := jsonnet.Parse(req, p.fileService, p.limit, nil, nil)

	if err != nil {
		return nil, err
	}
	return &core.Config{
		Data: file,
	}, nil
}
