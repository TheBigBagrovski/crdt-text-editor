package okp.nic.utils;

import java.net.ServerSocket;

import static okp.nic.utils.Utils.getUtfString;

public class PortChecker {

    // метод для нахождения доступного порта
    public static int findAvailablePort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (Exception e) {
            System.out.println(getUtfString("Не удалось выбрать свободный порт"));
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
