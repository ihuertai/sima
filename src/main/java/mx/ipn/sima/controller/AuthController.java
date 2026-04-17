package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.dto.AuthenticatedUser;
import mx.ipn.sima.service.JwtTokenValidatorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenValidatorService jwtTokenValidatorService;
    private final String loginUrl;

    public AuthController(JwtTokenValidatorService jwtTokenValidatorService,
                          @Value("${app.sso.login-url:http://localhost:5173}") String loginUrl) {
        this.jwtTokenValidatorService = jwtTokenValidatorService;
        this.loginUrl = loginUrl;
    }

    @GetMapping("/callback")
    public String callback(@RequestParam String token, HttpServletRequest request) {
        AuthenticatedUser user = jwtTokenValidatorService.validate(token);
        HttpSession session = request.getSession(true);
        session.setAttribute("AUTHENTICATED_USER", user);
        session.setAttribute("AUTH_TOKEN", token);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:" + loginUrl;
    }
}
