package okp.nic;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Utils {

    public static final String SALT = "somesaltsomesalt";

    // метод инкапсуляции текста в UTF-8 для Swing
    public static String getUtfString(String str) {
        return new String(str.getBytes(), StandardCharsets.UTF_8);
    }

    // метод для нахождения доступного порта
    public static int findAvailablePort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (Exception e) {
            System.out.println("Не удалось выбрать свободный порт");
            return -1;
        }
    }

    // метод определения доступности порта
    public static boolean isPortAvailable(int port) {
        boolean portFree;
        try (ServerSocket ignored = new ServerSocket(port)) {
            portFree = true;
        } catch (Exception e) {
            portFree = false;
        }
        return port > 1024 && portFree;
    }

}
