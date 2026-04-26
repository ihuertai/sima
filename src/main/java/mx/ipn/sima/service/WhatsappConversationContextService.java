package mx.ipn.sima.service;

import mx.ipn.sima.model.CampanaEnvio;
import mx.ipn.sima.model.Cliente;
import mx.ipn.sima.model.WhatsappConversationContext;
import mx.ipn.sima.repository.WhatsappConversationContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WhatsappConversationContextService {

    private final WhatsappConversationContextRepository repository;

    public WhatsappConversationContextService(WhatsappConversationContextRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registerLastSentContext(Cliente cliente, CampanaEnvio campana) {
        if (cliente == null || campana == null || cliente.getTelefono() == null || cliente.getTelefono().isBlank()) {
            return;
        }

        String normalizedPhone = normalizePhone(cliente.getTelefono());
        WhatsappConversationContext context = repository.findByPhoneNumber(normalizedPhone)
                .orElseGet(WhatsappConversationContext::new);
        context.setPhoneNumber(normalizedPhone);
        context.setCliente(cliente);
        context.setCampana(campana);
        context.setAnuncio(campana.getAnuncio());
        context.setJefeResponsable(cliente.getJefeSucursal());
        context.setProductoNombre(campana.getAnuncio() != null ? campana.getAnuncio().getTitulo() : null);
        repository.save(context);
    }

    @Transactional(readOnly = true)
    public ConversationContextData getLastContext(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        return repository.findByPhoneNumber(normalizePhone(phone))
                .map(context -> new ConversationContextData(
                        context.getCliente(),
                        context.getCampana(),
                        context.getAnuncio(),
                        context.getJefeResponsable(),
                        context.getProductoNombre()
                ))
                .orElse(null);
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("\\D", "");
    }

    public record ConversationContextData(
            Cliente cliente,
            CampanaEnvio campana,
            mx.ipn.sima.model.Anuncio anuncio,
            mx.ipn.sima.model.Empleado jefeResponsable,
            String productoNombre
    ) {
    }
}