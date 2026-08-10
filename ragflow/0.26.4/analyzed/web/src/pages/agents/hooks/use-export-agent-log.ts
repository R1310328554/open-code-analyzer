// use-export-agent-log.ts — Agent 运行日志导出：按筛选条件拉取日志并生成 UTF-8 CSV 下载。

import message from '@/components/ui/message';
import { useExportAgentLog } from '@/hooks/use-agent-request';
import { IAgentLogResponse } from '@/interfaces/database/agent';
import { downloadFileFromBlob } from '@/utils/file-util';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';

/** 导出日志时的查询参数，与列表页筛选字段一致。 */
interface ISearchParams {
  keywords?: string;
  from_date?: Date;
  to_date?: Date;
  orderby?: string;
  desc?: boolean;
  page?: number;
  page_size?: number;
}

/** 封装 CSV 转换与下载逻辑，返回 handleExport 与 loading 状态。 */
export const useExportAgentLogToCSV = () => {
  const { t } = useTranslation();
  const { id: canvasId } = useParams();
  const { exportLogs, loading } = useExportAgentLog();

  /** 将日志数组转为带表头的 CSV 字符串，单元格内双引号按 RFC 4180 转义。 */
  const convertToCSV = (data: IAgentLogResponse[]) => {
    const headers = [
      t('flow.id'),
      t('flow.userId'),
      t('flow.logTitle'),
      t('flow.state'),
      t('flow.number'),
      t('flow.latestDate'),
      t('flow.createDate'),
      t('flow.version.version'),
    ];

    const rows = data.map((item) => [
      item.id,
      item.user_id,
      item.message?.length ? item.message[0]?.content : '',
      item.errors ? t('flow.failed') : t('flow.success'),
      item.round,
      item.update_date,
      item.create_date,
      item.version_title,
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map((row) =>
        row.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(','),
      ),
    ].join('\n');

    return csvContent;
  };

  /** 调用 exportLogs 拉取数据，空结果提示后中止，否则触发浏览器下载。 */
  const handleExport = async (searchParams: ISearchParams) => {
    const allData = await exportLogs({
      keywords: searchParams.keywords,
      from_date: searchParams.from_date,
      to_date: searchParams.to_date,
      orderby: searchParams.orderby,
      desc: searchParams.desc,
      page: searchParams.page,
      page_size: searchParams.page_size,
    });

    if (allData.length === 0) {
      console.log('No data to export', allData);
      message.warning(t('flow.noDataToExport'));
      return;
    }

    const csvContent = convertToCSV(allData);
    // 前置 BOM 以便 Excel 正确识别 UTF-8 中文
    // Add BOM for Excel to correctly display UTF-8
    const BOM = '\uFEFF';
    const blob = new Blob([BOM + csvContent], {
      type: 'text/csv;charset=utf-8;',
    });
    downloadFileFromBlob(
      blob,
      `agent-logs-${canvasId}-${new Date().toISOString().split('T')[0]}.csv`,
    );
  };

  return { handleExport, loading };
};
