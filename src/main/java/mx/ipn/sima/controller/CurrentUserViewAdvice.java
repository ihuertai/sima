package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.dto.AuthenticatedUser;
import mx.ipn.sima.service.RoleAccessService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserViewAdvice {

    private final RoleAccessService roleAccessService;

    public CurrentUserViewAdvice(RoleAccessService roleAccessService) {
        this.roleAccessService = roleAccessService;
    }

    @ModelAttribute("currentUser")
    public AuthenticatedUser currentUser(HttpSession session) {
        Object value = session != null ? session.getAttribute("AUTHENTICATED_USER") : null;
        return value instanceof AuthenticatedUser user ? user : null;
    }

    @ModelAttribute("canManageClients")
    public boolean canManageClients(HttpSession session) {
        AuthenticatedUser user = currentUser(session);
        return user != null && roleAccessService.canManageClients(user);
    }

    @ModelAttribute("canManageAds")
    public boolean canManageAds(HttpSession session) {
        AuthenticatedUser user = currentUser(session);
        return user != null && roleAccessService.canManageAds(user);
    }

    @ModelAttribute("canManageCampaigns")
    public boolean canManageCampaigns(HttpSession session) {
        AuthenticatedUser user = currentUser(session);
        return user != null && roleAccessService.canManageCampaigns(user);
    }

    @ModelAttribute("canViewRequests")
    public boolean canViewRequests(HttpSession session) {
        AuthenticatedUser user = currentUser(session);
        return user != null && roleAccessService.canViewRequests(user);
    }
}