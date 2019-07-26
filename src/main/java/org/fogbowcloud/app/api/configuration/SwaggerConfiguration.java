package org.fogbowcloud.app.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static org.fogbowcloud.app.api.constants.Documentation.ApiInfo;

@EnableSwagger2
@Configuration
public class SwaggerConfiguration extends WebMvcConfigurationSupport {

    private static final String BASE_PACKAGE = "org.fogbowcloud.app.api.http";

    private static final Contact CONTACT =
            new Contact(ApiInfo.CONTACT_NAME, ApiInfo.CONTACT_URL, ApiInfo.CONTACT_EMAIL);

    @Bean
    public Docket apiDetails() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(this.apiInfo().build());
    }

    private ApiInfoBuilder apiInfo() {
        ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
        apiInfoBuilder.title(ApiInfo.TITLE);
        apiInfoBuilder.description(ApiInfo.DESCRIPTION);
        apiInfoBuilder.version("1.0");
        apiInfoBuilder.contact(CONTACT);

        return apiInfoBuilder;
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
