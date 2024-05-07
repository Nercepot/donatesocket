package org.nercepot;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {
    static final String DASERVER = "https://socket.donationalerts.ru:443";
    static Socket socket;

    public static void main(String[] args) {
        try {
            URI url = new URI(DASERVER);
            socket = IO.socket(url);

            // Подключаемся к серверу
            socket.connect();

            // Подписываемся на события подключения и донатов
            socket.on(Socket.EVENT_CONNECT, connectListener)
                    .on("donation", donationListener)
                    .on("disconnect", disconnectListener);

            // Бесконечный цикл для поддержания соединения
            while (true) {
                try {
                    Thread.sleep(1000); // Пауза перед следующей проверкой
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (socket!= null) {
                socket.disconnect();
            }
        }
    }

    private static final Emitter.Listener connectListener = arg0 -> {
        try {
            connect();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    };

    private static final Emitter.Listener donationListener = arg0 -> {
        try {
            processDonation(arg0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    };

    private static final Emitter.Listener disconnectListener = arg0 -> {
        // Обработка отключения от сервера
        System.out.println("Disconnected from server. Attempting to reconnect...");
        // Попытка переподключения
        socket.connect();
    };

    private static void connect() throws JSONException {
        socket.emit("add-user", new JSONObject()
                .put("token", "") //token
                .put("type", "alert_widget"));
    }

    private static void processDonation(Object... arg0) throws JSONException {
        JSONObject json = new JSONObject((String) arg0[0]);

        Integer donationAmount = json.getInt("amount");
        String donationUsername = json.getString("username");

        // Здесь может быть логика обработки доната
        System.out.println("Donation received from: " + donationUsername);
        System.out.println("Amount: " + donationAmount);
    }
}
