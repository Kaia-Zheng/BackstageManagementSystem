import request from '../utils/request';
import { BASE_URI } from './base';

export interface ClubInfo {
  clubId: number;
  clubName: string;
  category: string;
  memberCount: number;
  role: string;
  joinedTime: string;
  description?: string;
  state?: string;
}

export interface ActivityInfo {
  registrationId: number;
  activityId: number;
  title: string;
  clubName: string;
  location?: string;
  activityTime: string;
  activityStatus: string;
  registerStatus: string;
  registerTime: string;
  maxParticipants?: number;
  currentParticipants?: number;
}

export interface ProfileData {
  userId: number;
  username: string;
  realName: string;
  avatar: string;
  phone: string;
  email: string;
  gender: string;
  state: string;
  roles: string[];
  permissions: string[];
  clubCount: number;
  activityCount: number;
  pendingAuditCount: number;
  clubs: ClubInfo[];
  activities: ActivityInfo[];
  recentActivities: ActivityInfo[];
  managedClubs: ClubInfo[];
}

export function getProfile() {
  return request({ url: `${BASE_URI}/user/profile`, method: 'get' });
}

export function updateProfile(data: { avatar?: string; phone?: string; email?: string }) {
  return request({ url: `${BASE_URI}/user/profile`, method: 'put', data });
}

export function getMyClubs() {
  return request({ url: `${BASE_URI}/user/my-clubs`, method: 'get' });
}

export function getMyActivities() {
  return request({ url: `${BASE_URI}/user/my-activities`, method: 'get' });
}
