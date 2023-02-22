package bot.by.khomichenko.javatestbot.service.impl;

import bot.by.khomichenko.javatestbot.service.BotService;
import org.springframework.stereotype.Service;

@Service
public class BotServiceImpl implements BotService {

    @Override
    public String startCommandMessage(String name) {
        return  "Hi, " + name + " here you can see rate of BTC/USTD and USDT/BTC";
    }

    @Override
    public String getUsdtBtcRate() {
        return null;
    }



}