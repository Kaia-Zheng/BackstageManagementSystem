<template>
  <div class="login-wrap">
    <div class="ms-login" style="margin: -300px 0 0 -175px;">
      <div class="ms-title">用户注册</div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0px" class="ms-content">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="学号/教职工号">
            <template #prepend>
              <el-button :icon="User" disabled></el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input type="password" placeholder="密码（6-20位）" v-model="form.password" show-password>
            <template #prepend>
              <el-button :icon="Lock" disabled></el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input type="password" placeholder="确认密码" v-model="form.confirmPassword" show-password>
            <template #prepend>
              <el-button :icon="Lock" disabled></el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名">
            <template #prepend>
              <el-button :icon="UserFilled" disabled></el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号">
            <template #prepend>
              <el-button :icon="Iphone" disabled></el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱">
            <template #prepend>
              <el-button :icon="Message" disabled></el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="roleKey">
          <el-select v-model="form.roleKey" placeholder="请选择身份" style="width: 100%;">
            <el-option label="学生" value="student" />
            <el-option label="教职工" value="teacher" />
          </el-select>
        </el-form-item>
        <div class="login-btn">
          <el-button type="primary" @click="submitForm" :loading="submitting">注册</el-button>
        </div>
        <p class="login-tips" style="text-align: center;">
          已有账号？<el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
        </p>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { Lock, User, UserFilled, Iphone, Message } from '@element-plus/icons-vue';
import { register } from '../api/register';

const router = useRouter();
const formRef = ref<FormInstance>();
const submitting = ref(false);

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  roleKey: ''
});

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度为4-20位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '用户名只能包含字母和数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  roleKey: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ]
};

const submitForm = () => {
  if (!formRef.value) return;
  formRef.value.validate((valid: boolean) => {
    if (!valid) return;
    submitting.value = true;
    register(form).then(() => {
      ElMessage.success('注册成功，请登录');
      router.push('/login');
    }).catch((err) => {
      const msg = err?.response?.data?.message || err?.message || '注册失败';
      ElMessage.error(msg);
    }).finally(() => {
      submitting.value = false;
    });
  });
};
</script>

<style scoped>
.login-wrap {
  position: relative;
  width: 100%;
  height: 100%;
  background-image: url(../assets/img/login-bg.jpg);
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
}

.login-wrap::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 0;
}

.ms-title {
  width: 100%;
  line-height: 50px;
  text-align: center;
  font-size: 20px;
  color: #fff;
  border-bottom: 1px solid #ddd;
}

.ms-login {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 350px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.3);
  overflow: hidden;
  z-index: 1;
}

.ms-content {
  padding: 30px 30px;
}

.login-btn {
  text-align: center;
}

.login-btn button {
  width: 100%;
  height: 36px;
  margin-bottom: 10px;
}

.login-tips {
  font-size: 12px;
  line-height: 30px;
  color: #fff;
}
</style>
