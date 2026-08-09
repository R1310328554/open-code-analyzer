// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"log"

	"github.com/gin-gonic/gin/binding"
)

// BindWith 使用指定的 binding 引擎将请求数据绑定到传入的结构体指针。
// 详见 binding 包。
//
// Deprecated: 请改用 MustBindWith 或 ShouldBindWith。
func (c *Context) BindWith(obj any, b binding.Binding) error {
	log.Println(`BindWith(\"any, binding.Binding\") error is going to
	be deprecated, please check issue #662 and either use MustBindWith() if you
	want HTTP 400 to be automatically returned if any error occur, or use
	ShouldBindWith() if you need to manage the error.`)
	return c.MustBindWith(obj, b)
}
