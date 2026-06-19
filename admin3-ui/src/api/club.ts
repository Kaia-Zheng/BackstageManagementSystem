import request from '../utils/request';
import { BASE_URI } from './base';

export type ClubCategory = 'ACADEMIC' | 'CULTURAL' | 'SPORTS' | 'VOLUNTEER' | 'OTHER';

export const ClubCategoryList: { value: ClubCategory; label: string }[] = [
  { value: 'ACADEMIC', label: '学术科技' },
  { value: 'CULTURAL', label: '文化体育' },
  { value: 'SPORTS', label: '运动竞技' },
  { value: 'VOLUNTEER', label: '公益志愿' },
  { value: 'OTHER', label: '其他' }
];

export const ClubCategoryLabel = (c: ClubCategory): string => {
  const found = ClubCategoryList.find(item => item.value === c);
  return found ? found.label : c;
};

export type ClubState = 'ACTIVE' | 'DISBANDED';

export const ClubStateList: { value: ClubState; label: string; type: string }[] = [
  { value: 'ACTIVE', label: '正常', type: 'success' },
  { value: 'DISBANDED', label: '已解散', type: 'danger' }
];

export const ClubStateLabel = (s: ClubState): string => {
  const found = ClubStateList.find(item => item.value === s);
  return found ? found.label : s;
};

export interface Club {
  id: number;
  name: string;
  description: string;
  category: ClubCategory;
  foundedDate: string;
  state: ClubState;
  avatar: string | null;
  memberCount: number;
  owner: { id: number; username: string } | null;
}

export function getClubList(data: { page: number; size: number; name?: string; category?: ClubCategory; state?: ClubState }) {
  return request({
    url: `${BASE_URI}/clubs`,
    method: 'get',
    params: data
  });
}

export function getClub(clubId: number) {
  return request({
    url: `${BASE_URI}/clubs/${clubId}`,
    method: 'get'
  });
}

export function createClub(data: {
  name: string;
  description: string;
  category: ClubCategory;
  foundedDate: string;
  avatar?: string;
  ownerId: number;
}) {
  return request({
    url: `${BASE_URI}/clubs`,
    method: 'post',
    data
  });
}

export function updateClub(clubId: number, data: {
  name: string;
  description: string;
  category: ClubCategory;
  foundedDate: string;
  state: ClubState;
  avatar?: string;
  ownerId: number;
}) {
  return request({
    url: `${BASE_URI}/clubs/${clubId}`,
    method: 'put',
    data
  });
}

export function deleteClub(clubId: number) {
  return request({
    url: `${BASE_URI}/clubs/${clubId}`,
    method: 'delete'
  });
}

// 成员管理
export type MemberRole = 'MEMBER' | 'LEADER';

export const MemberRoleMap: Record<MemberRole, string> = {
  MEMBER: '成员',
  LEADER: '管理员'
};

export interface ClubMember {
  id: number;
  club: { id: number; name: string };
  user: { id: number; username: string };
  role: MemberRole;
}

export function getMemberList(clubId: number, data: { page: number; size: number; role?: MemberRole }) {
  return request({
    url: `${BASE_URI}/clubs/${clubId}/members`,
    method: 'get',
    params: data
  });
}

export function addMember(clubId: number, data: { userId: number; role?: MemberRole }) {
  return request({
    url: `${BASE_URI}/clubs/${clubId}/members`,
    method: 'post',
    data
  });
}

export function removeMember(clubId: number, userId: number) {
  return request({
    url: `${BASE_URI}/clubs/${clubId}/members/${userId}`,
    method: 'delete'
  });
}

export function updateMemberRole(clubId: number, userId: number, role: MemberRole) {
  return request({
    url: `${BASE_URI}/clubs/${clubId}/members/${userId}/role`,
    method: 'put',
    data: { role }
  });
}
