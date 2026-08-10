function humanId() {
  // 固定 mock ID，供 human-id 包在测试中替换
  return 'mock-human-id';
}

// CommonJS 与 default/named 导出兼容
module.exports = humanId;
module.exports.default = humanId;
module.exports.humanId = humanId;
