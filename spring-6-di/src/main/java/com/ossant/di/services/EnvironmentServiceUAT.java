package com.ossant.di.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("uat")
@Service
public class EnvironmentServiceUAT implements EnvironmentService {
    @Override
    public String getEnv() {
        return "UAT";
    }
}
