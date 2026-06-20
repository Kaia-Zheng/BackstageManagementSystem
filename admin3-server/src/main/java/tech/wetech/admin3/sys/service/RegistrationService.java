package tech.wetech.admin3.sys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.wetech.admin3.common.BusinessException;
import tech.wetech.admin3.common.CommonResultStatus;
import tech.wetech.admin3.common.SecurityUtil;
import tech.wetech.admin3.sys.model.Role;
import tech.wetech.admin3.sys.model.User;
import tech.wetech.admin3.sys.model.UserCredential;
import tech.wetech.admin3.sys.repository.RoleRepository;
import tech.wetech.admin3.sys.repository.UserRepository;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 用户注册 Service
 */
@Service
public class RegistrationService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  public RegistrationService(UserRepository userRepository, RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  @Transactional
  public User register(String username, String password, String realName, String roleKey, String phone, String email) {
    // 1. 校验用户名是否已存在
    if (userRepository.findByUsername(username) != null) {
      throw new BusinessException(CommonResultStatus.PARAM_ERROR, "用户名已存在");
    }

    // 2. 根据roleKey查找角色
    String roleName = switch (roleKey) {
      case "student" -> "学生";
      case "teacher" -> "教职工";
      default -> throw new BusinessException(CommonResultStatus.PARAM_ERROR, "无效的身份类型");
    };

    Role role = roleRepository.findAll().stream()
      .filter(r -> r.getName().equals(roleName))
      .findFirst()
      .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "角色不存在：" + roleName));

    // 3. 创建用户
    User user = new User();
    user.setUsername(username);
    user.setRealName(realName);
    user.setPhone(phone);
    user.setEmail(email);
    user.setGender(User.Gender.MALE); // 默认
    user.setState(User.State.NORMAL);
    user.setRoles(new LinkedHashSet<>(Set.of(role)));

    // 4. 创建密码凭证
    UserCredential credential = new UserCredential();
    credential.setIdentifier(username);
    try {
      credential.setCredential(SecurityUtil.md5(username, password));
    } catch (NoSuchAlgorithmException e) {
      throw new BusinessException(CommonResultStatus.FAIL, "密码加密失败");
    }
    credential.setIdentityType(UserCredential.IdentityType.PASSWORD);
    credential.setUser(user);
    user.setCredentials(new LinkedHashSet<>(Set.of(credential)));

    // 5. 保存
    return userRepository.save(user);
  }
}
