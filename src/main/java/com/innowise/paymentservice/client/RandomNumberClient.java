package com.innowise.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "random-api", url = "https://www.random.org")
public interface RandomNumberClient {
    @GetMapping("/decimal/?num=1&min=1&max=100&col=1&format=plain&rnd=new")
    Integer getRandomNumber();
}