package tech.wetech.admin3.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.wetech.admin3.sys.model.User;
import tech.wetech.admin3.sys.service.RegistrationService;

/**
 * 用户注册 Controller
 */
@RestController
public class RegisterController {

  private final RegistrationService registrationService;

  public RegisterController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
    if (!request.password().equals(request.confirmPassword())) {
      return ResponseEntity.badRequest().body(new RegisterResponse("两次输入的密码不一致"));
    }
    try {
      User user = registrationService.register(
        request.username(),
        request.password(),
        request.realName(),
        request.roleKey()
      );
      return ResponseEntity.ok(new RegisterResponse("注册成功"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new RegisterResponse(e.getMessage()));
    }
  }

  record RegisterRequest(
    @NotBlank @Size(min = 4, max = 20) @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "用户名只能包含字母和数字") String username,
    @NotBlank @Size(min = 6, max = 20) String password,
    @NotBlank String confirmPassword,
    @NotBlank String realName,
    @NotBlank @Pattern(regexp = "^(student|teacher)$", message = "身份类型无效") String roleKey
  ) {}

  record RegisterResponse(String message) {}
}
