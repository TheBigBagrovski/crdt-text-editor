package okp.nic;

import okp.nic.network.Controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static okp.nic.Utils.findAvailablePort;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите адрес пира: ");
        String host = scanner.nextLine();
        int port = findAvailablePort();
        System.out.println("Пир запущен на адресе: " + host + ", порт: " + port);
        System.out.print("Введите адрес сигнального сервера: ");
        String signalHost = scanner.nextLine();
        System.out.print("Введите порт сигнального сервера: ");
        int signalPort = Integer.parseInt(scanner.nextLine());
        Controller controller = new Controller(host, port, signalHost, signalPort);
        controller.start();
    }

}
