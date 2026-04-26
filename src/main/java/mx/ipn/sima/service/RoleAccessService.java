package mx.ipn.sima.service;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.dto.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class RoleAccessService {

    public AuthenticatedUser getCurrentUser(HttpSession session) {
        Object value = session != null ? session.getAttribute("AUTHENTICATED_USER") : null;
        if (value instanceof AuthenticatedUser user) {
            return user;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "SesiÃ³n no vÃ¡lida");
    }

    public boolean canManageClients(AuthenticatedUser user) {
        return isGerente(user) || isSucursal(user);
    }

    public boolean canManageCampaigns(AuthenticatedUser user) {
        return isGerente(user);
    }

    public boolean canManageAds(AuthenticatedUser user) {
        return isGerente(user);
    }

    public boolean canViewRequests(AuthenticatedUser user) {
        return isGerente(user) || isSucursal(user);
    }

    public boolean isGerente(AuthenticatedUser user) {
        return hasAnyToken(user.roles(), "GERENTE", "ADMIN");
    }

    public boolean isSucursal(AuthenticatedUser user) {
        return hasAnyToken(user.roles(), "SUCURSAL");
    }

    public void requireClientManagement(HttpSession session) {
        AuthenticatedUser user = getCurrentUser(session);
        if (!canManageClients(user)) {
            throw forbidden("No tienes permisos para gestionar clientes");
        }
    }

    public void requireCampaignManagement(HttpSession session) {
        AuthenticatedUser user = getCurrentUser(session);
        if (!canManageCampaigns(user)) {
            throw forbidden("Solo un gerente puede gestionar campaÃ±as y envÃ­os");
        }
    }

    public void requireAdsManagement(HttpSession session) {
        AuthenticatedUser user = getCurrentUser(session);
        if (!canManageAds(user)) {
            throw forbidden("Solo un gerente puede gestionar anuncios");
        }
    }

    public void requireRequestsView(HttpSession session) {
        AuthenticatedUser user = getCurrentUser(session);
        if (!canViewRequests(user)) {
            throw forbidden("No tienes permisos para revisar solicitudes");
        }
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private boolean hasAnyToken(List<String> roles, String... tokens) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.toUpperCase(Locale.ROOT))
                .anyMatch(role -> {
                    for (String token : tokens) {
                        if (role.contains(token)) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}