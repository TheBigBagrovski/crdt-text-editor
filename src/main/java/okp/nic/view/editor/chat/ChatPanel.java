package okp.nic.view.editor.chat;

import lombok.Getter;
import okp.nic.presenter.Presenter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static okp.nic.utils.Utils.getUtfString;
import static okp.nic.view.editor.texteditor.TextEditorView.CHAT_PANEL_WIDTH_RATIO;
import static okp.nic.view.editor.texteditor.TextEditorView.MAX_CHAT_MESSAGE_LENGTH;

public class ChatPanel extends JPanel {

    @Getter
    private final JTextArea chatArea = new JTextArea();
    private final JTextArea chatInput = new JTextArea();

    public ChatPanel(BorderLayout bl, Presenter presenter, int screenWidth) {
        super(bl);
        setPreferredSize(new Dimension((int) (screenWidth * CHAT_PANEL_WIDTH_RATIO), 0));
        // настройка поля сообщений
        JScrollPane chatScrollPane = new JScrollPane(chatArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatArea.setEditable(false);
        chatArea.setFont(chatArea.getFont().deriveFont(16f));
        // настройка поля ввода
        chatInput.setPreferredSize(new Dimension((int) (screenWidth * CHAT_PANEL_WIDTH_RATIO), 100));
        chatInput.setLineWrap(true);
        chatInput.setWrapStyleWord(true);
        chatInput.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        chatInput.setFont(chatInput.getFont().deriveFont(14f));
        add(chatScrollPane, BorderLayout.CENTER);
        add(chatInput, BorderLayout.SOUTH);
        // отправка сообщения по нажатию Enter
        chatInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    String message = chatInput.getText().trim();
                    if (message.length() > MAX_CHAT_MESSAGE_LENGTH) {
                        chatInput.setBackground(Color.PINK);
                    } else if (!message.isEmpty()) {
                        presenter.sendChatMessage(message);
                        chatInput.setText("");
                        chatInput.setBackground(Color.WHITE);
                    }
                }
            }
        });
        // Заголовок "ЧАТ"
        JLabel chatLabel = new JLabel(getUtfString("ЧАТ"));
        chatLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        chatLabel.setFont(chatLabel.getFont().deriveFont(Font.BOLD, 16));
        chatLabel.setOpaque(true);
        chatLabel.setBackground(Color.LIGHT_GRAY);
        chatLabel.setHorizontalAlignment(JLabel.CENTER);
        add(chatLabel, BorderLayout.NORTH);
        // плейсхолдер и обработка фокуса
        chatInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (chatInput.getText().equals(getUtfString("Введите сообщение..."))) {
                    chatInput.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (chatInput.getText().isEmpty()) {
                    chatInput.setText(getUtfString("Введите сообщение..."));
                }
            }
        });
    }

}
