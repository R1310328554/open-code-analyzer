// Copyright 2017 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

//go:build appengine

package gin

func init() {
	// 在 Google App Engine 环境下将默认平台设为 App Engine
	defaultPlatform = PlatformGoogleAppEngine
}
