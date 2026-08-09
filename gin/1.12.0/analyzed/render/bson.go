// Copyright 2025 Gin Core Team. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package render

import (
	"net/http"

	"go.mongodb.org/mongo-driver/v2/bson"
)

// BSON 包含给定的接口对象。
type BSON struct {
	Data any
}

var bsonContentType = []string{"application/bson"}

// 渲染 (BSON) 编组给定的接口对象并使用自定义 ContentType 写入数据。
func (r BSON) Render(w http.ResponseWriter) error {
	r.WriteContentType(w)

	bytes, err := bson.Marshal(&r.Data)
	if err == nil {
		_, err = w.Write(bytes)
	}
	return err
}

// WriteContentType (BSONBuf) 写入 BSONBuf ContentType。
func (r BSON) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, bsonContentType)
}
