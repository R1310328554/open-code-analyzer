// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package converter

import (
	"context"

	"github.com/drone/drone/core"
)

// Legacy 创建旧版 Drone 0.8 YAML 配置的直通转换服务（非 OSS 构建）。
func Legacy(enabled bool) core.ConvertService {
	return &legacyPlugin{
		enabled: enabled,
	}
}

// legacyPlugin 启用时原样返回输入配置，不做格式迁移。
type legacyPlugin struct {
	enabled bool
}

// Convert 若插件启用则返回 req.Config 中的原始 YAML 数据。
func (p *legacyPlugin) Convert(ctx context.Context, req *core.ConvertArgs) (*core.Config, error) {
	if p.enabled == false {
		return nil, nil
	}
	return &core.Config{
		Data: req.Config.Data,
	}, nil
}
