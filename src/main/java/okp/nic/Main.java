package okp.nic;

import okp.nic.network.Controller;

import static okp.nic.Utils.findAvailablePort;
//todo() тест на 3 пирах
//todo() никнеймы у пиров
//todo() пароль комнаты на сигнальном сервере
//todo() объединить типы сообщений и операций
//todo() паттерны и принципы
//todo() окно логов в интерфейсе + список подключенных участников
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
        } else {
            System.out.println("Пользователь отменил ввод");
        }
    }

}
