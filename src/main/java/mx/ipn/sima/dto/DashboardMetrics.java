package mx.ipn.sima.dto;

import mx.ipn.sima.model.CampanaEnvio;
import mx.ipn.sima.model.InteraccionCliente;

import java.util.List;

public record DashboardMetrics(
        long totalClientes,
        long totalAnuncios,
        long totalCampanas,
        long campanasProgramadas,
        long campanasEjecutadas,
        long totalEnviosExitosos,
        long totalEnviosError,
        long totalInteracciones,
        long solicitudesContacto,
        long solicitudesMasInfo,
        List<CampanaEnvio> recentCampaigns,
        List<InteraccionCliente> recentInteractions
) {
}