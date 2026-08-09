// Copyright 2017 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package binding

import (
	"reflect"
	"strconv"
	"strings"
	"sync"

	"github.com/go-playground/validator/v10"
)

type defaultValidator struct {
	once     sync.Once
	validate *validator.Validate
}

type SliceValidationError []error

// Error 将 SliceValidationError 中所有错误用换行符拼接为单个字符串。
func (err SliceValidationError) Error() string {
	if len(err) == 0 {
		return ""
	}

	var b strings.Builder
	for i := range len(err) {
		if err[i] != nil {
			if b.Len() > 0 {
				b.WriteString("\n")
			}
			b.WriteString("[" + strconv.Itoa(i) + "]: " + err[i].Error())
		}
	}
	return b.String()
}

var _ StructValidator = (*defaultValidator)(nil)

// ValidateStruct 接收任意类型，但仅对 struct 或 struct 指针执行校验。
func (v *defaultValidator) ValidateStruct(obj any) error {
	if obj == nil {
		return nil
	}

	value := reflect.ValueOf(obj)
	switch value.Kind() {
	case reflect.Ptr:
		if value.Elem().Kind() != reflect.Struct {
			return v.ValidateStruct(value.Elem().Interface())
		}
		return v.validateStruct(obj)
	case reflect.Struct:
		return v.validateStruct(obj)
	case reflect.Slice, reflect.Array:
		count := value.Len()
		validateRet := make(SliceValidationError, 0)
		for i := range count {
			if err := v.ValidateStruct(value.Index(i).Interface()); err != nil {
				validateRet = append(validateRet, err)
			}
		}
		if len(validateRet) == 0 {
			return nil
		}
		return validateRet
	default:
		return nil
	}
}

// validateStruct 对 struct 类型执行校验。
func (v *defaultValidator) validateStruct(obj any) error {
	v.lazyinit()
	return v.validate.Struct(obj)
}

// Engine 返回驱动默认 Validator 实例的底层校验引擎。
// 可用于注册自定义校验或结构体级别校验。
// 详见 validator 文档：https://pkg.go.dev/github.com/go-playground/validator/v10
func (v *defaultValidator) Engine() any {
	v.lazyinit()
	return v.validate
}

func (v *defaultValidator) lazyinit() {
	v.once.Do(func() {
		v.validate = validator.New()
		v.validate.SetTagName("binding")
	})
}
