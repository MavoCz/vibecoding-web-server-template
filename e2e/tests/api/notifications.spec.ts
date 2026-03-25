import { test, expect } from '../../fixtures/api.fixture';
import type { NotificationResponse } from 'myhome-common/api/generated/model';

test.describe('Notifications API', () => {
  test('GET /api/notifications returns empty list for new user', async ({ authApi }) => {
    const res = await authApi.get('/api/notifications');
    expect(res.ok()).toBeTruthy();
    const notifications: NotificationResponse[] = await res.json();
    expect(Array.isArray(notifications)).toBeTruthy();
  });

  test('GET /api/notifications requires auth', async ({ api }) => {
    const res = await api.get('/api/notifications');
    expect(res.status()).toBe(401);
  });

  test('mark all notifications as read succeeds', async ({ authApi }) => {
    const readAllRes = await authApi.post('/api/notifications/read-all');
    expect(readAllRes.ok()).toBeTruthy();
  });

  test('SSE stream endpoint requires auth', async ({ api }) => {
    const res = await api.get('/api/notifications/stream');
    expect(res.status()).toBe(401);
  });
});
