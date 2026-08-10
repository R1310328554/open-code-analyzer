// 故意写错的脚本映射器：函数名拼写错误，用于测试无效脚本的加载/执行失败路径
func_tion foo(){ return 'fail';} foo();
