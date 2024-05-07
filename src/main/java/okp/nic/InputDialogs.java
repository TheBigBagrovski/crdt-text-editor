package okp.nic;

import javax.swing.*;
import java.awt.*;

import static okp.nic.Utils.getString;

public class InputDialogs {

    private static final Font font = new Font("Arial", Font.BOLD, 16);
    private static final Dimension textFieldSize = new Dimension(200, 25);

    public static String getSignalServerAddress() {
        JTextField hostField = new JTextField(18);
        hostField.setPreferredSize(textFieldSize);
        hostField.setFont(font);
        JPanel panel = new JPanel(new GridLayout(1, 2));
        JLabel hostLabel = new JLabel(getString("Адрес сигнального сервера:"));
        hostLabel.setFont(font);
        panel.add(hostLabel);
        panel.add(hostField);
        int result = JOptionPane.showConfirmDialog(null, panel, getString("Сигнальный сервер"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return hostField.getText();
        } else {
            return null;
        }
    }

    public static String getSignalServerPort() {
        JTextField portField = new JTextField(10);
        portField.setPreferredSize(textFieldSize);
        portField.setFont(font);
        JPanel panel = new JPanel(new GridLayout(1, 2));
        JLabel portLabel = new JLabel(getString("Порт сигнального сервера:"));
        portLabel.setFont(font);
        panel.add(portLabel);
        panel.add(portField);
        int result = JOptionPane.showConfirmDialog(null, panel, getString("Сигнальный сервер"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return portField.getText();
        } else {
            return null;
        }
    }

    public static String[] getPeerInfo() {
        JTextField peerAddressField = new JTextField(20);
        peerAddressField.setPreferredSize(textFieldSize);
        peerAddressField.setFont(font);
        JTextField signalHostField = new JTextField(20);
        signalHostField.setPreferredSize(textFieldSize);
        signalHostField.setFont(font);
        JTextField signalPortField = new JTextField(5);
        signalPortField.setPreferredSize(textFieldSize);
        signalPortField.setFont(font);
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JLabel peerLabel = new JLabel(getString("Адрес пира:"));
        peerLabel.setFont(font);
        JLabel signalHostLabel = new JLabel(getString("Адрес сигнального сервера:"));
        signalHostLabel.setFont(font);
        JLabel signalPortLabel = new JLabel(getString("Порт сигнального сервера:"));
        signalPortLabel.setFont(font);
        panel.add(peerLabel);
        panel.add(peerAddressField);
        panel.add(signalHostLabel);
        panel.add(signalHostField);
        panel.add(signalPortLabel);
        panel.add(signalPortField);
        int result = JOptionPane.showConfirmDialog(null, panel, getString("Подключение"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return new String[]{peerAddressField.getText(), signalHostField.getText(), signalPortField.getText()};
        } else {
            return null;
        }
    }
}
