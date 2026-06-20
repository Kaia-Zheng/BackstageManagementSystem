import request from '../utils/request';
import { BASE_URI } from './base';

export interface RegisterParams {
  username: string;
  password: string;
  confirmPassword: string;
  realName: string;
  roleKey: string;
}

export function register(data: RegisterParams) {
  return request({
    url: `${BASE_URI}/register`,
    method: 'post',
    data
  });
}
