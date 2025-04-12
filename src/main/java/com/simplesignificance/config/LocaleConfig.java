package com.simplesignificance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.context.annotation.Bean;

import java.util.Locale;

@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(LocaleConfig.class);

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        logger.info("LocaleResolver bean configured with default ENGLISH (session-based)");
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");

        registry.addInterceptor(interceptor);
        logger.info("LocaleChangeInterceptor registered via addInterceptors()");
    }
}