package com.account.moneytranser.config.togglz;

import com.account.moneytranser.feature.MyFeatures;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

@Configuration
public class TogglzConfiguration {

    @Bean
    public FeatureManager featureManager() {
        return new FeatureManagerBuilder()
                .featureEnum(MyFeatures.class)
                .stateRepository(featureStateRepository())
                .build();
    }

    @Bean
    public StateRepository featureStateRepository() {
        return new InMemoryStateRepository();
    }
}
