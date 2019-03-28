package ru.ku88.account.core.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport

@Configuration
class Config : WebMvcConfigurationSupport() {

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer
                .defaultContentType(MediaType.APPLICATION_JSON)
                .ignoreAcceptHeader(true)
    }
}