package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.dto.AuthenticatedUser;
import mx.ipn.sima.service.RoleAccessService;
import mx.ipn.sima.service.WhatsappInteractionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final RoleAccessService roleAccessService;
    private final WhatsappInteractionService whatsappInteractionService;

    public SolicitudController(RoleAccessService roleAccessService,
                               WhatsappInteractionService whatsappInteractionService) {
        this.roleAccessService = roleAccessService;
        this.whatsappInteractionService = whatsappInteractionService;
    }

    @GetMapping
    public String listarSolicitudes(Model model, HttpSession session) {
        roleAccessService.requireRequestsView(session);
        AuthenticatedUser user = roleAccessService.getCurrentUser(session);
        model.addAttribute("solicitudes", whatsappInteractionService.getInteractionsForUser(user));
        return "solicitudes-lista";
    }
}