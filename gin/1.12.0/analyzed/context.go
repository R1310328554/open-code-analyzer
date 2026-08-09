// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"errors"
	"fmt"
	"io"
	"io/fs"
	"log"
	"maps"
	"math"
	"mime/multipart"
	"net"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"time"

	"github.com/gin-contrib/sse"
	"github.com/gin-gonic/gin/binding"
	"github.com/gin-gonic/gin/render"
)

// 最常见数据格式的 Content-Type MIME。
const (
	MIMEJSON              = binding.MIMEJSON
	MIMEHTML              = binding.MIMEHTML
	MIMEXML               = binding.MIMEXML
	MIMEXML2              = binding.MIMEXML2
	MIMEPlain             = binding.MIMEPlain
	MIMEPOSTForm          = binding.MIMEPOSTForm
	MIMEMultipartPOSTForm = binding.MIMEMultipartPOSTForm
	MIMEYAML              = binding.MIMEYAML
	MIMEYAML2             = binding.MIMEYAML2
	MIMETOML              = binding.MIMETOML
	MIMEPROTOBUF          = binding.MIMEPROTOBUF
	MIMEBSON              = binding.MIMEBSON
)

// BodyBytesKey 表示默认的主体字节密钥。
const BodyBytesKey = "_gin-gonic/gin/bodybyteskey"

// ContextKey 是 Context 返回自身的键。
const ContextKey = "_gin-gonic/gin/contextkey"

type ContextKeyType int

const ContextRequestKey ContextKeyType = 0

// abortIndex 表示中止函数中使用的典型值。
const abortIndex int8 = math.MaxInt8 >> 1

// 背景是杜松子酒最重要的部分。它允许我们在中间件之间传递变量，
//  例如，管理流程、验证请求的 JSON 并呈现 JSON 响应。
type Context struct {
	writermem responseWriter
	Request   *http.Request
	Writer    ResponseWriter

	Params   Params
	handlers HandlersChain
	index    int8
	fullPath string

	engine       *Engine
	params       *Params
	skippedNodes *[]skippedNode

	// 该互斥锁保护键映射。
	mu sync.RWMutex

	// Keys 是专门用于每个请求上下文的键/值对。
	Keys map[any]any

	// 错误是附加到使用此上下文的所有处理程序/中间件的错误列表。
	Errors errorMsgs

	// 已接受定义了用于内容协商的手动接受格式的列表。
	Accepted []string

	// queryCache 缓存 c.Request.URL.Query() 的查询结果。
	queryCache url.Values

	// formCache缓存c.Request.PostForm，其中包含从POST、PATCH、
	//  或 PUT 主体参数。
	formCache url.Values

	// SameSite 允许服务器定义 cookie 属性，使其无法
	//  浏览器将此 cookie 与跨站点请求一起发送。
	sameSite http.SameSite
}

/************************************/
/********** CONTEXT CREATION ********/
/************************************/

func (c *Context) reset() {
	c.Writer = &c.writermem
	c.Params = c.Params[:0]
	c.handlers = nil
	c.index = -1

	c.fullPath = ""
	c.Keys = nil
	c.Errors = c.Errors[:0]
	c.Accepted = nil
	c.queryCache = nil
	c.formCache = nil
	c.sameSite = 0
	*c.params = (*c.params)[:0]
	*c.skippedNodes = (*c.skippedNodes)[:0]
}

// Copy 返回当前上下文的副本，可以在请求范围之外安全地使用该副本。
//  当上下文必须传递给 goroutine 时必须使用它。
func (c *Context) Copy() *Context {
	cp := Context{
		writermem: c.writermem,
		Request:   c.Request,
		engine:    c.engine,
	}

	cp.writermem.ResponseWriter = nil
	cp.Writer = &cp.writermem
	cp.index = abortIndex
	cp.handlers = nil
	cp.fullPath = c.fullPath

	cKeys := c.Keys
	c.mu.RLock()
	cp.Keys = maps.Clone(cKeys)
	c.mu.RUnlock()

	cParams := c.Params
	cp.Params = make([]Param, len(cParams))
	copy(cp.Params, cParams)

	return &cp
}

// HandlerName 返回主处理程序的名称。例如，如果处理程序是“handleGetUsers()”，
//  该函数将返回“main.handleGetUsers”。
func (c *Context) HandlerName() string {
	return nameOfFunction(c.handlers.Last())
}

// HandlerNames 按降序返回此上下文的所有已注册处理程序的列表，
//  遵循 HandlerName() 的语义
func (c *Context) HandlerNames() []string {
	hn := make([]string, 0, len(c.handlers))
	for _, val := range c.handlers {
		if val == nil {
			continue
		}
		hn = append(hn, nameOfFunction(val))
	}
	return hn
}

// 处理程序返回主处理程序。
func (c *Context) Handler() HandlerFunc {
	return c.handlers.Last()
}

// FullPath 返回匹配的路由完整路径。对于未找到的路线
//  返回一个空字符串。
//
// 	router.GET("/user/:id", func(c *gin.Context) {
// 	    c.FullPath() == "/user/:id" // true
// 	})
func (c *Context) FullPath() string {
	return c.fullPath
}

/************************************/
/*********** FLOW CONTROL ***********/
/************************************/

// Next 只能在中间件内部使用。
//  它执行调用处理程序内链中的待处理处理程序。
//  请参阅 GitHub 中的示例。
func (c *Context) Next() {
	c.index++
	for c.index < safeInt8(len(c.handlers)) {
		if c.handlers[c.index] != nil {
			c.handlers[c.index](c)
		}
		c.index++
	}
}

// 如果当前上下文已中止，则 IsAborted 返回 true。
func (c *Context) IsAborted() bool {
	return c.index >= abortIndex
}

// 中止可防止调用挂起的处理程序。请注意，这不会停止当前处理程序。
//  假设您有一个授权中间件来验证当前请求是否已获得授权。
//  如果授权失败（例如：密码不匹配），请调用 Abort 以确保剩余的处理程序
//  此请求不会被调用。
func (c *Context) Abort() {
	c.index = abortIndex
}

// AbortWithStatus 调用 `Abort()` 并写入具有指定状态代码的标头。
//  例如，尝试验证请求失败可以使用：context.AbortWithStatus(401)。
func (c *Context) AbortWithStatus(code int) {
	c.Status(code)
	c.Writer.WriteHeaderNow()
	c.Abort()
}

// AbortWithStatusPureJSON 在内部调用 `Abort()`，然后调用 `PureJSON`。
//  此方法停止链，写入状态代码并返回 JSON 正文而不转义。
//  它还将 Content-Type 设置为“application/json”。
func (c *Context) AbortWithStatusPureJSON(code int, jsonObj any) {
	c.Abort()
	c.PureJSON(code, jsonObj)
}

// AbortWithStatusJSON 在内部调用 `Abort()`，然后调用 `JSON`。
//  此方法停止链、写入状态代码并返回 JSON 正文。
//  它还将 Content-Type 设置为“application/json”。
func (c *Context) AbortWithStatusJSON(code int, jsonObj any) {
	c.Abort()
	c.JSON(code, jsonObj)
}

// AbortWithError 在内部调用 `AbortWithStatus()` 和 `Error()`。
//  此方法停止链，写入状态代码并将指定的错误推送到 `c.Errors`。
//  有关更多详细信息，请参阅 Context.Error()。
func (c *Context) AbortWithError(code int, err error) *Error {
	c.AbortWithStatus(code)
	return c.Error(err)
}

/************************************/
/********* ERROR MANAGEMENT *********/
/************************************/

// 错误将错误附加到当前上下文。该错误被推送到错误列表中。
//  对于请求解析过程中发生的每个错误，调用 Error 是一个好主意。
//  中间件可用于收集所有错误并将它们一起推送到数据库，
//  打印日志，或将其附加到 HTTP 响应中。
//  如果 err 为零，错误将发生恐慌。
func (c *Context) Error(err error) *Error {
	if err == nil {
		panic("err is nil")
	}

	var parsedError *Error
	ok := errors.As(err, &parsedError)
	if !ok {
		parsedError = &Error{
			Err:  err,
			Type: ErrorTypePrivate,
		}
	}

	c.Errors = append(c.Errors, parsedError)
	return parsedError
}

/************************************/
/******** METADATA MANAGEMENT********/
/************************************/

// Set 用于专门存储此上下文的新键/值对。
//  如果之前未使用过，它还会延迟初始化 c.Keys。
func (c *Context) Set(key any, value any) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.Keys == nil {
		c.Keys = make(map[any]any)
	}

	c.Keys[key] = value
}

// Get 返回给定键的值，即：(value, true)。
//  如果值不存在则返回 (nil, false)
func (c *Context) Get(key any) (value any, exists bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()
	value, exists = c.Keys[key]
	return
}

// MustGet 返回给定键的值（如果存在），否则会出现恐慌。
func (c *Context) MustGet(key any) any {
	if value, exists := c.Get(key); exists {
		return value
	}
	panic(fmt.Sprintf("key %v does not exist", key))
}

func getTyped[T any](c *Context, key any) (res T) {
	if val, ok := c.Get(key); ok && val != nil {
		res, _ = val.(T)
	}
	return
}

// GetString 以字符串形式返回与键关联的值。
func (c *Context) GetString(key any) string {
	return getTyped[string](c, key)
}

// GetBool 以布尔值形式返回与键关联的值。
func (c *Context) GetBool(key any) bool {
	return getTyped[bool](c, key)
}

// GetInt 以整数形式返回与键关联的值。
func (c *Context) GetInt(key any) int {
	return getTyped[int](c, key)
}

// GetInt8 以整数 8 的形式返回与键关联的值。
func (c *Context) GetInt8(key any) int8 {
	return getTyped[int8](c, key)
}

// GetInt16 以整数 16 的形式返回与键关联的值。
func (c *Context) GetInt16(key any) int16 {
	return getTyped[int16](c, key)
}

// GetInt32 以整数 32 的形式返回与键关联的值。
func (c *Context) GetInt32(key any) int32 {
	return getTyped[int32](c, key)
}

// GetInt64 以整数 64 形式返回与键关联的值。
func (c *Context) GetInt64(key any) int64 {
	return getTyped[int64](c, key)
}

// GetUint 以无符号整数形式返回与键关联的值。
func (c *Context) GetUint(key any) uint {
	return getTyped[uint](c, key)
}

// GetUint8 以无符号整数 8 的形式返回与键关联的值。
func (c *Context) GetUint8(key any) uint8 {
	return getTyped[uint8](c, key)
}

// GetUint16 以无符号整数 16 形式返回与键关联的值。
func (c *Context) GetUint16(key any) uint16 {
	return getTyped[uint16](c, key)
}

// GetUint32 以无符号整数 32 形式返回与键关联的值。
func (c *Context) GetUint32(key any) uint32 {
	return getTyped[uint32](c, key)
}

// GetUint64 以无符号整数 64 形式返回与键关联的值。
func (c *Context) GetUint64(key any) uint64 {
	return getTyped[uint64](c, key)
}

// GetFloat32 以 float32 形式返回与键关联的值。
func (c *Context) GetFloat32(key any) float32 {
	return getTyped[float32](c, key)
}

// GetFloat64 以 float64 形式返回与键关联的值。
func (c *Context) GetFloat64(key any) float64 {
	return getTyped[float64](c, key)
}

// GetTime 返回与键关联的值作为时间。
func (c *Context) GetTime(key any) time.Time {
	return getTyped[time.Time](c, key)
}

// GetDuration 返回与键关联的值作为持续时间。
func (c *Context) GetDuration(key any) time.Duration {
	return getTyped[time.Duration](c, key)
}

// GetError 将与该键关联的值作为错误返回。
func (c *Context) GetError(key any) error {
	return getTyped[error](c, key)
}

// GetIntSlice 以整数切片的形式返回与键关联的值。
func (c *Context) GetIntSlice(key any) []int {
	return getTyped[[]int](c, key)
}

// GetInt8Slice 以 int8 整数切片的形式返回与键关联的值。
func (c *Context) GetInt8Slice(key any) []int8 {
	return getTyped[[]int8](c, key)
}

// GetInt16Slice 以 int16 整数切片的形式返回与键关联的值。
func (c *Context) GetInt16Slice(key any) []int16 {
	return getTyped[[]int16](c, key)
}

// GetInt32Slice 以 int32 整数切片的形式返回与键关联的值。
func (c *Context) GetInt32Slice(key any) []int32 {
	return getTyped[[]int32](c, key)
}

// GetInt64Slice 以 int64 整数切片的形式返回与键关联的值。
func (c *Context) GetInt64Slice(key any) []int64 {
	return getTyped[[]int64](c, key)
}

// GetUintSlice 将与键关联的值作为无符号整数切片返回。
func (c *Context) GetUintSlice(key any) []uint {
	return getTyped[[]uint](c, key)
}

// GetUint8Slice 以 uint8 整数切片的形式返回与键关联的值。
func (c *Context) GetUint8Slice(key any) []uint8 {
	return getTyped[[]uint8](c, key)
}

// GetUint16Slice 以 uint16 整数切片的形式返回与键关联的值。
func (c *Context) GetUint16Slice(key any) []uint16 {
	return getTyped[[]uint16](c, key)
}

// GetUint32Slice 以 uint32 整数切片的形式返回与键关联的值。
func (c *Context) GetUint32Slice(key any) []uint32 {
	return getTyped[[]uint32](c, key)
}

// GetUint64Slice 将与键关联的值作为 uint64 整数切片返回。
func (c *Context) GetUint64Slice(key any) []uint64 {
	return getTyped[[]uint64](c, key)
}

// GetFloat32Slice 将与键关联的值作为 float32 数字的切片返回。
func (c *Context) GetFloat32Slice(key any) []float32 {
	return getTyped[[]float32](c, key)
}

// GetFloat64Slice 将与键关联的值作为 float64 数字的切片返回。
func (c *Context) GetFloat64Slice(key any) []float64 {
	return getTyped[[]float64](c, key)
}

// GetStringSlice 以字符串切片的形式返回与键关联的值。
func (c *Context) GetStringSlice(key any) []string {
	return getTyped[[]string](c, key)
}

// GetErrorSlice 返回与键关联的值作为错误切片。
func (c *Context) GetErrorSlice(key any) []error {
	return getTyped[[]error](c, key)
}

// GetStringMap 返回与键关联的值作为接口映射。
func (c *Context) GetStringMap(key any) map[string]any {
	return getTyped[map[string]any](c, key)
}

// GetStringMapString 返回与键关联的值作为字符串映射。
func (c *Context) GetStringMapString(key any) map[string]string {
	return getTyped[map[string]string](c, key)
}

// GetStringMapStringSlice 返回与键关联的值作为字符串切片的映射。
func (c *Context) GetStringMapStringSlice(key any) map[string][]string {
	return getTyped[map[string][]string](c, key)
}

// 删除将从上下文的键映射中删除该键（如果存在）。
//  此操作可以安全地由并发 go 例程使用
func (c *Context) Delete(key any) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.Keys != nil {
		delete(c.Keys, key)
	}
}

/************************************/
/************ INPUT DATA ************/
/************************************/

// 参数返回 URL 参数的值。
//  它是 c.Params.ByName(key) 的快捷方式
//
// 	router.GET("/user/:id", func(c *gin.Context) {
// 	    // a GET request to /user/john
// 	    id := c.Param("id") // id == "john"
// 	    // a GET request to /user/john/
// 	    id := c.Param("id") // id == "/john/"
// 	})
func (c *Context) Param(key string) string {
	return c.Params.ByName(key)
}

// AddParam 将参数添加到上下文中并
//  将路径参数键替换为给定值以用于 e2e 测试目的
//  示例路由：“/user/:id”
//  添加参数（“id”，1）
//  结果：“/用户/1”
func (c *Context) AddParam(key, value string) {
	c.Params = append(c.Params, Param{Key: key, Value: value})
}

// 查询返回键控的 url 查询值（如果存在），
//  否则返回空字符串 `("")`。
//  它是 `c.Request.URL.Query().Get(key)` 的快捷方式
//
// 	    GET /path?id=1234&name=Manu&value=
// 		   c.Query("id") == "1234"
// 		   c.Query("name") == "Manu"
// 		   c.Query("value") == ""
// 		   c.Query("wtf") == ""
func (c *Context) Query(key string) (value string) {
	value, _ = c.GetQuery(key)
	return
}

// DefaultQuery 返回键控 url 查询值（如果存在），
//  否则返回指定的defaultValue 字符串。
//  请参阅：Query() 和 GetQuery() 了解更多信息。
//
// 	GET /?name=Manu&lastname=
// 	c.DefaultQuery("name", "unknown") == "Manu"
// 	c.DefaultQuery("id", "none") == "none"
// 	c.DefaultQuery("lastname", "none") == ""
func (c *Context) DefaultQuery(key, defaultValue string) string {
	if value, ok := c.GetQuery(key); ok {
		return value
	}
	return defaultValue
}

// GetQuery 类似于 Query()，它返回带键的 url 查询值
//  如果存在 `(value, true)`（即使该值为空字符串），
//  否则返回 `("", false)`。
//  这是 `c.Request.URL.Query().Get(key)` 的快捷方式
//
// 	GET /?name=Manu&lastname=
// 	("Manu", true) == c.GetQuery("name")
// 	("", false) == c.GetQuery("id")
// 	("", true) == c.GetQuery("lastname")
func (c *Context) GetQuery(key string) (string, bool) {
	if values, ok := c.GetQueryArray(key); ok {
		return values[0], ok
	}
	return "", false
}

// QueryArray 返回给定查询键的字符串切片。
//  切片的长度取决于给定键的参数数量。
func (c *Context) QueryArray(key string) (values []string) {
	values, _ = c.GetQueryArray(key)
	return
}

func (c *Context) initQueryCache() {
	if c.queryCache == nil {
		if c.Request != nil && c.Request.URL != nil {
			c.queryCache = c.Request.URL.Query()
		} else {
			c.queryCache = url.Values{}
		}
	}
}

// GetQueryArray 返回给定查询键的字符串切片，加上
//  一个布尔值，给定的键是否至少存在一个值。
func (c *Context) GetQueryArray(key string) (values []string, ok bool) {
	c.initQueryCache()
	values, ok = c.queryCache[key]
	return
}

// QueryMap 返回给定查询键的映射。
func (c *Context) QueryMap(key string) (dicts map[string]string) {
	dicts, _ = c.GetQueryMap(key)
	return
}

// GetQueryMap 返回给定查询键的映射以及布尔值
//  给定键是否至少存在一个值。
func (c *Context) GetQueryMap(key string) (map[string]string, bool) {
	c.initQueryCache()
	return getMapFromFormData(c.queryCache, key)
}

// PostForm 从 POST urlencoded 表单或多部分表单返回指定的键
//  如果存在，否则返回空字符串 `("")`。
func (c *Context) PostForm(key string) (value string) {
	value, _ = c.GetPostForm(key)
	return
}

// DefaultPostForm 从 POST urlencoded 表单或多部分表单返回指定的键
//  如果存在，否则返回指定的defaultValue 字符串。
//  请参阅：PostForm() 和 GetPostForm() 了解更多信息。
func (c *Context) DefaultPostForm(key, defaultValue string) string {
	if value, ok := c.GetPostForm(key); ok {
		return value
	}
	return defaultValue
}

// GetPostForm 类似于 PostForm(key)。它从 POST urlencoded 返回指定的键
//  表单或多部分表单（当存在 `(value, true)` 时）（即使该值为空字符串），
//  否则返回 ("", false)。
//  例如，在更新用户电子邮件的 PATCH 请求期间：
//
// 	    email=mail@example.com  -->  ("mail@example.com", true) := GetPostForm("email") // set email to "mail@example.com"
// 		   email=                  -->  ("", true) := GetPostForm("email") // set email to ""
// 	                            -->  ("", false) := GetPostForm("email") // do nothing with email
func (c *Context) GetPostForm(key string) (string, bool) {
	if values, ok := c.GetPostFormArray(key); ok {
		return values[0], ok
	}
	return "", false
}

// PostFormArray 返回给定表单键的字符串切片。
//  切片的长度取决于给定键的参数数量。
func (c *Context) PostFormArray(key string) (values []string) {
	values, _ = c.GetPostFormArray(key)
	return
}

func (c *Context) initFormCache() {
	if c.formCache == nil {
		c.formCache = make(url.Values)
		req := c.Request
		if err := req.ParseMultipartForm(c.engine.MaxMultipartMemory); err != nil {
			if !errors.Is(err, http.ErrNotMultipart) {
				debugPrint("error on parse multipart form array: %v", err)
			}
		}
		c.formCache = req.PostForm
	}
}

// GetPostFormArray 返回给定表单键的字符串切片，加上
//  一个布尔值，给定的键是否至少存在一个值。
func (c *Context) GetPostFormArray(key string) (values []string, ok bool) {
	c.initFormCache()
	values, ok = c.formCache[key]
	return
}

// PostFormMap 返回给定表单键的映射。
func (c *Context) PostFormMap(key string) (dicts map[string]string) {
	dicts, _ = c.GetPostFormMap(key)
	return
}

// GetPostFormMap 返回给定表单键的映射以及布尔值
//  给定键是否至少存在一个值。
func (c *Context) GetPostFormMap(key string) (map[string]string, bool) {
	c.initFormCache()
	return getMapFromFormData(c.formCache, key)
}

// getMapFromFormData 返回满足条件的地图。
//  它将带有括号符号（如“key[subkey]=value”）的数据解析为映射。
func getMapFromFormData(m map[string][]string, key string) (map[string]string, bool) {
	d := make(map[string]string)
	found := false
	keyLen := len(key)

	for k, v := range m {
		if len(k) < keyLen+3 { // key + "[" + at least one char + "]"
			continue
		}

		if k[:keyLen] != key || k[keyLen] != '[' {
			continue
		}

		if j := strings.IndexByte(k[keyLen+1:], ']'); j > 0 {
			found = true
			d[k[keyLen+1:keyLen+1+j]] = v[0]
		}
	}

	return d, found
}

// FormFile 返回所提供的表单密钥的第一个文件。
func (c *Context) FormFile(name string) (*multipart.FileHeader, error) {
	if c.Request.MultipartForm == nil {
		if err := c.Request.ParseMultipartForm(c.engine.MaxMultipartMemory); err != nil {
			return nil, err
		}
	}
	f, fh, err := c.Request.FormFile(name)
	if err != nil {
		return nil, err
	}
	f.Close()
	return fh, err
}

// MultipartForm 是解析后的多部分表单，包括文件上传。
func (c *Context) MultipartForm() (*multipart.Form, error) {
	err := c.Request.ParseMultipartForm(c.engine.MaxMultipartMemory)
	return c.Request.MultipartForm, err
}

// SaveUploadedFile 将表单文件上传到特定目标。
func (c *Context) SaveUploadedFile(file *multipart.FileHeader, dst string, perm ...fs.FileMode) error {
	src, err := file.Open()
	if err != nil {
		return err
	}
	defer src.Close()

	var mode os.FileMode = 0o750
	if len(perm) > 0 {
		mode = perm[0]
	}
	dir := filepath.Dir(dst)
	if err = os.MkdirAll(dir, mode); err != nil {
		return err
	}
	if err = os.Chmod(dir, mode); err != nil {
		return err
	}

	out, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer out.Close()

	_, err = io.Copy(out, src)
	return err
}

// Bind 检查 Method 和 Content-Type 以自动选择绑定引擎，
//  根据“Content-Type”标头，使用不同的绑定，例如：
//
// 	"application/json" --> JSON binding
// 	"application/xml"  --> XML binding
//
//  它根据内容类型（例如 JSON 或 XML）解析请求的正文。
//  它将有效负载解码为指定为指针的结构。
//  如果输入无效，它会写入 400 错误并在响应中设置 Content-Type 标头“text/plain”。
func (c *Context) Bind(obj any) error {
	b := binding.Default(c.Request.Method, c.ContentType())
	return c.MustBindWith(obj, b)
}

// BindJSON 是 c.MustBindWith(obj, binding.JSON) 的快捷方式。
func (c *Context) BindJSON(obj any) error {
	return c.MustBindWith(obj, binding.JSON)
}

// BindXML 是 c.MustBindWith(obj, binding.BindXML) 的快捷方式。
func (c *Context) BindXML(obj any) error {
	return c.MustBindWith(obj, binding.XML)
}

// BindQuery 是 c.MustBindWith(obj, binding.Query) 的快捷方式。
func (c *Context) BindQuery(obj any) error {
	return c.MustBindWith(obj, binding.Query)
}

// BindYAML 是 c.MustBindWith(obj, binding.YAML) 的快捷方式。
func (c *Context) BindYAML(obj any) error {
	return c.MustBindWith(obj, binding.YAML)
}

// BindTOML 是 c.MustBindWith(obj, binding.TOML) 的快捷方式。
func (c *Context) BindTOML(obj any) error {
	return c.MustBindWith(obj, binding.TOML)
}

// BindPlain 是 c.MustBindWith(obj, binding.Plain) 的快捷方式。
func (c *Context) BindPlain(obj any) error {
	return c.MustBindWith(obj, binding.Plain)
}

// BindHeader 是 c.MustBindWith(obj, binding.Header) 的快捷方式。
func (c *Context) BindHeader(obj any) error {
	return c.MustBindWith(obj, binding.Header)
}

// BindUri 使用 binding.Uri 绑定传递的结构指针。
//  如果发生任何错误，它将中止请求并返回 HTTP 400。
func (c *Context) BindUri(obj any) error {
	if err := c.ShouldBindUri(obj); err != nil {
		c.AbortWithError(http.StatusBadRequest, err).SetType(ErrorTypeBind) //nolint: errcheck
		return err
	}
	return nil
}

// MustBindWith 使用指定的绑定引擎绑定传递的结构指针。
//  如果发生任何错误，它将中止请求并返回 HTTP 400。
//  请参阅绑定包。
func (c *Context) MustBindWith(obj any, b binding.Binding) error {
	err := c.ShouldBindWith(obj, b)
	if err != nil {
		var maxBytesErr *http.MaxBytesError

		// 注意：当使用 sonic 或 go-json 作为 JSON 编码器时，它们不会传播 http.MaxBytesError 错误
		//  https://github.com/goccy/go-json/issues/485
		//  https://github.com/bytedance/sonic/issues/800
		switch {
		case errors.As(err, &maxBytesErr):
			c.AbortWithError(http.StatusRequestEntityTooLarge, err).SetType(ErrorTypeBind) //nolint: errcheck
		default:
			c.AbortWithError(http.StatusBadRequest, err).SetType(ErrorTypeBind) //nolint: errcheck
		}
		return err
	}
	return nil
}

// ShouldBind 检查 Method 和 Content-Type 以自动选择绑定引擎，
//  根据“Content-Type”标头，使用不同的绑定，例如：
//
// 	"application/json" --> JSON binding
// 	"application/xml"  --> XML binding
//
//  它根据内容类型（例如 JSON 或 XML）解析请求的正文。
//  它将有效负载解码为指定为指针的结构。
//  与 c.Bind() 类似，但此方法不会将响应状态代码设置为 400 或在输入无效时中止。
func (c *Context) ShouldBind(obj any) error {
	b := binding.Default(c.Request.Method, c.ContentType())
	return c.ShouldBindWith(obj, b)
}

// ShouldBindJSON 是 c.ShouldBindWith(obj, binding.JSON) 的快捷方式。
//
//  例子：
//
// 	POST /user
// 	Content-Type: application/json
//
// 	Request Body:
// 	{
// 		"name": "Manu",
// 		"age": 20
// 	}
//
// 	type User struct {
// 		Name string `json:"name"`
// 		Age  int    `json:"age"`
// 	}
//
// 	var user User
// 	if err := c.ShouldBindJSON(&user); err != nil {
// 		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
// 		return
// 	}
// 	c.JSON(http.StatusOK, user)
func (c *Context) ShouldBindJSON(obj any) error {
	return c.ShouldBindWith(obj, binding.JSON)
}

// ShouldBindXML 是 c.ShouldBindWith(obj, binding.XML) 的快捷方式。
//  它的工作方式类似于 ShouldBindJSON，但将请求正文绑定为 XML 数据。
func (c *Context) ShouldBindXML(obj any) error {
	return c.ShouldBindWith(obj, binding.XML)
}

// ShouldBindQuery 是 c.ShouldBindWith(obj, binding.Query) 的快捷方式。
//  它的工作方式类似于 ShouldBindJSON，但绑定来自 URL 的查询参数。
func (c *Context) ShouldBindQuery(obj any) error {
	return c.ShouldBindWith(obj, binding.Query)
}

// ShouldBindYAML 是 c.ShouldBindWith(obj, binding.YAML) 的快捷方式。
//  它的工作方式类似于 ShouldBindJSON，但将请求正文绑定为 YAML 数据。
func (c *Context) ShouldBindYAML(obj any) error {
	return c.ShouldBindWith(obj, binding.YAML)
}

// ShouldBindTOML 是 c.ShouldBindWith(obj, binding.TOML) 的快捷方式。
//  它的工作方式类似于 ShouldBindJSON，但将请求正文绑定为 TOML 数据。
func (c *Context) ShouldBindTOML(obj any) error {
	return c.ShouldBindWith(obj, binding.TOML)
}

// ShouldBindPlain 是 c.ShouldBindWith(obj, binding.Plain) 的快捷方式。
//  它的工作方式类似于 ShouldBindJSON，但绑定来自请求正文的纯文本数据。
func (c *Context) ShouldBindPlain(obj any) error {
	return c.ShouldBindWith(obj, binding.Plain)
}

// ShouldBindHeader 是 c.ShouldBindWith(obj, binding.Header) 的快捷方式。
//  它的工作方式类似于 ShouldBindJSON，但绑定来自 HTTP 标头的值。
func (c *Context) ShouldBindHeader(obj any) error {
	return c.ShouldBindWith(obj, binding.Header)
}

// ShouldBindUri 使用指定的绑定引擎绑定传递的结构指针。
//  它的工作方式类似于 ShouldBindJSON，但绑定来自 URI 的参数。
func (c *Context) ShouldBindUri(obj any) error {
	m := make(map[string][]string, len(c.Params))
	for _, v := range c.Params {
		m[v.Key] = []string{v.Value}
	}
	return binding.Uri.BindUri(m, obj)
}

// ShouldBindWith 使用指定的绑定引擎绑定传递的结构指针。
//  请参阅绑定包。
func (c *Context) ShouldBindWith(obj any, b binding.Binding) error {
	return b.Bind(c.Request, obj)
}

// ShouldBindBodyWith 与 ShouldBindWith 类似，但它存储请求
//  body 放入上下文中，并在再次调用时重用。
//
//  注意：此方法在绑定之前读取正文。所以你应该使用
//  如果您只需要调用一次，ShouldBindWith 可以获得更好的性能。
func (c *Context) ShouldBindBodyWith(obj any, bb binding.BindingBody) (err error) {
	var body []byte
	if cb, ok := c.Get(BodyBytesKey); ok {
		if cbb, ok := cb.([]byte); ok {
			body = cbb
		}
	}
	if body == nil {
		body, err = io.ReadAll(c.Request.Body)
		if err != nil {
			return err
		}
		c.Set(BodyBytesKey, body)
	}
	return bb.BindBody(body, obj)
}

// ShouldBindBodyWithJSON 是 c.ShouldBindBodyWith(obj, binding.JSON) 的快捷方式。
func (c *Context) ShouldBindBodyWithJSON(obj any) error {
	return c.ShouldBindBodyWith(obj, binding.JSON)
}

// ShouldBindBodyWithXML 是 c.ShouldBindBodyWith(obj, binding.XML) 的快捷方式。
func (c *Context) ShouldBindBodyWithXML(obj any) error {
	return c.ShouldBindBodyWith(obj, binding.XML)
}

// ShouldBindBodyWithYAML 是 c.ShouldBindBodyWith(obj, binding.YAML) 的快捷方式。
func (c *Context) ShouldBindBodyWithYAML(obj any) error {
	return c.ShouldBindBodyWith(obj, binding.YAML)
}

// ShouldBindBodyWithTOML 是 c.ShouldBindBodyWith(obj, binding.TOML) 的快捷方式。
func (c *Context) ShouldBindBodyWithTOML(obj any) error {
	return c.ShouldBindBodyWith(obj, binding.TOML)
}

// ShouldBindBodyWithPlain 是 c.ShouldBindBodyWith(obj, binding.Plain) 的快捷方式。
func (c *Context) ShouldBindBodyWithPlain(obj any) error {
	return c.ShouldBindBodyWith(obj, binding.Plain)
}

// ClientIP 实现一种尽力而为的算法来返回真实的客户端 IP。
//  它在底层调用 c.RemoteIP() 来检查远程 IP 是否是受信任的代理。
//  如果是，它将尝试解析 Engine.RemoteIPHeaders 中定义的标头（默认为 [X-Forwarded-For, X-Real-IP]）。
//  如果标头在语法上无效或远程 IP 不对应于受信任的代理，
//  返回远程 IP（来自 Request.RemoteAddr）。
func (c *Context) ClientIP() string {
	// 检查我们是否在受信任的平台上运行，如果出现错误则继续向后运行
	if c.engine.TrustedPlatform != "" {
		// 开发人员可以定义自己的可信平台标头或使用预定义常量
		if addr := c.requestHeader(c.engine.TrustedPlatform); addr != "" {
			return addr
		}
	}

	// 旧版“AppEngine”标志
	if c.engine.AppEngine {
		log.Println(`The AppEngine flag is going to be deprecated. Please check issues #2723 and #2739 and use 'TrustedPlatform: gin.PlatformGoogleAppEngine' instead.`)
		if addr := c.requestHeader("X-Appengine-Remote-Addr"); addr != "" {
			return addr
		}
	}

	var (
		trusted  bool
		remoteIP net.IP
	)
	// 如果 gin 正在监听 unix 套接字，请始终信任它。
	localAddr, ok := c.Request.Context().Value(http.LocalAddrContextKey).(net.Addr)
	if ok && strings.HasPrefix(localAddr.Network(), "unix") {
		trusted = true
	}

	// 倒退
	if !trusted {
		// 它还检查remoteIP 是否是受信任的代理。
		//  为了执行此验证，它将查看 IP 是否包含在至少一个 CIDR 块中
		//  由 Engine.SetTrustedProxies() 定义
		remoteIP = net.ParseIP(c.RemoteIP())
		if remoteIP == nil {
			return ""
		}
		trusted = c.engine.isTrustedProxy(remoteIP)
	}

	if trusted && c.engine.ForwardedByClientIP && c.engine.RemoteIPHeaders != nil {
		for _, headerName := range c.engine.RemoteIPHeaders {
			headerValue := strings.Join(c.Request.Header.Values(headerName), ",")
			ip, valid := c.engine.validateHeader(headerValue)
			if valid {
				return ip
			}
		}
	}
	return remoteIP.String()
}

// RemoteIP 解析来自 Request.RemoteAddr 的 IP，规范化并返回 IP（不带端口）。
func (c *Context) RemoteIP() string {
	ip, _, err := net.SplitHostPort(strings.TrimSpace(c.Request.RemoteAddr))
	if err != nil {
		return ""
	}
	return ip
}

// ContentType 返回请求的 Content-Type 标头。
func (c *Context) ContentType() string {
	return filterFlags(c.requestHeader("Content-Type"))
}

// 如果请求标头指示 Websocket，则 IsWebsocket 返回 true
//  握手是由客户端发起的。
func (c *Context) IsWebsocket() bool {
	if strings.Contains(strings.ToLower(c.requestHeader("Connection")), "upgrade") &&
		strings.EqualFold(c.requestHeader("Upgrade"), "websocket") {
		return true
	}
	return false
}

func (c *Context) requestHeader(key string) string {
	return c.Request.Header.Get(key)
}

/************************************/
/******** RESPONSE RENDERING ********/
/************************************/

// bodyAllowedForStatus 是 http.bodyAllowedForStatus 非导出函数的副本。
//  使用 http.StatusContinue 常量以获得更好的代码清晰度。
func bodyAllowedForStatus(status int) bool {
	switch {
	case status >= http.StatusContinue && status < http.StatusOK:
		return false
	case status == http.StatusNoContent:
		return false
	case status == http.StatusNotModified:
		return false
	}
	return true
}

// 状态设置 HTTP 响应代码。
func (c *Context) Status(code int) {
	c.Writer.WriteHeader(code)
}

// Header 是 c.Writer.Header().Set(key, value) 的智能快捷方式。
//  它在响应中写入标头。
//  如果 value == ""，则此方法删除标头 `c.Writer.Header().Del(key)`
func (c *Context) Header(key, value string) {
	if value == "" {
		c.Writer.Header().Del(key)
		return
	}
	c.Writer.Header().Set(key, value)
}

// GetHeader 从请求标头返回值。
func (c *Context) GetHeader(key string) string {
	return c.requestHeader(key)
}

// GetRawData 返回流数据。
func (c *Context) GetRawData() ([]byte, error) {
	if c.Request.Body == nil {
		return nil, errors.New("cannot read nil body")
	}
	return io.ReadAll(c.Request.Body)
}

// 使用 cookie 设置SameSite
func (c *Context) SetSameSite(samesite http.SameSite) {
	c.sameSite = samesite
}

// SetCookie 将 Set-Cookie 标头添加到 ResponseWriter 的标头中。
//  提供的 cookie 必须具有有效的名称。无效的 cookie 可能是
//  悄然落下。
func (c *Context) SetCookie(name, value string, maxAge int, path, domain string, secure, httpOnly bool) {
	if path == "" {
		path = "/"
	}
	http.SetCookie(c.Writer, &http.Cookie{
		Name:     name,
		Value:    url.QueryEscape(value),
		MaxAge:   maxAge,
		Path:     path,
		Domain:   domain,
		SameSite: c.sameSite,
		Secure:   secure,
		HttpOnly: httpOnly,
	})
}

// SetCookieData 将 Set-Cookie 标头添加到 ResponseWriter 的标头中。
//  它接受指向 http.Cookie 结构的指针，以便更灵活地设置 cookie 属性。
//  提供的 cookie 必须具有有效的名称。无效的 cookie 可能会被悄悄删除。
func (c *Context) SetCookieData(cookie *http.Cookie) {
	if cookie.Path == "" {
		cookie.Path = "/"
	}
	if cookie.SameSite == http.SameSiteDefaultMode {
		cookie.SameSite = c.sameSite
	}
	http.SetCookie(c.Writer, cookie)
}

// Cookie 返回请求中提供的指定 cookie 或
//  如果没有找到 ErrNoCookie。并返回未转义的命名 cookie。
//  如果有多个 cookie 与给定名称匹配，则只有一个 cookie 会匹配
//  被退回。
func (c *Context) Cookie(name string) (string, error) {
	cookie, err := c.Request.Cookie(name)
	if err != nil {
		return "", err
	}
	val, _ := url.QueryUnescape(cookie.Value)
	return val, nil
}

// Render 写入响应标头并调用 render.Render 来渲染数据。
func (c *Context) Render(code int, r render.Render) {
	c.Status(code)

	if !bodyAllowedForStatus(code) {
		r.WriteContentType(c.Writer)
		c.Writer.WriteHeaderNow()
		return
	}

	if err := r.Render(c.Writer); err != nil {
		// 将错误推送到 c.Errors
		_ = c.Error(err)
		c.Abort()
	}
}

// HTML 呈现由其文件名指定的 HTTP 模板。
//  它还更新 HTTP 代码并将 Content-Type 设置为“text/html”。
//  请参阅http://golang.org/doc/articles/wiki/
func (c *Context) HTML(code int, name string, obj any) {
	instance := c.engine.HTMLRender.Instance(name, obj)
	c.Render(code, instance)
}

// IndentedJSON 将给定的结构序列化为漂亮的 JSON（缩进 + 结束行）到响应正文中。
//  它还将 Content-Type 设置为“application/json”。
//  警告：我们建议仅将其用于开发目的，因为打印漂亮的 JSON 是
//  消耗更多的CPU和带宽。请改用 Context.JSON()。
func (c *Context) IndentedJSON(code int, obj any) {
	c.Render(code, render.IndentedJSON{Data: obj})
}

// SecureJSON 将给定结构作为安全 JSON 序列化到响应正文中。
//  如果给定的结构是数组值，则默认在响应正文中添加“while(1),”。
//  它还将 Content-Type 设置为“application/json”。
func (c *Context) SecureJSON(code int, obj any) {
	c.Render(code, render.SecureJSON{Prefix: c.engine.secureJSONPrefix, Data: obj})
}

// JSONP 将给定结构序列化为 JSON 到响应正文中。
//  它向响应正文添加填充，以从驻留在与客户端不同域的服务器请求数据。
//  它还将 Content-Type 设置为“application/javascript”。
func (c *Context) JSONP(code int, obj any) {
	callback := c.DefaultQuery("callback", "")
	if callback == "" {
		c.Render(code, render.JSON{Data: obj})
		return
	}
	c.Render(code, render.JsonpJSON{Callback: callback, Data: obj})
}

// JSON 将给定结构序列化为 JSON 到响应正文中。
//  它还将 Content-Type 设置为“application/json”。
func (c *Context) JSON(code int, obj any) {
	c.Render(code, render.JSON{Data: obj})
}

// AsciiJSON 将给定的结构序列化为 JSON 到响应正文中，其中包含 unicode 到 ASCII 字符串。
//  它还将 Content-Type 设置为“application/json”。
func (c *Context) AsciiJSON(code int, obj any) {
	c.Render(code, render.AsciiJSON{Data: obj})
}

// PureJSON 将给定结构作为 JSON 序列化到响应正文中。
//  PureJSON 与 JSON 不同，不会用其 unicode 实体替换特殊的 html 字符。
func (c *Context) PureJSON(code int, obj any) {
	c.Render(code, render.PureJSON{Data: obj})
}

// XML 将给定结构作为 XML 序列化到响应正文中。
//  它还将 Content-Type 设置为“application/xml”。
func (c *Context) XML(code int, obj any) {
	c.Render(code, render.XML{Data: obj})
}

// YAML 将给定结构序列化为 YAML 到响应正文中。
func (c *Context) YAML(code int, obj any) {
	c.Render(code, render.YAML{Data: obj})
}

// TOML 将给定结构作为 TOML 序列化到响应正文中。
func (c *Context) TOML(code int, obj any) {
	c.Render(code, render.TOML{Data: obj})
}

// ProtoBuf 将给定的结构作为 ProtoBuf 序列化到响应主体中。
func (c *Context) ProtoBuf(code int, obj any) {
	c.Render(code, render.ProtoBuf{Data: obj})
}

// BSON 将给定结构序列化为 BSON 到响应正文中。
func (c *Context) BSON(code int, obj any) {
	c.Render(code, render.BSON{Data: obj})
}

// String 将给定字符串写入响应正文。
func (c *Context) String(code int, format string, values ...any) {
	c.Render(code, render.String{Format: format, Data: values})
}

// Redirect 返回到特定位置的 HTTP 重定向。
func (c *Context) Redirect(code int, location string) {
	c.Render(-1, render.Redirect{
		Code:     code,
		Location: location,
		Request:  c.Request,
	})
}

// Data 将一些数据写入主体流并更新 HTTP 代码。
func (c *Context) Data(code int, contentType string, data []byte) {
	c.Render(code, render.Data{
		ContentType: contentType,
		Data:        data,
	})
}

// DataFromReader 将指定的读取器写入正文流并更新 HTTP 代码。
func (c *Context) DataFromReader(code int, contentLength int64, contentType string, reader io.Reader, extraHeaders map[string]string) {
	c.Render(code, render.Reader{
		Headers:       extraHeaders,
		ContentType:   contentType,
		ContentLength: contentLength,
		Reader:        reader,
	})
}

// File 以高效的方式将指定文件写入主体流。
func (c *Context) File(filepath string) {
	http.ServeFile(c.Writer, c.Request, filepath)
}

// FileFromFS 以高效的方式将 http.FileSystem 中的指定文件写入正文流。
func (c *Context) FileFromFS(filepath string, fs http.FileSystem) {
	defer func(old string) {
		c.Request.URL.Path = old
	}(c.Request.URL.Path)

	c.Request.URL.Path = filepath

	http.FileServer(fs).ServeHTTP(c.Writer, c.Request)
}

var quoteEscaper = strings.NewReplacer("\\", "\\\\", `"`, "\\\"")

func escapeQuotes(s string) string {
	return quoteEscaper.Replace(s)
}

// FileAttachment 以高效的方式将指定文件写入主体流
//  在客户端，通常会使用给定的文件名下载文件
func (c *Context) FileAttachment(filepath, filename string) {
	if isASCII(filename) {
		c.Writer.Header().Set("Content-Disposition", `attachment; filename="`+escapeQuotes(filename)+`"`)
	} else {
		c.Writer.Header().Set("Content-Disposition", `attachment; filename*=UTF-8''`+url.QueryEscape(filename))
	}
	http.ServeFile(c.Writer, c.Request, filepath)
}

// SSEvent 将服务器发送的事件写入正文流。
func (c *Context) SSEvent(name string, message any) {
	c.Render(-1, sse.Event{
		Event: name,
		Data:  message,
	})
}

// Stream 发送流式响应并返回布尔值
//  指示“客户端是否在流中断开连接”
func (c *Context) Stream(step func(w io.Writer) bool) bool {
	w := c.Writer
	clientGone := w.CloseNotify()
	for {
		select {
		case <-clientGone:
			return true
		default:
			keepOpen := step(w)
			w.Flush()
			if !keepOpen {
				return false
			}
		}
	}
}

/************************************/
/******** CONTENT NEGOTIATION *******/
/************************************/

// Negotiate 包含所有谈判数据。
type Negotiate struct {
	Offered      []string
	HTMLName     string
	HTMLData     any
	JSONData     any
	XMLData      any
	YAMLData     any
	Data         any
	TOMLData     any
	PROTOBUFData any
	BSONData     any
}

// Negotiate根据可接受的Accept格式调用不同的Render。
func (c *Context) Negotiate(code int, config Negotiate) {
	switch c.NegotiateFormat(config.Offered...) {
	case binding.MIMEJSON:
		data := chooseData(config.JSONData, config.Data)
		c.JSON(code, data)

	case binding.MIMEHTML:
		data := chooseData(config.HTMLData, config.Data)
		c.HTML(code, config.HTMLName, data)

	case binding.MIMEXML:
		data := chooseData(config.XMLData, config.Data)
		c.XML(code, data)

	case binding.MIMEYAML, binding.MIMEYAML2:
		data := chooseData(config.YAMLData, config.Data)
		c.YAML(code, data)

	case binding.MIMETOML:
		data := chooseData(config.TOMLData, config.Data)
		c.TOML(code, data)

	case binding.MIMEPROTOBUF:
		data := chooseData(config.PROTOBUFData, config.Data)
		c.ProtoBuf(code, data)

	case binding.MIMEBSON:
		data := chooseData(config.BSONData, config.Data)
		c.BSON(code, data)

	default:
		c.AbortWithError(http.StatusNotAcceptable, errors.New("the accepted formats are not offered by the server")) //nolint: errcheck
	}
}

// NegotiateFormat 返回可接受的 Accept 格式。
func (c *Context) NegotiateFormat(offered ...string) string {
	assert1(len(offered) > 0, "you must provide at least one offer")

	if c.Accepted == nil {
		c.Accepted = parseAccept(c.requestHeader("Accept"))
	}
	if len(c.Accepted) == 0 {
		return offered[0]
	}
	for _, accepted := range c.Accepted {
		for _, offer := range offered {
			// 根据 RFC 2616 和 RFC 2396，标头中不允许使用非 ASCII 字符，
			//  因此我们可以只迭代字符串而不将其转换为 []rune
			i := 0
			for ; i < len(accepted) && i < len(offer); i++ {
				if accepted[i] == '*' || offer[i] == '*' {
					return offer
				}
				if accepted[i] != offer[i] {
					break
				}
			}
			if i == len(accepted) {
				return offer
			}
		}
	}
	return ""
}

// SetAccepted 设置 Accept 标头数据。
func (c *Context) SetAccepted(formats ...string) {
	c.Accepted = formats
}

/************************************/
/***** GOLANG.ORG/X/NET/CONTEXT *****/
/************************************/

// hasRequestContext 返回 c.Request 是否具有 Context 和后备。
func (c *Context) hasRequestContext() bool {
	hasFallback := c.engine != nil && c.engine.ContextWithFallback
	hasRequestContext := c.Request != nil && c.Request.Context() != nil
	return hasFallback && hasRequestContext
}

// 当c.Request没有Context时，Deadline返回没有截止日期（ok==false）。
func (c *Context) Deadline() (deadline time.Time, ok bool) {
	if !c.hasRequestContext() {
		return
	}
	return c.Request.Context().Deadline()
}

// 当 c.Request 没有 Context 时，Done 返回 nil（chan 将永远等待）。
func (c *Context) Done() <-chan struct{} {
	if !c.hasRequestContext() {
		return nil
	}
	return c.Request.Context().Done()
}

// 当 c.Request 没有 Context 时，Err 返回 nil。
func (c *Context) Err() error {
	if !c.hasRequestContext() {
		return nil
	}
	return c.Request.Context().Err()
}

// Value 返回与 key 的上下文关联的值，或者 nil
//  如果没有值与键关联。连续调用 Value with
//  相同的键返回相同的结果。
func (c *Context) Value(key any) any {
	if key == ContextRequestKey {
		return c.Request
	}
	if key == ContextKey {
		return c
	}
	if keyAsString, ok := key.(string); ok {
		if val, exists := c.Get(keyAsString); exists {
			return val
		}
	}
	if !c.hasRequestContext() {
		return nil
	}
	return c.Request.Context().Value(key)
}
