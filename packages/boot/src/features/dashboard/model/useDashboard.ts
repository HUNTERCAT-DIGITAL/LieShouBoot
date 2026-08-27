/**
 * 工作台执行逻辑（feature: dashboard · model 层）。
 * 依赖 api/ 数据层；逻辑单点，四端共用。
 */
import { useEffect, useState } from 'react';
import { dashboardApi } from '../api/dashboardApi';

export function useDashboard() {
  const [backendStatus, setBackendStatus] = useState<string | null>(null);
  const [userCount, setUserCount] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let alive = true;
    Promise.all([dashboardApi.health(), dashboardApi.userCount()])
      .then(([health, count]) => {
        if (!alive) return;
        setBackendStatus(health);
        setUserCount(count);
      })
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, []);

  return { backendStatus, userCount, loading };
}
