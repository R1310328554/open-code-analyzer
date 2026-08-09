// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package ginS

import (
	"html/template"
	"net/http"
	"sync"

	"github.com/gin-gonic/gin"
)

var engine = sync.OnceValue(func() *gin.Engine {
	return gin.Default()
})

// LoadHTMLGlob 是 Engine.LoadHTMLGlob 的包装器。
func LoadHTMLGlob(pattern string) {
	engine().LoadHTMLGlob(pattern)
}

// LoadHTMLFiles 是 Engine.LoadHTMLFiles 的包装器。
func LoadHTMLFiles(files ...string) {
	engine().LoadHTMLFiles(files...)
}

// LoadHTMLFS 是 Engine.LoadHTMLFS 的包装器。
func LoadHTMLFS(fs http.FileSystem, patterns ...string) {
	engine().LoadHTMLFS(fs, patterns...)
}

// SetHTMLTemplate 是 Engine.SetHTMLTemplate 的包装器。
func SetHTMLTemplate(templ *template.Template) {
	engine().SetHTMLTemplate(templ)
}

// NoRoute 添加了 NoRoute 的处理程序。默认情况下它返回 404 代码。
func NoRoute(handlers ...gin.HandlerFunc) {
	engine().NoRoute(handlers...)
}

// NoMethod 是 Engine.NoMethod 的包装器。
func NoMethod(handlers ...gin.HandlerFunc) {
	engine().NoMethod(handlers...)
}

// Group 创建一个新的路由器组。您应该添加具有公共中间件或相同路径前缀的所有路由。
//  例如，可以对使用公共中间件进行授权的所有路由进行分组。
func Group(relativePath string, handlers ...gin.HandlerFunc) *gin.RouterGroup {
	return engine().Group(relativePath, handlers...)
}

// Handle 是 Engine.Handle 的包装器。
func Handle(httpMethod, relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().Handle(httpMethod, relativePath, handlers...)
}

// POST 是 router.Handle("POST",path,handle) 的快捷方式
func POST(relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().POST(relativePath, handlers...)
}

// GET 是 router.Handle("GET",path,handle) 的快捷方式
func GET(relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().GET(relativePath, handlers...)
}

// DELETE 是 router.Handle("DELETE", path, handle) 的快捷方式
func DELETE(relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().DELETE(relativePath, handlers...)
}

// PATCH 是 router.Handle("PATCH",path,handle) 的快捷方式
func PATCH(relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().PATCH(relativePath, handlers...)
}

// PUT 是 router.Handle("PUT",path,handle) 的快捷方式
func PUT(relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().PUT(relativePath, handlers...)
}

// OPTIONS 是 router.Handle("OPTIONS", path, handle) 的快捷方式
func OPTIONS(relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().OPTIONS(relativePath, handlers...)
}

// HEAD 是 router.Handle("HEAD",path,handle) 的快捷方式
func HEAD(relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().HEAD(relativePath, handlers...)
}

// Any 是 Engine.Any 的包装器。
func Any(relativePath string, handlers ...gin.HandlerFunc) gin.IRoutes {
	return engine().Any(relativePath, handlers...)
}

// StaticFile 是 Engine.StaticFile 的包装器。
func StaticFile(relativePath, filepath string) gin.IRoutes {
	return engine().StaticFile(relativePath, filepath)
}

// 静态服务来自给定文件系统根的文件。
//  内部使用 http.FileServer，因此使用 http.NotFound 代替
//  路由器的 NotFound 处理程序。
//  要使用操作系统的文件系统实现，
//  使用：
//
// 	router.Static("/static", "/var/www")
func Static(relativePath, root string) gin.IRoutes {
	return engine().Static(relativePath, root)
}

// StaticFS 是 Engine.StaticFS 的包装器。
func StaticFS(relativePath string, fs http.FileSystem) gin.IRoutes {
	return engine().StaticFS(relativePath, fs)
}

// 使用将全局中间件附加到路由器。即通过 Use() 附加的中间件将是
//  包含在每个请求的处理程序链中。甚至404、405、静态文件……
//  例如，这是记录器或错误管理中间件的正确位置。
func Use(middlewares ...gin.HandlerFunc) gin.IRoutes {
	return engine().Use(middlewares...)
}

// 路由返回注册路由的切片。
func Routes() gin.RoutesInfo {
	return engine().Routes()
}

// Run 附加到 http.Server 并开始侦听和服务 HTTP 请求。
//  这是 http.ListenAndServe(addr, router) 的快捷方式
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func Run(addr ...string) (err error) {
	return engine().Run(addr...)
}

// RunTLS 附加到 http.Server 并开始侦听和服务 HTTPS 请求。
//  它是 http.ListenAndServeTLS(addr, certFile, keyFile, router) 的快捷方式
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func RunTLS(addr, certFile, keyFile string) (err error) {
	return engine().RunTLS(addr, certFile, keyFile)
}

// RunUnix 附加到 http.Server 并开始侦听和服务 HTTP 请求
//  通过指定的unix套接字（即文件）
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func RunUnix(file string) (err error) {
	return engine().RunUnix(file)
}

// RunFd 将路由器连接到 http.Server 并开始侦听和服务 HTTP 请求
//  通过指定的文件描述符。
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func RunFd(fd int) (err error) {
	return engine().RunFd(fd)
}
