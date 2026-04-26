package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.service.DashboardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final String loginUrl;
    private final DashboardService dashboardService;

    public LoginController(@Value("${app.sso.login-url:http://localhost:5173}") String loginUrl,
                           DashboardService dashboardService) {
        this.loginUrl = loginUrl;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String login(HttpSession session) {
        return session.getAttribute("AUTHENTICATED_USER") != null
                ? "redirect:/dashboard"
                : "redirect:" + loginUrl;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("metrics", dashboardService.getMetrics());
        return "dashboard";
    }

    @GetMapping("/gestion-clientes")
    public String gestionClientes() {
        return "redirect:/clientes/lista";
    }

    @GetMapping("/gestion-anuncios")
    public String gestionAnuncios() {
        return "redirect:/anuncios/lista";
    }
}