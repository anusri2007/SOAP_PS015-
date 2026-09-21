package com.talentacquisition.applicationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service")
public interface ProfileServiceClient {

    @GetMapping("/api/profiles/candidate/{candidateId}")
    ProfileResponseDto getProfileByCandidateId(@PathVariable("candidateId") Long candidateId);
}

