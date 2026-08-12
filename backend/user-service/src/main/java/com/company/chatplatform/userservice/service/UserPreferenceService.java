package com.company.chatplatform.userservice.service;

import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.userservice.domain.entity.UserPreference;
import com.company.chatplatform.userservice.domain.repository.UserPreferenceRepository;
import com.company.chatplatform.userservice.dto.UserPreferenceDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
    }

    public UserPreferenceDto getPreferences(String userId) {
        UserPreference pref = userPreferenceRepository.findById(userId)
                .orElseGet(() -> userPreferenceRepository.save(new UserPreference(userId)));
        return new UserPreferenceDto(pref.getUserId(), pref.getTheme(), pref.isNotificationsEnabled(), pref.isSoundEnabled());
    }

    @Transactional
    public UserPreferenceDto updatePreferences(String userId, UserPreferenceDto request) {
        UserPreference pref = userPreferenceRepository.findById(userId)
                .orElseGet(() -> new UserPreference(userId));

        if (request.getTheme() != null) {
            pref.setTheme(request.getTheme());
        }
        pref.setNotificationsEnabled(request.isNotificationsEnabled());
        pref.setSoundEnabled(request.isSoundEnabled());
        pref.setUpdatedAt(Instant.now());

        userPreferenceRepository.save(pref);
        return new UserPreferenceDto(pref.getUserId(), pref.getTheme(), pref.isNotificationsEnabled(), pref.isSoundEnabled());
    }
}
