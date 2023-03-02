package bot.by.khomichenko.javatestbot.controller;

import bot.by.khomichenko.javatestbot.model.Bot;
import bot.by.khomichenko.javatestbot.service.impl.BotServiceImpl;
import bot.by.khomichenko.javatestbot.service.impl.ParserImpl;
import com.wavesplatform.wavesj.Account;
import com.wavesplatform.wavesj.Node;
import com.wavesplatform.wavesj.PrivateKeyAccount;
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

import java.io.IOException;
import java.net.URISyntaxException;
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
    private static final String EXCHANGE_COMMAND = "EXHANGE_COMMAND";
    private static final String BTC_RATE = "/usdtbtc";
    private static final String EXCHANGE_USDT = "/exchangebtconusdt";
    private static final String EXCHANGE_BTC = "/exchangeusdtonbtc";
    private static final String USER_SENDED_STATUS_TRUE = "USER_SENDED_STATUS_TRUE";
    private String exhange;
    private double usdt;
    private double btc;
    private boolean tokenBtc;

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
                    usdt = botService.getUsdtBtc(value, parser.getRate());
                    sendMessage(chatId, "You will have: " + usdt + " USDT");
                    tokenBtc = false;
                    exchange(chatId);
                    exhange = null;
                } catch (Exception e) {
                    enterValidNumber(chatId);
                    exhange = BTC_ON_USDT;
                }
            }
            else if (Objects.equals(exhange, USDT_ON_BTC)) {
                try {
                    value = Integer.parseInt(message);
                    btc = botService.getBtcUsdt(value, parser.getRate());
                    sendMessage(chatId, "You will have: " + btc + " BTC");
                    tokenBtc = true;
                    exchange(chatId);
                    exhange = null;
                } catch (Exception e) {
                    enterValidNumber(chatId);
                    exhange = USDT_ON_BTC;
                }
            }

            switch (message) {
                case "/start" -> {
                    exhange = WELCOME_COMMAND;
                    sendMessage(chatId,
                            botService.startCommandMessage(update.getMessage().getChat().getFirstName()));
                }
                case "/usdtbtc" -> {
                    exhange = EXCHANGE_COMMAND;
                    sendMessage(chatId,
                            "1 BTC = " + parser.getRate() + " USDT");
                }
                case "/exchangebtconusdt" -> {
                    exhange = BTC_ON_USDT;
                    backButton(chatId, "BTC");
                }
                case "/exchangeusdtonbtc" -> {
                    exhange = USDT_ON_BTC;
                    backButton(chatId, "USDT");
                }
                default -> {
                    if (Objects.equals(exhange, BTC_ON_USDT) && Objects.equals(exhange, USDT_ON_BTC)) {
                        sendMessage(update.getMessage().getChatId(), "enter another command");
                    }
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            try {
                callbackButtons(update);
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void callbackButtons(Update update) throws URISyntaxException, IOException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        switch (callbackData) {
            case YES_BUTTON -> {
                sendMessage(chatId, "Send the desired number of tokens to this account: " + getAddress());
                waitingForUser(chatId);
                exhange = null;
            }
            case NO_BUTTON -> {
                sendMessage(chatId, "Request don't send");
                exhange = null;
            }
            case RETURN_BUTTON -> {
                exhange = null;
                sendMessage(chatId, "ok");
            }
            case USER_SENDED_STATUS_TRUE -> {
                exhange = null;
                System.out.println("In my wallet " + getBalance());
                sendMessage(chatId, "Check if the tokens have arrived...");
                sendMessage(chatId, "Getting your address from transaction...");
                // transaction to user
                if (tokenBtc) {
                    sendMessage(chatId, "Sending " + btc + " BTC to you...");
                } else {
                    sendMessage(chatId, "Sending " + usdt + " USDT to you...");
                }
                sendMessage(chatId, "Success!");
            }
        }
    }

    private void exchange(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Do you really want to exchange?");

        setupButtons(message, YES_BUTTON, NO_BUTTON);
    }

    private void waitingForUser(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Have you already send money?");

        setupButtons(message, USER_SENDED_STATUS_TRUE, RETURN_BUTTON);
    }

    private void setupButtons(SendMessage message, String userSendedStatus, String returnButton) {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(userSendedStatus);

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(returnButton);

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

    private void sendMessage(long id, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(id));
        message.setText(textToSend);

        addMainButtons(message);
        try {
            execute(message);
        } catch (TelegramApiException exception) {
            throw new RuntimeException();
        }
    }

    private void addMainButtons(SendMessage message) {
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

    private long getBalance() throws URISyntaxException, IOException {
        String address = getAddress();
        Node node = new Node("https://testnode2.wavesnodes.com");
        return node.getBalance(address) ;
    }

    private String getAddress() {
        PrivateKeyAccount account = PrivateKeyAccount.fromSeed("9Lb87Hqv9xJeDEi_", 0, Account.TESTNET);
        return account.getAddress();
    }

    private void enterValidNumber(long chatId) {
        sendMessage(chatId,"enter a valid number");
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
