package io.github.Nyg404;


import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;



public class Main {
    private static Bot botInstance;

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        try{
            String token = dotenv.get("TOKEN");
            TelegramBotsLongPollingApplication botsLongPollingApplication = new TelegramBotsLongPollingApplication();
            botInstance = new Bot(token);
            botsLongPollingApplication.registerBot(token, botInstance);
        } catch (TelegramApiException e){
            e.getMessage();
        }
    }

    public static Bot getBot(){
        return botInstance;
    }

}