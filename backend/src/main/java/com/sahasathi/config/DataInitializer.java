package com.sahasathi.config;

import com.sahasathi.model.Interest;
import com.sahasathi.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final InterestRepository interestRepository;

    @Override
    public void run(String... args) {
        if (interestRepository.count() > 0) {
            return;
        }

        List<Interest> interests = List.of(
                createInterest("Walking", "🚶"),
                createInterest("Yoga", "🧘"),
                createInterest("Gardening", "🌱"),
                createInterest("Reading", "📚"),
                createInterest("Music", "🎵"),
                createInterest("Cooking", "🍳"),
                createInterest("Travel", "✈️"),
                createInterest("Photography", "📷"),
                createInterest("Meditation", "🧠"),
                createInterest("Dancing", "💃"),
                createInterest("Board Games", "🎲"),
                createInterest("Swimming", "🏊"),
                createInterest("Painting", "🎨"),
                createInterest("Volunteering", "🤝"),
                createInterest("Badminton", "🏸"),
                createInterest("Card Games", "🃏")
        );

        interestRepository.saveAll(interests);
        log.info("{} interests seeded successfully", interests.size());
    }

    private Interest createInterest(String name, String icon) {
        return Interest.builder().name(name).icon(icon).build();
    }
}
