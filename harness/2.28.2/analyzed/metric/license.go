// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package metric

import "github.com/drone/drone/core"

// License 注册许可证相关 Prometheus 指标（到期天数、用户/仓库上限等）。
func License(license core.LicenseService) {
	// track days until expires
	// track user limit
	// track repo limit
}
