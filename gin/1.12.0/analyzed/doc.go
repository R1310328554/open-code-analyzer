/*
Package gin 实现名为 gin 的 HTTP Web 框架。

更多信息请参见 https://gin-gonic.com/。

示例：

	package main

	import "github.com/gin-gonic/gin"

	func main() {
		r := gin.Default()
		r.GET("/ping", func(c *gin.Context) {
			c.JSON(200, gin.H{
				"message": "pong",
			})
		})
		r.Run() // 在 0.0.0.0:8080 监听并服务
	}
*/
package gin // import "github.com/gin-gonic/gin"
