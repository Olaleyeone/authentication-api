package com.olaleyeone.auth.search;

import com.querydsl.core.types.EntityPath;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;

public interface SearchFilter<E, Q extends EntityPath<E>> extends QuerydslBinderCustomizer<Q> {
}
