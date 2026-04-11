// package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.security;
// 
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// 
// @Configuration
// public class WebMvcConfig implements WebMvcConfigurer {
// 
//     @Value("${app.upload.dir}")
//     private String uploadDir;
// 
//     @Override
//     public void addResourceHandlers(ResourceHandlerRegistry registry) {
//         // Serve GET /images/** from the local uploads/images/ folder
//         registry.addResourceHandler("/images/**")
//             .addResourceLocations("file:///" + uploadDir + "/");
//     }
// }
