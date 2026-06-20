import request from '../utils/request';
import { BASE_URI } from './base';

export interface Notification {
  id: number;
  title: string;
  content: string;
  read: boolean;
  createdTime: string;
}

export function getMyNotifications(data: { page: number; size: number }) {
  return request({
    url: `${BASE_URI}/notifications`,
    method: 'get',
    params: data
  });
}

export function getUnreadCount() {
  return request({
    url: `${BASE_URI}/notifications/unread-count`,
    method: 'get'
  });
}

export function markAllAsRead() {
  return request({
    url: `${BASE_URI}/notifications/mark-all-read`,
    method: 'post'
  });
}
