/**
 * LieShouBoot 专属产品介绍门户页（首页）。
 *
 * 自包含实现（antd + 内联样式），不依赖 admin-web 内部组件；
 * 由 boot edition 的 portal 槽位经 edition.portal 懒加载渲染。
 */
import { ArrowRightOutlined, CloudServerOutlined, RocketOutlined, SafetyOutlined } from '@ant-design/icons';
import { Button, Card, Col, Row, Space, Tag, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';

const { Title, Paragraph, Text } = Typography;

const BRAND = 'LieShouBoot';
const PRIMARY = '#1677ff';

const FEATURES = [
  {
    icon: <CloudServerOutlined />,
    title: '单体架构 · 一条命令',
    desc: '认证 / 用户 / 管理 / 审批一体内置为单个 Spring Boot 应用，docker compose up 即全栈跑通，无 Nacos / 网关 / 多服务依赖。',
  },
  {
    icon: <RocketOutlined />,
    title: '开箱即用',
    desc: '前端四端（Web / 桌面 / 移动 / 小程序）与共享包完全复用，默认管理员账号开箱即登。',
  },
  {
    icon: <SafetyOutlined />,
    title: '安全合规',
    desc: 'JWT 双 token、RBAC 权限、Flyway 迁移、审计日志内置；敏感配置走 .env。',
  },
];

export default function BootPortal() {
  const navigate = useNavigate();

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fa' }}>
      {/* 吸顶导航 */}
      <header
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 10,
          background: 'rgba(255,255,255,0.95)',
          boxShadow: '0 1px 8px rgba(0,0,0,0.06)',
          padding: '0 48px',
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <Space>
          <img src="/logo.png" alt={BRAND} style={{ height: 32 }} />
          <Text strong style={{ fontSize: 18 }}>{BRAND}</Text>
          <Tag color="blue" style={{ marginLeft: 4 }}>单体版</Tag>
        </Space>
        <Space>
          <Button type="primary" onClick={() => navigate('/login')}>登录</Button>
        </Space>
      </header>

      {/* Hero */}
      <section
        style={{
          textAlign: 'center',
          padding: '96px 24px 72px',
          background: `linear-gradient(135deg, ${PRIMARY} 0%, #003eb3 100%)`,
          color: '#fff',
        }}
      >
        <Title style={{ color: '#fff', fontSize: 44, marginBottom: 16 }}>
          LieShouBoot · 开源的轻量单体版
        </Title>
        <Paragraph style={{ color: 'rgba(255,255,255,0.9)', fontSize: 18, maxWidth: 720, margin: '0 auto 32px' }}>
          把猎手云微服务版重组为单个 Spring Boot 应用：认证 / 用户 / 管理 / 审批一体内置，
          前端四端复用，一条 docker compose 命令即可自部署体验。
        </Paragraph>
        <Space size="middle">
          <Button
            size="large"
            type="primary"
            style={{ background: '#fff', color: PRIMARY, borderColor: '#fff' }}
            onClick={() => navigate('/register')}
          >
            免费体验 <ArrowRightOutlined />
          </Button>
          <Button size="large" ghost onClick={() => navigate('/login')}>
            登录现有账号
          </Button>
        </Space>
      </section>

      {/* 特性 */}
      <section style={{ padding: '64px 48px' }}>
        <Title level={2} style={{ textAlign: 'center', marginBottom: 8 }}>为什么选 LieShouBoot</Title>
        <Paragraph type="secondary" style={{ textAlign: 'center', marginBottom: 40 }}>
          低门槛、开箱即用，与微服务版同源可平滑演进
        </Paragraph>
        <Row gutter={[24, 24]} justify="center">
          {FEATURES.map((f) => (
            <Col key={f.title} xs={24} md={8}>
              <Card style={{ height: '100%', borderTop: `3px solid ${PRIMARY}` }}>
                <div style={{ fontSize: 32, color: PRIMARY, marginBottom: 12 }}>{f.icon}</div>
                <Title level={4}>{f.title}</Title>
                <Paragraph type="secondary">{f.desc}</Paragraph>
              </Card>
            </Col>
          ))}
        </Row>
      </section>

      {/* CTA */}
      <section style={{ textAlign: 'center', padding: '48px 24px 64px' }}>
        <Card style={{ maxWidth: 720, margin: '0 auto', background: '#fff' }}>
          <Title level={3}>开始使用</Title>
          <Paragraph type="secondary">开源单体版，可自部署 / 二次开发；需要多租户或行业能力时无缝升级商业版。</Paragraph>
          <Button type="primary" size="large" onClick={() => navigate('/register')}>
            免费体验 LieShouBoot
          </Button>
        </Card>
      </section>

      <footer style={{ textAlign: 'center', padding: '24px', color: '#999' }}>
        LieShouBoot · Apache-2.0 · 猎手云开源产品线
      </footer>
    </div>
  );
}
