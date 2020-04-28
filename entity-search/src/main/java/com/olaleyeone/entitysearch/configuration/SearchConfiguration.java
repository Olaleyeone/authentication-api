package com.olaleyeone.entitysearch.configuration;

import com.olaleyeone.entitysearch.JpaQuerySource;
import com.olaleyeone.entitysearch.util.PredicateExtractor;
import com.olaleyeone.entitysearch.util.SearchFilterPredicateExtractor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class SearchConfiguration {

    @Bean
    public PredicateExtractor predicateExtractor(ApplicationContext applicationContext) {
        return applicationContext.getAutowireCapableBeanFactory().createBean(PredicateExtractor.class);
    }

    @Bean
    public SearchFilterPredicateExtractor searchFilterPredicateExtractor(PredicateExtractor predicateExtractor) {
        return new SearchFilterPredicateExtractor(predicateExtractor);
    }

    @Bean
    public JpaQuerySource jpaQuerySource(EntityManager entityManager) {
        return new JpaQuerySource(entityManager);
    }
}
