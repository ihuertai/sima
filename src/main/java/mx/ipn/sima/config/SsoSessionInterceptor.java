package mx.ipn.sima.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SsoSessionInterceptor implements HandlerInterceptor {

    private final String loginUrl;

    public SsoSessionInterceptor(@Value("${app.sso.login-url:http://localhost:5173}") String loginUrl) {
        this.loginUrl = loginUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("AUTHENTICATED_USER") != null) {
            return true;
        }

        response.sendRedirect(loginUrl);
        return false;
    }
}
