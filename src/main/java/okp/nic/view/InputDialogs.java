package okp.nic.view;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Objects;

import static okp.nic.utils.Utils.getUtfString;

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
        while (true) {
            int result = JOptionPane.showConfirmDialog(null, panel, getUtfString("Сигнальный сервер"), JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return null;
            }
            checkFilled(hostField);
            if (!hostField.getText().isEmpty()) {
                return new String[]{hostField.getText(), new String(passwordField.getPassword())};
            }
        }
    }

    public static String getSignalServerPort() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        addLabel(panel, "Порт сигнального сервера:");
        JTextField portField = addTextField(panel);
        while (true) {
            int result = JOptionPane.showConfirmDialog(null, panel, getUtfString("Сигнальный сервер"), JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return null;
            }
            checkFilled(portField);
            if (!portField.getText().isEmpty()) {
                return portField.getText();
            }
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
        while (true) {
            int result = JOptionPane.showConfirmDialog(null, panel, getUtfString("Подключение"), JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return null;
            }
            checkFilled(peerAddressField);
            checkFilled(signalHostField);
            checkFilled(signalPortField);
            checkFilled(nameField);
            if (peerAddressField.getText().isEmpty() || signalHostField.getText().isEmpty() || signalPortField.getText().isEmpty() || nameField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, getUtfString("Заполнены не все нужные поля"), getUtfString("Ошибка"), JOptionPane.ERROR_MESSAGE);
            } else {
                return new String[]{peerAddressField.getText(), signalHostField.getText(), signalPortField.getText(), nameField.getText(), new String(passwordField.getPassword())};
            }
        }
    }

    public static void showInfoWindow(String socketAddress) {
        JFrame frame = new JFrame(getUtfString("Адрес сигнального сервера"));
        JLabel socketLabel = new JLabel(getUtfString("Сигнальный сервер запущен на сокете: ") + socketAddress);
        socketLabel.setFont(new Font("Arial", Font.BOLD, 18));
        socketLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel panel = new JPanel();
        panel.add(socketLabel);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(InputDialogs.class.getResource("/img/logo.png")));
        frame.setIconImage(logo.getImage());
        frame.add(panel);
        frame.setSize(600, 100);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void checkFilled(JTextField field) {
        if (field.getText().isEmpty()) {
            field.setBackground(Color.PINK);
        } else {
            field.setBackground(Color.WHITE);
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
