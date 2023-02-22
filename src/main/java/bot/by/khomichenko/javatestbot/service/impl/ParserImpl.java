package bot.by.khomichenko.javatestbot.service.impl;

import bot.by.khomichenko.javatestbot.service.Parser;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class ParserImpl implements Parser {

    private static final String url =  "https://api.wavesplatform.com/v0/pairs?search_by_assets=btc%2C%20usdt";

    @Override
    public String getRate() {
        String json = null;
        try {
            json = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
        JSONObject jsonObject2 = jsonObject1.getJSONObject("data");

        return jsonObject2.get("lastPrice").toString();
    }
}
