/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

//import io.mandrel.common.settings.InfoSettings;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
//import com.mangofactory.swagger.models.dto.ApiInfo;
//import com.mangofactory.swagger.models.dto.builder.ApiInfoBuilder;
//import com.mangofactory.swagger.paths.AbsoluteSwaggerPathProvider;
//import com.mangofactory.swagger.plugin.EnableSwagger;
//import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
//
//@Configuration
//@EnableSwagger
//public class SwaggerConfiguration {
//
//	private SpringSwaggerConfig springSwaggerConfig;
//
//	@Autowired
//	public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
//		this.springSwaggerConfig = springSwaggerConfig;
//	}
//
//	@Bean
//	public SwaggerSpringMvcPlugin customImplementation(InfoSettings settings) {
//		SwaggerSpringMvcPlugin swaggerSpringMvcPlugin = new SwaggerSpringMvcPlugin(this.springSwaggerConfig);
//		return swaggerSpringMvcPlugin.pathProvider(swaggerPathProvider()).apiInfo(apiInfo(settings)).includePatterns("/logs", "/nodes", "/spiders")
//				.apiVersion(settings.getVersion());
//	}
//
//	private ApiInfo apiInfo(InfoSettings settings) {
//		ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
//		apiInfoBuilder.description(settings.getDescription());
//		apiInfoBuilder.title(settings.getName() + "(" + settings.getArtifact() + ")");
//		return apiInfoBuilder.build();
//	}
//
//	@Bean
//	public AbsoluteSwaggerPathProvider swaggerPathProvider() {
//		return new AbsoluteSwaggerPathProvider();
//	}
}
