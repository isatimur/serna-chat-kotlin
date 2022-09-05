//package com.timurisachenko.chatreactive.config
//
//import org.springframework.context.annotation.Configuration
//import org.springframework.web.reactive.config.CorsRegistry
//import org.springframework.web.reactive.config.EnableWebFlux
//import org.springframework.web.reactive.config.WebFluxConfigurer
//
//@Configuration
//@EnableWebFlux
//class WebConfig : WebFluxConfigurer {
//    override fun addCorsMappings(registry: CorsRegistry) {
//        registry.addMapping("/**")
//            .allowedOrigins("http://localhost:3000", "http://localhost:8080", "http://localhost:4200")
//    }
//}