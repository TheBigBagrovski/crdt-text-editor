package okp.nic;

import okp.nic.network.Controller;

import static okp.nic.Utils.findAvailablePort;
//todo() тест на 3 пирах
//todo() никнеймы у пиров
//todo() окно логов в интерфейсе + список подключенных участников
//todo() https://javaswing.wordpress.com/2009/08/04/jscrollpane_using/
//todo() сохранение и загрузка файла
//todo() улетает каретка при изменениях от другого пира
//todo() ctrl+c, ctrl+x, ctrl+v
//todo() безпоасный протокол
//todo() паттерны и принципы

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
            Controller controller = new Controller(host, port, signalHost, signalPort);
            controller.start();
            System.out.println("Пир запущен на адресе: " + host + ", порт: " + port);
        } else {
            System.out.println("Пользователь отменил ввод");
        }
    }

}
