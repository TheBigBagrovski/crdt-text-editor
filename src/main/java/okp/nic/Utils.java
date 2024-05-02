package okp.nic;

import java.net.ServerSocket;

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

}
