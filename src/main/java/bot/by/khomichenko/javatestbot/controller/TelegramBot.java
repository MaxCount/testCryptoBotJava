package bot.by.khomichenko.javatestbot.controller;

import bot.by.khomichenko.javatestbot.model.Bot;
import bot.by.khomichenko.javatestbot.service.impl.BotServiceImpl;
import bot.by.khomichenko.javatestbot.service.impl.ParserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final Bot bot;
    private BotServiceImpl botService;
    private ParserImpl parser;
    private static final String YES_BUTTON = "YES_BUTTON";
    private static final String NO_BUTTON = "NO_BUTTON";
    private static final String RETURN_BUTTON = "RETURN_BUTTON";
    private static final String BTC_ON_USDT = "BTC_ON_USDT";
    private static final String USDT_ON_BTC = "USDT_ON_BTC";
    private static final String WELCOME_COMMAND = "WELCOME_COMMAND";
    private static final String EXHANGE_COMMAND = "EXHANGE_COMMAND";
    private static final String BTC_RATE = "/usdtbtc";
    private static final String EXCHANGE_USDT = "/exchangebtconusdt";
    private static final String EXCHANGE_BTC= "/exchangeusdtonbtc";
    private String exhange;

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
            int value;

            if (Objects.equals(exhange, BTC_ON_USDT)) {
                try {
                    value = Integer.parseInt(message);
                    sendMessage(chatId,botService.getUsdtBtc(value, parser.getRate()).toString());
                    exchange(chatId);
                } catch (Exception e) {
                    sendMessage(chatId,"enter a valid number");
                }
                exhange = null;
            }
            else if (Objects.equals(exhange, USDT_ON_BTC)) {
                try {
                    value = Integer.parseInt(message);
                    sendMessage(chatId,botService.getBtcUsdt(value, parser.getRate()).toString());
                    exchange(chatId);
                } catch (Exception e) {
                    sendMessage(chatId,"enter a valid number");
                }
                exhange = null;
            }

            switch (message) {
                case "/start" -> {
                    this.exhange = WELCOME_COMMAND;
                    sendMessage(chatId,
                            botService.startCommandMessage(update.getMessage().getChat().getFirstName()));
                }
                case "/usdtbtc" -> {
                    this.exhange = EXHANGE_COMMAND;
                    sendMessage(chatId,
                            "1 BTC = " + parser.getRate() + " USDT");
                }
                case "/exchangebtconusdt" -> {
                    this.exhange = BTC_ON_USDT;
                    backButton(chatId, "BTC");
                }
                case "/exchangeusdtonbtc" -> {
                    this.exhange = USDT_ON_BTC;
                    backButton(chatId, "USDT");
                }
                default -> {
                    if (exhange != null) {
                        sendMessage(update.getMessage().getChatId(), "enter another command");
                    }
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callbackData) {
                case YES_BUTTON -> sendMessage(chatId, "request send");
                case NO_BUTTON -> sendMessage(chatId, "request don't send");
                case RETURN_BUTTON -> {
                    exhange = null;
                    sendMessage(chatId, "ok");
                }
            }
        }
    }

    private void exchange(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Do you really want to exchange?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();

        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();

        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
        try {
            execute(message);
        } catch (TelegramApiException exception) {
            throw new RuntimeException();
        }
    }

    public void sendMessage(long id, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(id));
        message.setText(textToSend);

        addButtons(message);
        try {
            execute(message);
        } catch (TelegramApiException exception) {
            throw new RuntimeException();
        }
    }

    private void addButtons(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(EXCHANGE_USDT);
        row.add(BTC_RATE);
        row.add(EXCHANGE_BTC);
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);
    }

    private void backButton(long chatId, String token) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("how many " + token + " you give?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var returnButton = new InlineKeyboardButton();

        returnButton.setText("<- return");
        returnButton.setCallbackData(RETURN_BUTTON);

        rowInLine.add(returnButton);
        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
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

    public TelegramBot(Bot bot) {
        this.bot = bot;
    }
}
