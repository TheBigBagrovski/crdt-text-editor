package okp.nic;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;

@Slf4j
public class Utils {

    // Метод для нахождения доступного порта
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

    public static boolean isPortAvailable(int port) {
        boolean portFree;
        try (ServerSocket ignored = new ServerSocket(port)) {
            portFree = true;
        } catch (IOException e) {
            portFree = false;
        }
        return portFree;
    }

}
