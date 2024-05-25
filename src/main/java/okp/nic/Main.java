package okp.nic;

import okp.nic.network.messenger.Messenger;
import okp.nic.view.InputDialogs;

import static okp.nic.utils.PortChecker.findAvailablePort;

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
            Messenger messenger = new Messenger(host, port, signalHost, signalPort, password, name);
            System.out.println("Пир запущен на адресе: " + host + ", порт: " + port);
        } else {
            System.out.println("Пользователь отменил ввод");
        }
    }

}
