package com.example.dashboard.config;

import com.example.dashboard.model.Project;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, Project>() {
            @Override
            public Project convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                try {
                    return new Project(Long.valueOf(source.trim()));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
    }
}
