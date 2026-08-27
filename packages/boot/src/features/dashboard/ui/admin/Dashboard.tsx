/**
 * LieShouBoot 专属工作台页面（feature: dashboard · admin/desktop 版）。
 *
 * FSD 分层：ui/（页面） + model/（逻辑）+ api/（数据）。
 * 示例：展示后端健康 + 登录用户信息（执行逻辑在 model/useDashboard.ts）。
 */
import { Card, Col, Row, Statistic, Typography } from 'antd';
import { useDashboard } from '../../model/useDashboard';

export default function Dashboard() {
  const { backendStatus, userCount, loading } = useDashboard();

  return (
    <div style={{ padding: 24 }}>
      <Typography.Title level={3}>LieShouBoot 专属工作台</Typography.Title>
      <Row gutter={16}>
        <Col span={8}>
          <Card loading={loading}>
            <Statistic title="后端状态" value={backendStatus ?? '...'} />
          </Card>
        </Col>
        <Col span={8}>
          <Card loading={loading}>
            <Statistic title="用户数" value={userCount ?? '-'} />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
