package mx.ipn.sima.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WhatsappConfig {

    @Value("${whatsapp.api.url:https://graph.facebook.com/v21.0/}")
    private String apiUrl;

    @Value("${whatsapp.api.token}")
    private String accessToken;

    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;

    public String getFullUrl() { 
        return apiUrl + phoneNumberId + "/messages"; 
    }
    
    public String getAccessToken() { 
        return accessToken; 
    }
}