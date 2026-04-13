package ba.maloprodaja.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.slike-putanja}")
    private String uploadPutanja;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadPutanja.endsWith("/")
                ? "file:" + uploadPutanja
                : "file:" + uploadPutanja + "/";
        registry.addResourceHandler("/slike/**")
                .addResourceLocations(location);
    }
}
