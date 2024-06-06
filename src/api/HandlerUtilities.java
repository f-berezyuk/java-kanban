package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import api.adapters.DurationAdapter;
import api.adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

public class HandlerUtilities {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static void writeEndpoint404Response(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Такого эндпоинта не существует", 404);
    }

    public static void writeTask404Response(HttpExchange exchange, Long id) throws IOException {
        writeResponse(exchange, "Таск с идентификатором " + id + " не найден.", 404);
    }

    public static void write400Response(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "В запросе содержится ошибка. Проверьте параметры и повторите запрос.", 400);
    }

    public static void writeResponse(HttpExchange exchange,
                                     String responseString,
                                     int responseCode) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(responseCode, 0);
            os.write(responseString.getBytes(DEFAULT_CHARSET));
        }
        exchange.close();
    }

    public static String readRequestBodyAsString(HttpExchange exchange) throws IOException {
        String input;
        try (InputStream is = exchange.getRequestBody(); InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }

            input = sb.toString();
        }
        return input;
    }

    public static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public static void write500(HttpExchange exchange, IOException e) throws IOException {
        writeResponse(exchange, e.getMessage(), 500);
    }
}
