import request from '../utils/request';
import { BASE_URI } from './base';

export interface CategoryItem {
  name: string;
  count: number;
}

export interface ActivityRankItem {
  id: number;
  title: string;
  registrationCount: number;
}

export interface RecentRegistrationItem {
  id: number;
  username: string;
  activityTitle: string;
  status: string;
  registerTime: string;
}

export interface DashboardData {
  clubCount: number;
  activityCountThisMonth: number;
  registrationTotalCount: number;
  pendingActivityCount: number;
  monthLabels: string[];
  monthActivityCounts: number[];
  categoryDistribution: CategoryItem[];
  activityRank: ActivityRankItem[];
  recentRegistrations: RecentRegistrationItem[];
}

export function getDashboard() {
  return request({
    url: `${BASE_URI}/statistics/dashboard`,
    method: 'get'
  });
}
