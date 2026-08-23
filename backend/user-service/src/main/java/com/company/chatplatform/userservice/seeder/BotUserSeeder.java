package com.company.chatplatform.userservice.seeder;

import com.company.chatplatform.userservice.domain.entity.UserPreference;
import com.company.chatplatform.userservice.domain.entity.UserProfile;
import com.company.chatplatform.userservice.domain.repository.UserPreferenceRepository;
import com.company.chatplatform.userservice.domain.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BotUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BotUserSeeder.class);
    
    public static final String ASSISTANT_BOT_ID = "018f98d0-0000-0000-0000-000000000000";

    private final UserProfileRepository userProfileRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public BotUserSeeder(UserProfileRepository userProfileRepository, UserPreferenceRepository userPreferenceRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userPreferenceRepository = userPreferenceRepository;
    }

    @Override
    public void run(String... args) {
        if (!userProfileRepository.existsById(ASSISTANT_BOT_ID)) {
            log.info("Seeding Aura Assistant bot user with ID: {}", ASSISTANT_BOT_ID);
            
            UserProfile botProfile = new UserProfile(
                    ASSISTANT_BOT_ID,
                    "aura-assistant",
                    "Aura Assistant",
                    "assistant@aura.chat"
            );
            botProfile.setBio("Your friendly Aura AI chatbot assistant");
            botProfile.setPhoneNumber("System");
            botProfile.setAvatarUrl("https://api.dicebear.com/7.x/bottts/svg?seed=AuraAssistant");
            botProfile.setStatusMessage("Always online");
            
            userProfileRepository.save(botProfile);
            
            UserPreference botPreference = new UserPreference(ASSISTANT_BOT_ID);
            userPreferenceRepository.save(botPreference);
            
            log.info("Aura Assistant bot user seeded successfully.");
        } else {
            log.debug("Aura Assistant bot user already exists in user-profiles repository.");
        }
    }
}
