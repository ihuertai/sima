package mx.ipn.sima.service;

import mx.ipn.sima.model.CampanaEnvio;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CampanaSchedulerService {

    private final CampanaService campanaService;

    public CampanaSchedulerService(CampanaService campanaService) {
        this.campanaService = campanaService;
    }

    @Scheduled(fixedDelay = 60000)
    public void executeDueCampaigns() {
        for (CampanaEnvio campana : campanaService.getDueCampaigns()) {
            campanaService.executeCampaign(campana.getId());
        }
    }
}