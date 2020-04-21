package com.olaleyeone.auth.search.util;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.QuerydslPredicateBuilder;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Named;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Named
public class PredicateExtractor {

    private final EntityPathResolver entityPathResolver;
    private final QuerydslPredicateBuilder predicateBuilder;
    private final Provider<WebRequest> webRequest;

    public PredicateExtractor(
            QuerydslBindingsFactory bindingsFactory,
            Optional<ConversionService> conversionService,
            Provider<WebRequest> webRequest) {

        this.entityPathResolver = bindingsFactory.getEntityPathResolver();
        this.predicateBuilder = new QuerydslPredicateBuilder(conversionService.orElseGet(DefaultConversionService::new),
                entityPathResolver);
        this.webRequest = webRequest;
    }

    public <E, Q extends EntityPath<E>> Predicate getPredicate(
            QuerydslBinderCustomizer<Q> binderCustomizer,
            Class<E> entityType) {
        return getPredicate(binderCustomizer, getParameterMap(), entityType);
    }

    public <E, Q extends EntityPath<E>> Predicate getPredicate(
            QuerydslBinderCustomizer<Q> binderCustomizer,
            MultiValueMap<String, String> parameters,
            Class<E> entityType) {
        QuerydslBindings querydslBindings = getQuerydslBindings(binderCustomizer, entityType);
        return predicateBuilder.getPredicate(ClassTypeInformation.from(entityType), parameters, querydslBindings);
    }

    public <E, Q extends EntityPath<E>> QuerydslBindings getQuerydslBindings(QuerydslBinderCustomizer<Q> binderCustomizer, Class<E> entityType) {
        Q entityPath = (Q) entityPathResolver.createPath(entityType);
        QuerydslBindings bindings = new QuerydslBindings();
        binderCustomizer.customize(bindings, entityPath);
        return bindings;
    }

    public MultiValueMap<String, String> getParameterMap() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        for (Map.Entry<String, String[]> entry : webRequest.get().getParameterMap().entrySet()) {
            parameters.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return parameters;
    }
}
