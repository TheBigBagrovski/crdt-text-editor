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
//todo() тест на 3 пирах
//todo() показ имещегося текста при подключении
//todo() никнеймы у пиров
//todo() окно логов в интерфейсе + список подключенных участников
//todo() сохранение и загрузка файла
//todo() улетает каретка при изменениях от другого пира
//todo() ctrl+c, ctrl+x, ctrl+v
//todo() безпоасный протокол


//todo() тесты
//todo() рефакторинг
//todo() финальный смотр

public class Main {
    public static void main(String[] args) {
        String[] peerInfo = InputDialogs.getPeerInfo();
        if (peerInfo != null) {
            String host = peerInfo[0];
            String signalHost = peerInfo[1];
            String signalPort = peerInfo[2];
            int port = findAvailablePort();
            System.out.println("Пир запущен на адресе: " + host + ", порт: " + port);
            Controller controller = new Controller(host, port, signalHost, signalPort);
            controller.start();
        } else {
            System.out.println("User cancelled input.");
        }
    }

}
