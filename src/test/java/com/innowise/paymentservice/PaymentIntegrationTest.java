package com.innowise.paymentservice;

import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWireMock(port = 0)
@Import(TestSecurityConfig.class)
class PaymentIntegrationTest {

    @Container
    static MongoDBContainer mongoDB = new MongoDBContainer(DockerImageName.parse("mongo:6.0.1"));

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    @Autowired
    private PaymentService paymentService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        mongoDB.start();
        kafka.start();

        registry.add("spring.data.mongodb.uri", mongoDB::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("random.api.url", () -> "http://localhost:" + System.getProperty("wiremock.server.port"));
    }

    @Test
    void testFullPaymentFlow() {
        stubFor(get(urlPathMatching("/decimal/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("50")));

        PaymentRequestDto request = new PaymentRequestDto("order1", BigDecimal.valueOf(200.0));
        paymentService.createPayment(request, "user1");
    }
}