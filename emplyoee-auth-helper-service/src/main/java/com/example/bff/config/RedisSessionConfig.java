package com.example.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class RedisSessionConfig {

    @Value("${app.cookie.name:bff_session}")
    private String cookieName;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(cookieName);
        // set cookie path to root so it is available to other services on same domain
        serializer.setCookiePath("/");
        // if domain is empty, DefaultCookieSerializer will not set it and browser will use host-only cookie
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            serializer.setDomainName(cookieDomain);
        }
        // HttpOnly and Secure defaults: keep HttpOnly true, Secure left to container (can be set via properties)
        serializer.setUseHttpOnlyCookie(true);
        // SameSite (Lax, Strict, None)
        serializer.setSameSite(sameSite);
        return serializer;
    }
}
