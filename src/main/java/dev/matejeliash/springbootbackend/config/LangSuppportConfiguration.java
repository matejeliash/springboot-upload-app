package dev.matejeliash.springbootbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;
/*
Provides all configuration needed for language support,
files for specific languages are in resources/messages/message_<lang>.properties

*/
@Configuration
public class LangSuppportConfiguration implements WebMvcConfigurer {

    @Bean
    public ResourceBundleMessageSource messageSource(){
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();

        source.setBasenames("messages.messages");
        source.setDefaultEncoding("UTF-8"); // maybe try unicode for better support
        source.setUseCodeAsDefaultMessage(true); // fallback to code e.g in HTML
        return source;
    }

    @Bean
    public LocaleResolver localeResolver(){
        SessionLocaleResolver resolver  = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);// fallback to US
        return resolver;
    }

    // this provides way to set lang via path variable like ?last=us
    public LocaleChangeInterceptor localeChangeInterceptor(){
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return  interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

}
