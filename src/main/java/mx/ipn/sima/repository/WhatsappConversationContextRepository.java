package mx.ipn.sima.repository;

import mx.ipn.sima.model.WhatsappConversationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WhatsappConversationContextRepository extends JpaRepository<WhatsappConversationContext, Long> {
    Optional<WhatsappConversationContext> findByPhoneNumber(String phoneNumber);
}