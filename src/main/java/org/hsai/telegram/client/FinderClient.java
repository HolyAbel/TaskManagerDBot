package org.hsai.telegram.client;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import org.hsai.telegram.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FinderClient {

    @Autowired
    private OkHttpClient client;

    @Value("${server.hostname}")
    private String hostname;

    public String getRequest(String url) throws ServiceException {
        var request = new Request.Builder()
                .url(url)
                .build();
        try {
            var response = client.newCall(request).execute();
            var body = response.body();
            return body == null ? null: body.string();
        } catch (IOException e) {
            throw new ServiceException("Ошибка, неизвестный запрос", e);
        }

    }
}
