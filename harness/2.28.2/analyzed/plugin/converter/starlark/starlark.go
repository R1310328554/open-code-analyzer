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

package starlark

import (
	"bytes"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/errors"

	"github.com/sirupsen/logrus"
	"go.starlark.net/starlark"
)

// separator 与 newline 用于拼接多文档 YAML 输出。
const (
	separator = "---"
	newline   = "\n"
)

// defaultSizeLimit 为生成配置文件的默认最大字节数（1MB）。
const defaultSizeLimit = 1000000

var (
	// ErrMainMissing 表示 Starlark 脚本未定义 main 函数。
	ErrMainMissing = errors.New("starlark: missing main function")

	// ErrMainInvalid 表示 main 存在但不可调用。
	ErrMainInvalid = errors.New("starlark: main must be a function")

	// ErrMainReturn 表示 main 返回值类型不是 List 或 Dict。
	ErrMainReturn = errors.New("starlark: main returns an invalid type")

	// ErrMaximumSize 表示生成的配置超过 sizeLimit。
	ErrMaximumSize = errors.New("starlark: maximum file size exceeded")

	// ErrCannotLoad 表示脚本尝试 load 外部文件（当前禁止）。
	ErrCannotLoad = errors.New("starlark: cannot load external scripts")
)

// Parse 执行 Starlark 文件、调用 main(context)，将返回值序列化为 YAML/JSON 风格文本。
func Parse(req *core.ConvertArgs, template *core.Template, templateData map[string]interface{}, stepLimit uint64, sizeLimit uint64) (string, error) {
	thread := &starlark.Thread{
		Name: "drone",
		Load: noLoad,
		Print: func(_ *starlark.Thread, msg string) {
			logrus.WithFields(logrus.Fields{
				"namespace": req.Repo.Namespace,
				"name":      req.Repo.Name,
			}).Traceln(msg)
		},
	}
	var starlarkFile string
	var starlarkFileName string
	if template != nil {
		starlarkFile = template.Data
		starlarkFileName = template.Name
	} else {
		starlarkFile = req.Config.Data
		starlarkFileName = req.Repo.Config
	}

	globals, err := starlark.ExecFile(thread, starlarkFileName, starlarkFile, nil)
	if err != nil {
		return "", err
	}

	// find the main method in the starlark script and
	// cast to a callable type. If not callable the script
	// is invalid.
	mainVal, ok := globals["main"]
	if !ok {
		return "", ErrMainMissing
	}
	main, ok := mainVal.(starlark.Callable)
	if !ok {
		return "", ErrMainInvalid
	}

	// create the input args and invoke the main method
	// using the input args.
	args, err := createArgs(req.Repo, req.Build, templateData)
	if err != nil {
		return "", err
	}

	// set the maximum number of operations in the script. this
	// mitigates long running scripts.
	if stepLimit == 0 {
		stepLimit = 50000
	}
	thread.SetMaxExecutionSteps(stepLimit)

	// execute the main method in the script.
	mainVal, err = starlark.Call(thread, main, args, nil)
	if err != nil {
		return "", err
	}

	buf := new(bytes.Buffer)
	switch v := mainVal.(type) {
	case *starlark.List:
		for i := 0; i < v.Len(); i++ {
			item := v.Index(i)
			buf.WriteString(separator)
			buf.WriteString(newline)
			if err := write(buf, item); err != nil {
				return "", err
			}
			buf.WriteString(newline)
		}
	case *starlark.Dict:
		if err := write(buf, v); err != nil {
			return "", err
		}
	default:
		return "", ErrMainReturn
	}

	if sizeLimit == 0 {
		sizeLimit = defaultSizeLimit
	}

	// this is a temporary workaround until we
	// implement a LimitWriter.
	if b := buf.Bytes(); uint64(len(b)) > sizeLimit {
		return "", ErrMaximumSize
	}
	return buf.String(), nil
}

// noLoad 拒绝所有外部 load 请求，返回 ErrCannotLoad。
func noLoad(_ *starlark.Thread, _ string) (starlark.StringDict, error) {
	return nil, ErrCannotLoad
}
