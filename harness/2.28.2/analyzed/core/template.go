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

package core

import (
	"context"

	"github.com/drone/drone/handler/api/errors"
)

var (
	errTemplateNameInvalid = errors.New("No Template Name Provided")
	errTemplateDataInvalid = errors.New("No Template Data Provided")
)

type (
	// TemplateArgs 表示模板渲染请求参数。
	TemplateArgs struct {
		Kind string                 // 模板类型
		Load string                 // 加载路径或标识
		Data map[string]interface{} // 渲染变量
	}

	// Template 表示组织级流水线模板定义。
	Template struct {
		Id        int64  `json:"id,omitempty"`        // 主键 ID
		Name      string `json:"name,omitempty"`      // 模板名称
		Namespace string `json:"namespace,omitempty"` // 所属命名空间（组织）
		Data      string `json:"data,omitempty"`      // 模板 YAML 内容
		Created   int64  `json:"created,omitempty"`   // 创建时间戳
		Updated   int64  `json:"updated,omitempty"`   // 更新时间戳
	}

	// TemplateStore 管理组织级流水线模板的持久化与查询。
	TemplateStore interface {
		// List 返回指定命名空间下的模板列表。
		List(ctx context.Context, namespace string) ([]*Template, error)

		// ListAll 返回数据存储中的全部模板。
		ListAll(ctx context.Context) ([]*Template, error)

		// Find 按主键 ID 从数据存储查找模板。
		Find(ctx context.Context, id int64) (*Template, error)

		// FindName 按名称与命名空间从数据存储查找模板。
		FindName(ctx context.Context, name string, namespace string) (*Template, error)

		// Create 将新模板持久化到数据存储。
		Create(ctx context.Context, template *Template) error

		// Update 将更新后的模板持久化到数据存储。
		Update(ctx context.Context, template *Template) error

		// Delete 从数据存储删除模板。
		Delete(ctx context.Context, template *Template) error
	}
)

// Validate 校验模板必填字段与格式，不合法时返回对应错误。
func (s *Template) Validate() error {
	switch {
	case len(s.Name) == 0:
		return errTemplateNameInvalid
	case len(s.Data) == 0:
		return errTemplateDataInvalid
	default:
		return nil
	}
}
