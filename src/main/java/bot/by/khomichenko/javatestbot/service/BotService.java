package bot.by.khomichenko.javatestbot.service;

public interface BotService {

    String startCommandMessage(String name);

    Double getUsdtBtc(Integer amount, String rate);

    Double getBtcUsdt(Integer amount, String rate);
}
