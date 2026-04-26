package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.dto.AuthenticatedUser;
import mx.ipn.sima.service.CampanaService;
import mx.ipn.sima.service.RoleAccessService;
import mx.ipn.sima.service.WhatsappInteractionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final RoleAccessService roleAccessService;
    private final CampanaService campanaService;
    private final WhatsappInteractionService whatsappInteractionService;

    public ReporteController(RoleAccessService roleAccessService,
                             CampanaService campanaService,
                             WhatsappInteractionService whatsappInteractionService) {
        this.roleAccessService = roleAccessService;
        this.campanaService = campanaService;
        this.whatsappInteractionService = whatsappInteractionService;
    }

    @GetMapping
    public String verReportes(Model model, HttpSession session) {
        roleAccessService.requireRequestsView(session);
        AuthenticatedUser user = roleAccessService.getCurrentUser(session);
        model.addAttribute("campanas", campanaService.getCampanas());
        model.addAttribute("solicitudes", whatsappInteractionService.getInteractionsForUser(user));
        return "reportes";
    }

    @GetMapping("/campanas.csv")
    public ResponseEntity<byte[]> exportarCampanas(HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        StringBuilder csv = new StringBuilder();
        csv.append("Campana,Anuncio,Estado,Destinatarios,Exitosos,Errores,Programada\\n");
        campanaService.getCampanas().forEach(campana -> csv
                .append(escape(campana.getNombre())).append(',')
                .append(escape(campana.getAnuncio() != null ? campana.getAnuncio().getTitulo() : "")).append(',')
                .append(campana.getEstado()).append(',')
                .append(campana.getTotalDestinatarios()).append(',')
                .append(campana.getEnviosExitosos()).append(',')
                .append(campana.getEnviosError()).append(',')
                .append(escape(campana.getProgramadaPara() != null ? campana.getProgramadaPara().toString() : ""))
                .append("\\n"));
        return csvResponse("campanas.csv", csv.toString());
    }

    @GetMapping("/solicitudes.csv")
    public ResponseEntity<byte[]> exportarSolicitudes(HttpSession session) {
        roleAccessService.requireRequestsView(session);
        AuthenticatedUser user = roleAccessService.getCurrentUser(session);
        StringBuilder csv = new StringBuilder();
        csv.append("Fecha,Cliente,Telefono,Producto,Tipo,Responsable,Estado,Detalle\\n");
        whatsappInteractionService.getInteractionsForUser(user).forEach(interaccion -> csv
                .append(escape(interaccion.getFechaInteraccion() != null ? interaccion.getFechaInteraccion().toString() : "")).append(',')
                .append(escape(interaccion.getCliente() != null ? interaccion.getCliente().getNombre() : "")).append(',')
                .append(escape(interaccion.getTelefonoCliente())).append(',')
                .append(escape(interaccion.getProductoNombre())).append(',')
                .append(interaccion.getTipo()).append(',')
                .append(escape(interaccion.getJefeResponsable() != null ? interaccion.getJefeResponsable().getNombre() : "")).append(',')
                .append(interaccion.getEstadoSeguimiento()).append(',')
                .append(escape(interaccion.getMensajeCliente()))
                .append("\\n"));
        return csvResponse("solicitudes.csv", csv.toString());
    }

    private ResponseEntity<byte[]> csvResponse(String filename, String body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body.getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        String sanitized = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + sanitized + "\"";
    }
}