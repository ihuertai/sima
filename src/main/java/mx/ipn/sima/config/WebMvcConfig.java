package mx.ipn.sima.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final SsoSessionInterceptor ssoSessionInterceptor;

    public WebMvcConfig(SsoSessionInterceptor ssoSessionInterceptor) {
        this.ssoSessionInterceptor = ssoSessionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ssoSessionInterceptor)
                .addPathPatterns(
                        "/dashboard",
                        "/gestion-clientes",
                        "/gestion-anuncios",
                        "/clientes/**",
                        "/anuncios/**",
                        "/envio/**"
                )
                .excludePathPatterns(
                        "/",
                        "/auth/**",
                        "/webhook/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/docs/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}
