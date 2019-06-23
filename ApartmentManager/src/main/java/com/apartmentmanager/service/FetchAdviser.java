package com.apartmentmanager.service;

import com.apartmentmanager.dto.apartment.ApartmentInfoView;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FetchAdviser {

    private static final String NAME_SUGGESTION_KEY = "autocomplete:loginUser@%s.suggestion.names";

    @Autowired
    private RedisTemplate<String, Map<String, Set<String>>> redisTemplate;

    public Set<String> adviseNames(String userId) {
        Set<String> values = new HashSet<>();
        Optional.ofNullable(redisTemplate.<String, Set<String>>opsForHash().values(String.format(NAME_SUGGESTION_KEY, userId)))
                .ifPresent(v -> v.forEach(values::addAll));

        return values;

    }

    public void saveNameSuggestions(String userId, List<ApartmentInfoView> apartmentInfoViews) {
        if (CollectionUtils.isEmpty(apartmentInfoViews)) {
            return;
        }

        Map<String, Set<String>> namesMap = apartmentInfoViews.stream()
                .collect(Collectors.toMap(a -> String.valueOf(a.getApartmentId()), a -> {
                    Set<String> names = new HashSet<>(2);
                    names.add(a.getName());
                    if (StringUtils.isNotBlank(a.getPrimaryCustomerName())) {
                        names.add(a.getPrimaryCustomerName());
                    }
                    return names;
                }));
        redisTemplate.opsForHash().putAll(String.format(NAME_SUGGESTION_KEY, userId), namesMap);
    }

    public void removeSuggestionsByApartmentId(String userId, Integer apartmentId) {
        redisTemplate.opsForHash().delete(String.format(NAME_SUGGESTION_KEY, userId), String.valueOf(apartmentId));
    }
}

