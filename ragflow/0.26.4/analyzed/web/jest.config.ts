// Jest 单元测试配置：jsdom 环境、esbuild 转换、路径别名与覆盖率阈值。
import type { Config } from 'jest';

const config: Config = {  // 主配置对象
  testEnvironment: 'jsdom',
  transform: {
    '^.+\\.(ts|tsx|js|jsx)$': [
      'esbuild-jest',
      {
        sourcemap: true,
        loaders: {
          '.ts': 'tsx',
        },
      },
    ],
  },
  moduleNameMapper: {  // @/、human-id 与静态资源 mock 映射
    '^@/(.*)$': '<rootDir>/src/$1',
    '^human-id$': '<rootDir>/__mocks__/human-id.js',
    '\\.(css|less|scss|sass)$': '<rootDir>/__mocks__/styleMock.js',
    '\\.(jpg|jpeg|png|gif|svg|webp)$': '<rootDir>/__mocks__/fileMock.js',
  },
  setupFilesAfterEnv: ['<rootDir>/jest-setup.ts'],  // 每个测试文件前加载 jest-dom
  collectCoverageFrom: [  // 覆盖率统计范围，排除 .umi 与类型声明
    'src/**/*.{ts,tsx,js,jsx}',
    '!src/.umi/**',
    '!src/.umi-test/**',
    '!src/.umi-production/**',
    '!**/*.d.ts',
    '!coverage/**',
    '!dist/**',
    '!config/**',
    '!mock/**',
  ],
  coverageThreshold: {
    global: {
      lines: 1,
    },
  },
  testPathIgnorePatterns: ['/node_modules/', '/dist/'],
};

export default config;
