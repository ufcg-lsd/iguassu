package org.fogbowcloud.app.api.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfiguration {

	public static final String BASE_PACKAGE = "org.fogbowcloud.app.api.http";

	public static final Contact CONTACT = new Contact(
			ApiDocumentation.ApiInfo.CONTACT_NAME,
			ApiDocumentation.ApiInfo.CONTACT_URL,
			ApiDocumentation.ApiInfo.CONTACT_EMAIL);

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
		apiInfoBuilder.title(ApiDocumentation.ApiInfo.API_TITLE);
		apiInfoBuilder.description(ApiDocumentation.ApiInfo.API_DESCRIPTION);
		apiInfoBuilder.version("1.0");
		apiInfoBuilder.contact(CONTACT);

		return apiInfoBuilder;
	}

}
