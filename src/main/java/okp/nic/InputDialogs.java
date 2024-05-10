package okp.nic;

import javax.swing.*;
import java.awt.*;

import static okp.nic.Utils.getUtfString;

public class InputDialogs {

    private static final Font font = new Font("Arial", Font.BOLD, 16);
    private static final Dimension textFieldSize = new Dimension(200, 25);

    public static String[] getSignalServerAddress() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JPasswordField passwordField = new JPasswordField(18);
        passwordField.setPreferredSize(textFieldSize);
        passwordField.setFont(font);
        addLabel(panel, "Адрес сигнального сервера:");
        JTextField hostField = addTextField(panel);
        addLabel(panel, "Пароль:");
        panel.add(passwordField);
        int result = JOptionPane.showConfirmDialog(null, panel, getUtfString("Сигнальный сервер"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return new String[]{hostField.getText(), new String(passwordField.getPassword())};
        } else {
            return null;
        }
    }

    public static String getSignalServerPort() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        addLabel(panel, "Порт сигнального сервера:");
        JTextField portField = addTextField(panel);
        int result = JOptionPane.showConfirmDialog(null, panel, getUtfString("Сигнальный сервер"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return portField.getText();
        } else {
            return null;
        }
    }

    public static String[] getPeerInfo() {
        JPanel panel = new JPanel(new GridLayout(5, 2));
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(textFieldSize);
        passwordField.setFont(font);
        addLabel(panel, "Адрес пира:");
        JTextField peerAddressField = addTextField(panel);
        addLabel(panel, "Адрес сигнального сервера:");
        JTextField signalHostField = addTextField(panel);
        addLabel(panel, "Порт сигнального сервера:");
        JTextField signalPortField = addTextField(panel);
        addLabel(panel, "Ваше имя:");
        JTextField nameField = addTextField(panel);
        addLabel(panel, "Пароль:");
        panel.add(passwordField);
        int result = JOptionPane.showConfirmDialog(null, panel, getUtfString("Подключение"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return new String[]{peerAddressField.getText(), signalHostField.getText(), signalPortField.getText(), nameField.getText(), new String(passwordField.getPassword())};
        } else {
            return null;
        }
    }

    private static JTextField addTextField(JPanel panel) {
        JTextField field = new JTextField(20);
        field.setPreferredSize(textFieldSize);
        field.setFont(font);
        panel.add(field);
        return field;
    }

    private static void addLabel(JPanel panel, String labelText) {
        JLabel label = new JLabel(getUtfString(labelText));
        label.setFont(font);
        panel.add(label);
    }

}
