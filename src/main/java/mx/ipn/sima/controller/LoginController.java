package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final String loginUrl;

    public LoginController(@Value("${app.sso.login-url:http://localhost:5173}") String loginUrl) {
        this.loginUrl = loginUrl;
    }

    @GetMapping("/")
    public String login(HttpSession session) {
        return session.getAttribute("AUTHENTICATED_USER") != null
                ? "redirect:/dashboard"
                : "redirect:" + loginUrl;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/gestion-clientes")
    public String gestionClientes() {
        return "cliente-lista";
    }

    @GetMapping("/gestion-anuncios")
    public String gestionAnuncios() {
        return "anuncio-lista";
    }
}
