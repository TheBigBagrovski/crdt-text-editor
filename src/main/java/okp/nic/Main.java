package okp.nic;

import okp.nic.network.Controller;
import okp.nic.network.Messenger;

import static okp.nic.Utils.findAvailablePort;
//todo() никнеймы у пиров
//todo() окно логов в интерфейсе + список подключенных участников
//todo() ctrl+c, ctrl+x, ctrl+v
//todo() чат
//todo() паттерны и принципы
//todo() тест на 3 пирах
//todo() безопасность пароля? вынести соль?

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
            String name = peerInfo[3];
            String password = peerInfo[4];
            int port = findAvailablePort();
            Controller controller = new Controller(host, port);
            Messenger messenger = new Messenger(host, port, controller, signalHost, signalPort, password, name);
            System.out.println("Пир запущен на адресе: " + host + ", порт: " + port);
        } else {
            System.out.println("Пользователь отменил ввод");
        }
    }

}
