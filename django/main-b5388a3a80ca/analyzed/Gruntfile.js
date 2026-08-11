'use strict';

// Grunt 构建配置：为 Django 管理后台 JavaScript 运行 QUnit 单元测试
// 全局代码覆盖率阈值（百分比）
const globalThreshold = 50; // Global code coverage threshold (as a percentage)

module.exports = function(grunt) {
// 初始化 Grunt 任务：QUnit 测试入口
    grunt.initConfig({
        qunit: {
            all: ['js_tests/tests.html']
        }
    });

// 加载 grunt-contrib-qunit 插件
    grunt.loadNpmTasks('grunt-contrib-qunit');
// 注册 test 任务：运行 QUnit
    grunt.registerTask('test', ['qunit']);
// 默认任务指向 test
    grunt.registerTask('default', ['test']);
};
