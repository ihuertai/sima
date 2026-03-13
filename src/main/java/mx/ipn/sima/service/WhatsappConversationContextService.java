package mx.ipn.sima.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WhatsappConversationContextService {

    private static final long CONTEXT_EXPIRATION_HOURS = 24;

    private final Map<String, ContextEntry> lastProductByPhone = new ConcurrentHashMap<>();

    public void registerLastSentProduct(String phone, String productName) {
        if (phone == null || phone.isBlank() || productName == null || productName.isBlank()) {
            return;
        }
        lastProductByPhone.put(normalizePhone(phone), new ContextEntry(productName.trim(), Instant.now()));
    }

    public String getLastProduct(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        ContextEntry entry = lastProductByPhone.get(normalizePhone(phone));
        if (entry == null) {
            return null;
        }

        Instant expirationLimit = entry.registeredAt().plus(CONTEXT_EXPIRATION_HOURS, ChronoUnit.HOURS);
        if (Instant.now().isAfter(expirationLimit)) {
            lastProductByPhone.remove(normalizePhone(phone));
            return null;
        }

        return entry.productName();
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("\\D", "");
    }

    private record ContextEntry(String productName, Instant registeredAt) {
    }
}
