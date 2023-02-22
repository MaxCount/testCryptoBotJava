package bot.by.khomichenko.javatestbot.config;

import bot.by.khomichenko.javatestbot.domain.Bot;
import bot.by.khomichenko.javatestbot.service.impl.BotServiceImpl;
import bot.by.khomichenko.javatestbot.service.impl.ParserImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class BotConfig extends TelegramLongPollingBot {

    final Bot bot;
    private BotServiceImpl botService;
    private ParserImpl parser;

    public BotConfig(Bot bot) {
        this.bot = bot;
//        List<BotCommand> commands = new ArrayList<>();
//        commands.add(new BotCommand("/start", "get a welcome message"));
//        commands.add(new BotCommand("/USDT-BTC", "get rate of USDT-BTC"));
//        try {
//            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(),null));
//        } catch (TelegramApiException exception) {
//            exception.printStackTrace();
//        }
    }
   @Override
    public String getBotUsername() {
        return bot.getBotName();
    }

    @Override
    public String getBotToken() {
        return bot.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (message) {
                case "/start":
                    sendMessage(chatId,
                            botService.startCommandMessage(update.getMessage().getChat().getFirstName()));
                    break;
                case "/usdtbtc":
                    sendMessage(chatId,
                            parser.getRate());
                    break;
                case "/Exchange Btc on Usdt":
                    break;
                case "/Exchange Usdt on Btc":
                    break;
                default: sendMessage(chatId, "fuck off");
            }
        }
    }

    public void sendMessage(long id, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(id));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException exception) {
            throw new RuntimeException();
        }
    }

    @Autowired
    public void setBotService(BotServiceImpl botService) {
        this.botService = botService;
    }

    @Autowired
    public void setParser(ParserImpl parser) {
        this.parser = parser;
    }
}
