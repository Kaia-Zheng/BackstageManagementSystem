import request from '../utils/request';
import { BASE_URI } from './base';

export type ActivityStatus = 'PENDING' | 'PUBLISHED' | 'ONGOING' | 'ENDED' | 'CANCELLED';

export const ActivityStatusList: { value: ActivityStatus; label: string; color: string }[] = [
  { value: 'PENDING', label: '待审核', color: '#faad14' },
  { value: 'PUBLISHED', label: '已发布', color: '#1890ff' },
  { value: 'ONGOING', label: '进行中', color: '#52c41a' },
  { value: 'ENDED', label: '已结束', color: '#8c8c8c' },
  { value: 'CANCELLED', label: '已取消', color: '#f5222d' }
];

export const ActivityStatusLabel = (s: ActivityStatus | string): string => {
  const found = ActivityStatusList.find(item => item.value === s);
  return found ? found.label : String(s);
};

export const ActivityStatusColor = (s: ActivityStatus | string): string => {
  const found = ActivityStatusList.find(item => item.value === s);
  return found ? found.color : '#8c8c8c';
};

export interface Activity {
  id: number;
  title: string;
  description: string;
  location: string;
  activityTime: string;
  status: ActivityStatus;
  coverImage: string | null;
  maxParticipants: number;
  currentParticipants: number;
  club: { id: number; name: string };
}

export function getActivityList(data: {
  page: number;
  size: number;
  title?: string;
  clubId?: number;
  status?: ActivityStatus;
}) {
  return request({
    url: `${BASE_URI}/activities`,
    method: 'get',
    params: data
  });
}

export function getActivity(activityId: number) {
  return request({
    url: `${BASE_URI}/activities/${activityId}`,
    method: 'get'
  });
}

export function createActivity(data: {
  title: string;
  description: string;
  location: string;
  activityTime: string;
  coverImage?: string;
  maxParticipants: number;
  clubId: number;
}) {
  return request({
    url: `${BASE_URI}/activities`,
    method: 'post',
    data
  });
}

export function updateActivity(activityId: number, data: {
  title: string;
  description: string;
  location: string;
  activityTime: string;
  coverImage?: string;
  maxParticipants: number;
  clubId: number;
}) {
  return request({
    url: `${BASE_URI}/activities/${activityId}`,
    method: 'put',
    data
  });
}

export function deleteActivity(activityId: number) {
  return request({
    url: `${BASE_URI}/activities/${activityId}`,
    method: 'delete'
  });
}

export function approveActivity(activityId: number) {
  return request({
    url: `${BASE_URI}/activities/${activityId}/approve`,
    method: 'post'
  });
}

export function rejectActivity(activityId: number, reason?: string) {
  return request({
    url: `${BASE_URI}/activities/${activityId}/reject`,
    method: 'post',
    data: { reason }
  });
}

export function startActivity(activityId: number) {
  return request({
    url: `${BASE_URI}/activities/${activityId}/start`,
    method: 'post'
  });
}

export function endActivity(activityId: number) {
  return request({
    url: `${BASE_URI}/activities/${activityId}/end`,
    method: 'post'
  });
}

export function cancelActivity(activityId: number, reason?: string) {
  return request({
    url: `${BASE_URI}/activities/${activityId}/cancel`,
    method: 'post',
    data: { reason }
  });
}

// 报名
export function registerActivity(activityId: number) {
  return request({
    url: `${BASE_URI}/activities/${activityId}/registrations/me`,
    method: 'post'
  });
}

export function cancelRegistration(activityId: number) {
  return request({
    url: `${BASE_URI}/activities/${activityId}/registrations/me`,
    method: 'delete'
  });
}

export function getActivityRegistrations(activityId: number, data: { page: number; size: number }) {
  return request({
    url: `${BASE_URI}/activities/${activityId}/registrations`,
    method: 'get',
    params: data
  });
}
