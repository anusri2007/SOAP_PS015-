package com.talentacquisition.applicationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.talentacquisition.applicationservice.client.JobServiceClient;

@SpringBootTest
class ApplicationServiceApplicationTests {

    @MockBean
    private JobServiceClient jobServiceClient;

    @Test
    void contextLoads() {
    }
}

