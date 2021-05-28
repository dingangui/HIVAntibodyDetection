package HAD.controller;

import HAD.entity.User;
import HAD.mapper.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

//负责处理登录注册的控制器
@Controller
public class LoginHandler {

  @Resource
  private UserRepository userRepository;

  //  输入http://localhost:9090/index，进入login.html
  @RequestMapping("/index")
  public String login(Model model) {
    model.addAttribute("warning", "false");
    return "login";
  }

  //  提交登录请求
  @PostMapping("/login")
  public String login(User user, HttpSession session, Model model) {

//    不为空，表示查到了
//    登录成功，则跳转到待处理页面
    if (userRepository.login(user) != null) {
      session.setAttribute("username", user.getUsername());
      return "redirect:/pendingSamples";
    } else {
      //      登录失败，返回登录页
      model.addAttribute("warning", "true");
      model.addAttribute("message", "用户名或密码错误");
      return "login";
    }
  }

  //  注册
  @PostMapping("/register")
  public String register(User user, HttpSession session, Model model) {
    if (user.getUsername().length() == 0) {
      model.addAttribute("warning", "true");
      model.addAttribute("message", "用户名不能为空");
      return "login";
    }
    if (user.getPassword().length() < 6) {
      model.addAttribute("warning", "true");
      model.addAttribute("message", "密码不能小于6位");
      return "login";
    }
    if (userRepository.findByUsername(user.getUsername()) != null) {
      model.addAttribute("warning", "true");
      model.addAttribute("message", "用户名已存在");
      return "login";
    }
    userRepository.save(user);
    session.setAttribute("username", user.getUsername());
    return "redirect:/pendingSamples";
  }

  @RequestMapping("/logout")
  public String logout(HttpSession session) {
    session.removeAttribute("username");
    session.invalidate();
    return "redirect:/index";
  }

}