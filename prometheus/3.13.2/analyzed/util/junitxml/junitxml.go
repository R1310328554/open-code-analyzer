// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// JUnit XML 测试报告构建器：testsuites/testsuite/testcase 层次结构与失败/错误计数。

package junitxml

import (
	"encoding/xml"
	"io"
)

type JUnitXML struct {
	XMLName xml.Name     `xml:"testsuites"`
	Suites  []*TestSuite `xml:"testsuite"`
}

type TestSuite struct {
	Name         string      `xml:"name,attr"`
	TestCount    int         `xml:"tests,attr"`
	FailureCount int         `xml:"failures,attr"`
	ErrorCount   int         `xml:"errors,attr"`
	SkippedCount int         `xml:"skipped,attr"`
	Timestamp    string      `xml:"timestamp,attr"`
	Cases        []*TestCase `xml:"testcase"`
}
// TestCase 表示单个测试用例及其 failure/error 文本。
type TestCase struct {
	Name     string   `xml:"name,attr"`
	Failures []string `xml:"failure,omitempty"`
	Error    string   `xml:"error,omitempty"`
}

func (j *JUnitXML) WriteXML(h io.Writer) error {
	return xml.NewEncoder(h).Encode(j)
}

// Suite 追加并返回新的 TestSuite。
func (j *JUnitXML) Suite(name string) *TestSuite {
	ts := &TestSuite{Name: name}
	j.Suites = append(j.Suites, ts)
	return ts
}

func (ts *TestSuite) Fail(f string) {
	ts.FailureCount++
	curt := ts.lastCase()
	curt.Failures = append(curt.Failures, f)
}

// lastCase 返回最后一个 testcase，若无则创建名为 unknown 的占位。
func (ts *TestSuite) lastCase() *TestCase {
	if len(ts.Cases) == 0 {
		ts.Case("unknown")
	}
	return ts.Cases[len(ts.Cases)-1]
}

func (ts *TestSuite) Case(name string) *TestSuite {
	j := &TestCase{
		Name: name,
	}
	ts.Cases = append(ts.Cases, j)
	ts.TestCount++
	return ts
}

// Settime 设置 testsuite 的 timestamp 属性。
func (ts *TestSuite) Settime(name string) {
	ts.Timestamp = name
}

func (ts *TestSuite) Abort(e error) {
	ts.ErrorCount++
	curt := ts.lastCase()
	curt.Error = e.Error()
}
