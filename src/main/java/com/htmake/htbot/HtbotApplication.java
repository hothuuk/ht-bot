package com.htmake.htbot;

import com.htmake.htbot.discord.bot.HtBot;
import com.htmake.htbot.global.unirest.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.security.auth.login.LoginException;


@Slf4j
@EnableScheduling
@SpringBootApplication
public class HtbotApplication {

	private static HttpClient httpClient;

	public HtbotApplication(HttpClient httpClient) {
		HtbotApplication.httpClient = httpClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(HtbotApplication.class, args);
		startDiscordBot();
	}

	private static void startDiscordBot() {
		try {
			HtBot bot = new HtBot(httpClient);
		} catch (LoginException e) {
			log.error("ERROR : Provided bot token is invalid!");
		}
	}
}
