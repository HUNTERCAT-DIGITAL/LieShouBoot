/**
 * 工作台数据层（feature: dashboard · api）。
 * 只依赖 API 契约（/actuator/health、/api/users/count），不绑实现。
 */
async function request<T>(path: string): Promise<T> {
  const res = await fetch(path, { headers: { 'X-Tenant-Id': '1' } });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json() as Promise<T>;
}

export const dashboardApi = {
  health: () => request<{ status: string }>('/actuator/health').then((r) => r.status),
  userCount: () => request<number>('/api/users/count').catch(() => null),
};
