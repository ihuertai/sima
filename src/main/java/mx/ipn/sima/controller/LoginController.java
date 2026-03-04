package mx.ipn.sima.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String login() {
        return "login";
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
