package okp.nic;

import okp.nic.network.Controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // Открываем серверный сокет на случайном незанятом порту
            int port = findAvailablePort();
            ServerSocket serverSocket = new ServerSocket(port);
            InetAddress host = InetAddress.getLocalHost();

            // Выводим информацию о локальном адресе и порте
            System.out.println("Сервер запущен на адресе: " + host.getHostAddress() + ", порт: " + port);

            // Запрашиваем у пользователя адрес и порт сигнального сервера
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите адрес сигнального сервера: ");
            String signalHost = scanner.nextLine();
            System.out.print("Введите порт сигнального сервера: ");
            int signalPort = Integer.parseInt(scanner.nextLine());

            // Закрываем серверный сокет
            serverSocket.close();
            Controller controller = new Controller(host.getHostAddress(), port, signalHost, signalPort);
            controller.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Метод для нахождения доступного порта
    private static int findAvailablePort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}

/*

public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.print("Input host = ");
        String host = in.nextLine();
        System.out.print("Input port = ");
        int port = in.nextInt();


        Controller controller = new Controller(host, port);
        controller.start();
    }
try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            System.out.println("Текущий IP-адрес компьютера в локальной сети: " + ipAddress);
        } catch (UnknownHostException e) {
            System.err.println("Не удалось определить IP-адрес: " + e.getMessage());
        }
 */