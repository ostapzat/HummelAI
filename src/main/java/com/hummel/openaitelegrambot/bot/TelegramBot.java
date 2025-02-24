package com.hummel.openaitelegrambot.bot;

import com.hummel.openaitelegrambot.service.TextToImageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final TextToImageGenerator textToImageGenerator;

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    public TelegramBot(TextToImageGenerator textToImageGenerator) {
        this.textToImageGenerator = textToImageGenerator;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String messageText = update.getMessage().getText();

            if (messageText.startsWith("/start")) {
                sendMessage(chatId, "Hello, I am a HummelAI, you can generate images with /generate");
            }

            if (messageText.startsWith("/generate")) {
                String prompt = messageText.replace("/generate", "").trim();

                if (prompt.isEmpty()) {
                    sendMessage(chatId, "Please provide a prompt after the /generate command.");
                } else {
                    sendMessage(chatId, "Generating image...");
                    byte[] imageBytes = textToImageGenerator.generateImage(prompt);
                    if (imageBytes != null) {
                        sendPhoto(chatId, imageBytes);
                    } else {
                        sendMessage(chatId, "Failed to generate image.");
                    }
                }
            }
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPhoto(String chatId, byte[] imageBytes) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);

        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        InputFile inputFile = new InputFile(inputStream, "image.png");
        sendPhoto.setPhoto(inputFile);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            sendMessage("Cannot generate photo, share it with a developer", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
