package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.service.RoleAccessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/usuarios")
public class UsuariosExternosController {

    private final RoleAccessService roleAccessService;
    private final String loginAdminUrl;

    public UsuariosExternosController(RoleAccessService roleAccessService,
                                      @Value("${app.sso.login-admin-url:http://localhost:8080/auth/admin-bridge}") String loginAdminUrl) {
        this.roleAccessService = roleAccessService;
        this.loginAdminUrl = loginAdminUrl;
    }

    @GetMapping
    public String mostrarPantalla(HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        String authToken = (String) session.getAttribute("AUTH_TOKEN");
        String resolvedUrl = authToken != null && !authToken.isBlank()
                ? loginAdminUrl + "?token=" + URLEncoder.encode(authToken, StandardCharsets.UTF_8)
                : loginAdminUrl;
        return "redirect:" + resolvedUrl;
    }
}