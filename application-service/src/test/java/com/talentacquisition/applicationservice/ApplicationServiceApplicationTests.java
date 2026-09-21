package com.talentacquisition.applicationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.talentacquisition.applicationservice.client.JobServiceClient;
import com.talentacquisition.applicationservice.client.ProfileServiceClient;

@SpringBootTest
class ApplicationServiceApplicationTests {

    @MockBean
    private JobServiceClient jobServiceClient;

    @MockBean
    private ProfileServiceClient profileServiceClient;

    @Test
    void contextLoads() {
    }
}

