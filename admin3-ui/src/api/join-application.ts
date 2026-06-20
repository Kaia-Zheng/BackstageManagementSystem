import request from '../utils/request';
import { BASE_URI } from './base';

export interface JoinApplication {
  id: number;
  clubId: number;
  clubName: string;
  userId?: number;
  username?: string;
  realName?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  reason?: string;
  createTime: string;
  updateTime?: string;
}

export function applyToJoin(clubId: number) {
  return request({ url: `${BASE_URI}/clubs/${clubId}/join`, method: 'post' });
}

export function getJoinStatus(clubId: number) {
  return request({ url: `${BASE_URI}/clubs/${clubId}/join-status`, method: 'get' });
}

export function getMyApplications() {
  return request({ url: `${BASE_URI}/clubs/join-applications/my`, method: 'get' });
}

export function getClubApplications(clubId: number) {
  return request({ url: `${BASE_URI}/clubs/${clubId}/applications`, method: 'get' });
}

export function approveApplication(applicationId: number) {
  return request({ url: `${BASE_URI}/clubs/applications/${applicationId}/approve`, method: 'post' });
}

export function rejectApplication(applicationId: number, reason?: string) {
  return request({ url: `${BASE_URI}/clubs/applications/${applicationId}/reject`, method: 'post', data: { reason } });
}
