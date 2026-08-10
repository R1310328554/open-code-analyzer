"""
RAGFlow Elasticsearch 8+ 至 OceanBase 迁移 CLI：模式转换、向量映射、批量导入与断点续传。
RAGFlow ES to OceanBase Migration Tool

A CLI tool for migrating RAGFlow data from Elasticsearch 8+ to OceanBase,
supporting schema conversion, vector data mapping, batch import, and resume capability.

This tool is specifically designed for RAGFlow's data structure.
"""

__version__ = "0.1.0"

from .migrator import ESToOceanBaseMigrator
from .es_client import ESClient
from .ob_client import OBClient
from .schema import RAGFlowSchemaConverter, RAGFlowDataConverter, RAGFLOW_COLUMNS
from .verify import MigrationVerifier, VerificationResult
from .progress import ProgressManager, MigrationProgress

# 向后兼容别名
SchemaConverter = RAGFlowSchemaConverter
DataConverter = RAGFlowDataConverter

__all__ = [
    # 主类：Migrator、ES/OB 客户端
    "ESToOceanBaseMigrator",
    "ESClient",
    "OBClient",
    # 模式与数据转换
    "RAGFlowSchemaConverter",
    "RAGFlowDataConverter",
    "RAGFLOW_COLUMNS",
    # 迁移校验
    "MigrationVerifier",
    "VerificationResult",
    # 进度持久化与恢复
    "ProgressManager",
    "MigrationProgress",
    # 旧名别名
    "SchemaConverter",
    "DataConverter",
]
