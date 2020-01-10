package com.future.MRBS.service.ServiceImpl;

import com.future.MRBS.model.Config;
import com.future.MRBS.repository.ConfigRepository;
import com.future.MRBS.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service public class ConfigServiceImpl implements ConfigService {
    private static final String CONFIG_ID = "MRBS_C0NF1G_ID";
    private static final String DEFAULT_CODE = "TWICE2";

    private ConfigRepository configRepository;

    @Autowired public ConfigServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override public ResponseEntity<Config> getConfigCode(Authentication authentication) {
        Config code = configRepository.findConfigCodeById(CONFIG_ID);
        return code != null ?
            new ResponseEntity<>(code, HttpStatus.OK) :
            new ResponseEntity<>(new Config(CONFIG_ID, DEFAULT_CODE, null, null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Config> updateConfigCode(String newCode, Authentication authentication) {
        Config config = configRepository.findConfigCodeById(CONFIG_ID);
        if (config != null) {
            config.setCode(newCode);
            config.setLastEditAt(System.currentTimeMillis());
            config.setLastEditBy(authentication.getName());
        } else {
            config = new Config(CONFIG_ID, DEFAULT_CODE, authentication.getName(),
                System.currentTimeMillis());
        }
        configRepository.save(config);
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    @Override public ResponseEntity<Config> resetConfigCode(Authentication authentication) {
        Config config = new Config();
        config.setId(CONFIG_ID);
        config.setCode(DEFAULT_CODE);
        config.setLastEditAt(System.currentTimeMillis());
        config.setLastEditBy(authentication.getName());
        configRepository.save(config);
        return new ResponseEntity<>(config, HttpStatus.OK);
    }
}
