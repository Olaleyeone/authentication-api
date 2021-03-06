package com.olaleyeone.auth.configuration;

import com.github.olaleyeone.advice.ErrorAdvice;
import com.github.olaleyeone.auth.interceptors.AccessConstraintHandlerInterceptor;
import com.github.olaleyeone.configuration.BeanValidationConfiguration;
import com.github.olaleyeone.configuration.JacksonConfiguration;
import com.github.olaleyeone.configuration.PredicateConfiguration;
import com.github.olaleyeone.interceptor.TaskContextHandlerInterceptor;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springdoc.webmvc.ui.SwaggerWelcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@ComponentScan({
        "com.olaleyeone.auth.controller",
        "com.olaleyeone.auth.validator",
        "com.olaleyeone.auth.response.handler",
        "com.olaleyeone.auth.security.authorizer",
        "com.olaleyeone.auth.search"
})
@Import({
        RequestMetadataConfiguration.class,
        BeanValidationConfiguration.class,
        PredicateConfiguration.class,
        JacksonConfiguration.class,
        OpenApiConfiguration.class
})
public class WebConfiguration implements WebMvcConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ErrorAdvice errorAdvice() {
        return new ErrorAdvice();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        registry.addInterceptor(beanFactory.createBean(TaskContextHandlerInterceptor.class));
//        registry.addInterceptor(beanFactory.createBean(RemoteAddressConstraintHandlerInterceptor.class));
        AccessConstraintHandlerInterceptor accessConstraintHandlerInterceptor = new AccessConstraintHandlerInterceptor(
                applicationContext,
                Arrays.asList(BasicErrorController.class,
                        OpenApiResource.class,
                        SwaggerWelcome.class)
        );
        beanFactory.autowireBean(accessConstraintHandlerInterceptor);
        registry.addInterceptor(accessConstraintHandlerInterceptor);
    }
}
