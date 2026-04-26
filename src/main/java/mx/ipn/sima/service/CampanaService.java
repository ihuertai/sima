package mx.ipn.sima.service;

import mx.ipn.sima.model.*;
import mx.ipn.sima.repository.CampanaDestinatarioRepository;
import mx.ipn.sima.repository.CampanaEnvioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class CampanaService {

    private final CampanaEnvioRepository campanaEnvioRepository;
    private final CampanaDestinatarioRepository campanaDestinatarioRepository;
    private final AlmacenService almacenService;
    private final WhatsappService whatsappService;
    private final WhatsappConversationContextService conversationContextService;

    public CampanaService(CampanaEnvioRepository campanaEnvioRepository,
                          CampanaDestinatarioRepository campanaDestinatarioRepository,
                          AlmacenService almacenService,
                          WhatsappService whatsappService,
                          WhatsappConversationContextService conversationContextService) {
        this.campanaEnvioRepository = campanaEnvioRepository;
        this.campanaDestinatarioRepository = campanaDestinatarioRepository;
        this.almacenService = almacenService;
        this.whatsappService = whatsappService;
        this.conversationContextService = conversationContextService;
    }

    @Transactional
    public CampanaEnvio crearCampana(CampanaEnvio campana) {
        campana.setAnuncio(almacenService.getAnuncio(campana.getAnuncio().getId()));
        campana.setCreadoPor(campana.getCreadoPor() != null && campana.getCreadoPor().getId() != null
                ? almacenService.getEmpleado(campana.getCreadoPor().getId())
                : null);
        campana.setSucursal(campana.getSucursal() != null && campana.getSucursal().getId() != null
                ? almacenService.getSucursal(campana.getSucursal().getId())
                : null);
        normalizeFilters(campana);

        if (Boolean.TRUE.equals(campana.getEnviarAhora())) {
            campana.setEstado(EstadoCampana.EN_PROCESO);
            campana.setProgramadaPara(LocalDateTime.now());
        } else if (campana.getProgramadaPara() != null) {
            campana.setEstado(EstadoCampana.PROGRAMADA);
        } else {
            campana.setEstado(EstadoCampana.BORRADOR);
        }

        CampanaEnvio saved = campanaEnvioRepository.save(campana);
        recalculateRecipients(saved);
        if (Boolean.TRUE.equals(saved.getEnviarAhora())) {
            executeCampaign(saved.getId());
            return campanaEnvioRepository.findById(saved.getId()).orElse(saved);
        }
        return saved;
    }

    @Transactional
    public void executeCampaign(Long campanaId) {
        CampanaEnvio campana = getCampana(campanaId);
        List<CampanaDestinatario> destinatarios = campanaDestinatarioRepository.findAllByCampanaOrderByClienteNombreAsc(campana);
        if (destinatarios.isEmpty()) {
            recalculateRecipients(campana);
            destinatarios = campanaDestinatarioRepository.findAllByCampanaOrderByClienteNombreAsc(campana);
        }

        campana.setEstado(EstadoCampana.EN_PROCESO);
        campanaEnvioRepository.save(campana);

        int enviados = 0;
        int errores = 0;
        for (CampanaDestinatario destinatario : destinatarios) {
            Cliente cliente = destinatario.getCliente();
            Anuncio anuncio = campana.getAnuncio();
            boolean ok = whatsappService.sendTemplateWithImage(
                    cliente.getTelefono(),
                    anuncio.getImagen(),
                    List.of(anuncio.getTexto())
            );

            destinatario.setFechaIntento(LocalDateTime.now());
            if (ok) {
                destinatario.setEstado(EstadoDestinatario.ENVIADO);
                destinatario.setDetalleError(null);
                enviados++;
                conversationContextService.registerLastSentContext(cliente, campana);
            } else {
                destinatario.setEstado(EstadoDestinatario.ERROR);
                destinatario.setDetalleError("Meta no acepto el envio o no fue posible enviarlo.");
                errores++;
            }
        }

        campana.setEnviarAhora(Boolean.FALSE);
        campana.setUltimoEnvioAt(LocalDateTime.now());
        campana.setEnviosExitosos(enviados);
        campana.setEnviosError(errores);
        campana.setEstado(errores > 0 ? EstadoCampana.EJECUTADA_CON_ERRORES : EstadoCampana.EJECUTADA);
        campanaEnvioRepository.save(campana);
    }

    @Transactional
    public int recalculateRecipients(Long campanaId) {
        CampanaEnvio campana = getCampana(campanaId);
        return recalculateRecipients(campana);
    }

    @Transactional
    public int recalculateRecipients(CampanaEnvio campana) {
        List<Cliente> clientes = almacenService.getClientes();
        List<Cliente> seleccionados = clientes.stream()
                .filter(cliente -> matches(campana, cliente))
                .toList();

        campanaDestinatarioRepository.deleteAllByCampana(campana);

        List<CampanaDestinatario> nuevos = new ArrayList<>();
        for (Cliente cliente : seleccionados) {
            CampanaDestinatario destinatario = new CampanaDestinatario();
            destinatario.setCampana(campana);
            destinatario.setCliente(cliente);
            nuevos.add(destinatario);
        }
        campanaDestinatarioRepository.saveAll(nuevos);

        campana.setTotalDestinatarios(seleccionados.size());
        campana.setEnviosExitosos(0);
        campana.setEnviosError(0);
        campanaEnvioRepository.save(campana);
        return seleccionados.size();
    }

    @Transactional(readOnly = true)
    public List<CampanaEnvio> getCampanas() {
        return campanaEnvioRepository.findAllByActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public CampanaEnvio getCampana(Long id) {
        return campanaEnvioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campana no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<CampanaDestinatario> getDestinatarios(Long campanaId) {
        return campanaDestinatarioRepository.findAllByCampanaOrderByClienteNombreAsc(getCampana(campanaId));
    }

    @Transactional(readOnly = true)
    public List<CampanaEnvio> getDueCampaigns() {
        return campanaEnvioRepository.findAllByActiveTrueAndEstadoAndProgramadaParaLessThanEqual(
                EstadoCampana.PROGRAMADA,
                LocalDateTime.now()
        );
    }

    private boolean matches(CampanaEnvio campana, Cliente cliente) {
        if (campana.getSucursal() != null) {
            Long campanaSucursalId = campana.getSucursal().getId();
            Long clienteSucursalId = cliente.getSucursal() != null ? cliente.getSucursal().getId() : null;
            if (!Objects.equals(campanaSucursalId, clienteSucursalId)) {
                return false;
            }
        }

        BigDecimal facturacion = cliente.getFacturacionMensual() != null ? cliente.getFacturacionMensual() : BigDecimal.ZERO;
        if (campana.getFacturacionMin() != null && facturacion.compareTo(campana.getFacturacionMin()) < 0) {
            return false;
        }
        if (campana.getFacturacionMax() != null && facturacion.compareTo(campana.getFacturacionMax()) > 0) {
            return false;
        }

        if (campana.getTamanoEmpresa() != null && campana.getTamanoEmpresa() != cliente.getTamanoEmpresa()) {
            return false;
        }

        if (hasText(campana.getCategoriaProducto()) && !containsIgnoreCase(cliente.getCategoriaProducto(), campana.getCategoriaProducto())) {
            return false;
        }

        if (hasText(campana.getGiro()) && !containsIgnoreCase(cliente.getGiro(), campana.getGiro())) {
            return false;
        }

        return true;
    }

    private void normalizeFilters(CampanaEnvio campana) {
        if (campana.getCategoriaProducto() != null) {
            campana.setCategoriaProducto(blankToNull(campana.getCategoriaProducto()));
        }
        if (campana.getGiro() != null) {
            campana.setGiro(blankToNull(campana.getGiro()));
        }
        if (campana.getFacturacionMin() != null && campana.getFacturacionMax() != null
                && campana.getFacturacionMin().compareTo(campana.getFacturacionMax()) > 0) {
            BigDecimal tmp = campana.getFacturacionMin();
            campana.setFacturacionMin(campana.getFacturacionMax());
            campana.setFacturacionMax(tmp);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String blankToNull(String value) {
        String trimmed = value != null ? value.trim() : null;
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private boolean containsIgnoreCase(String source, String token) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(token.toLowerCase(Locale.ROOT));
    }
}