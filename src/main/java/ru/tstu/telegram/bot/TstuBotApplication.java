package ru.tstu.telegram.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ru.tstu.telegram"})
public class TstuBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TstuBotApplication.class, args);
    }

}
