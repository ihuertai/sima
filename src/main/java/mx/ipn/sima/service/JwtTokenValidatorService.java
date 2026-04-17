package mx.ipn.sima.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.ipn.sima.dto.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class JwtTokenValidatorService {

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final String issuer;

    public JwtTokenValidatorService(ObjectMapper objectMapper,
                                    @Value("${app.jwt.secret}") String secret,
                                    @Value("${app.jwt.issuer:mx.ipn.cajeme.login}") String issuer) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.issuer = issuer;
    }

    public AuthenticatedUser validate(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token no proporcionado");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token invalido");
        }

        String signedContent = parts[0] + "." + parts[1];
        String expectedSignature = sign(signedContent);
        if (!expectedSignature.equals(parts[2])) {
            throw new IllegalArgumentException("Firma del token invalida");
        }

        Map<String, Object> payload = decodePayload(parts[1]);
        validatePayload(payload);

        Long userId = ((Number) payload.get("uid")).longValue();
        String username = String.valueOf(payload.get("sub"));
        String email = String.valueOf(payload.get("email"));
        Object rawRoles = payload.get("roles");
        List<String> roles = rawRoles instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : Collections.emptyList();

        return new AuthenticatedUser(userId, username, email, roles);
    }

    private Map<String, Object> decodePayload(String encodedPayload) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
            return objectMapper.readValue(decoded, Map.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No fue posible leer el token", ex);
        }
    }

    private void validatePayload(Map<String, Object> payload) {
        if (!issuer.equals(String.valueOf(payload.get("iss")))) {
            throw new IllegalArgumentException("Emisor invalido");
        }

        long expiration = ((Number) payload.get("exp")).longValue();
        if (Instant.ofEpochSecond(expiration).isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expirado");
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] signature = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible validar el token", ex);
        }
    }
}
