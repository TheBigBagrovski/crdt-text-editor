package okp.nic.gui.editor;

import lombok.Getter;
import okp.nic.logger.Logger;
import okp.nic.network.Controller;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static okp.nic.Utils.getUtfString;

public class TextEditor extends JFrame implements CaretListener, DocumentListener, KeyListener, ComponentListener {

    //    private static final int FRAME_WIDTH = 1600;
//    private static final int FRAME_HEIGHT = 700;
//    private static final Dimension FRAME_SIZE = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
//    private static final int PEERS_WIDTH = 250;
//    private static final int PEERS_HEIGHT = 300;
//    private static final Dimension PEERS_SIZE = new Dimension(PEERS_WIDTH, PEERS_HEIGHT);
//    private static final int LOG_WIDTH = 250;
//    private static final int LOG_HEIGHT = 400;
//    private static final Dimension LOG_SIZE = new Dimension(LOG_WIDTH, LOG_HEIGHT);
//    private static final int RIGHT_PANEL_WIDTH = 250;
//    private static final int RIGHT_PANEL_HEIGHT = 700;
//    private static final Dimension RIGHT_PANEL_SIZE = new Dimension(RIGHT_PANEL_WIDTH, RIGHT_PANEL_HEIGHT);
//    private static final int CHAT_WIDTH = 250;
//    private static final int CHAT_HEIGHT = 700;
//    private static final Dimension CHAT_SIZE = new Dimension(CHAT_WIDTH, CHAT_HEIGHT);
    private static final double RIGHT_PANEL_WIDTH_RATIO = 0.2; // 20% от ширины экрана
    private static final double CHAT_PANEL_WIDTH_RATIO = 0.2; // 20% от ширины экрана
//    private static final double MAX_FRAME_SIZE_RATIO = 0.9; // 90% от размеров экрана
    private static final int MAX_CHAT_MESSAGE_LENGTH = 250;
    private final int screenWidth;

    private final Controller controller;
    private final Logger logger;

    @Getter
    private final JTextArea textArea = new JTextArea();
    private JPanel lineNumberPanel;

    private final JPanel rightPanel = new JPanel(new BorderLayout());
    private final JPanel peersListPanel = new JPanel();
    private final JPanel peersPanel = new JPanel();
    private final List<JLabel> peersList = new ArrayList<>();
    private final JTextArea logArea = new JTextArea();

    private final JPanel chatPanel = new JPanel(new BorderLayout());
    private final JTextArea chatArea = new JTextArea();
    private final JTextArea chatInput = new JTextArea();

    private JDialog importDialog;

    // cut-copy-paste
    private String selectedText;
    private String copiedText;
    private int selectStartPos;
    private int selectEndPos;


    public TextEditor(Controller controller, Logger logger) {
        this.controller = controller;
        this.logger = logger;
        // настройки фрейма
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        JFrame frame = setupFrame();
        // настройки основной панели
        JPanel mainPanel = setupMainPanel();
        // настройки правой панели
        JPanel rightPanel = setupRightPanel();
        // настройки меню
        JMenuBar menuBar = setupMenuBar();
        frame.setJMenuBar(menuBar);
        // настройки сочетаний клавиш (копировать, вырезать, вставить)
        setupKeyStrokeActions();
        // настройки чата

//        chatPanel.setPreferredSize(CHAT_SIZE);
        chatPanel.setPreferredSize(new Dimension((int) (screenWidth * CHAT_PANEL_WIDTH_RATIO), 0));

        JScrollPane chatScrollPane = new JScrollPane(chatArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatArea.setEditable(false);
        chatArea.setFont(chatArea.getFont().deriveFont(16f));
//        chatInput.setPreferredSize(new Dimension(CHAT_WIDTH, 100));
        chatInput.setPreferredSize(new Dimension((int) (screenWidth * CHAT_PANEL_WIDTH_RATIO), 100));

        chatInput.setLineWrap(true); // Включаем перенос по словам
        chatInput.setWrapStyleWord(true);
        chatInput.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Толстая рамка
        chatInput.setFont(chatInput.getFont().deriveFont(14f));
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);
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
                        sendChatMessage(message);
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
        chatPanel.add(chatLabel, BorderLayout.NORTH);
        // Placeholder и обработка фокуса
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
        // финальные настройки
        mainPanel.add(chatPanel, BorderLayout.EAST);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.addComponentListener(this);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Фрейм на весь экран
        frame.pack();
        addKeyListener(this);
        frame.setVisible(true);
    }

    private JFrame setupFrame() {
        JFrame frame = new JFrame("CRDT");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/logo.png")));
        frame.setIconImage(logo.getImage());
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        int maxWidth = (int) (screenSize.width * MAX_FRAME_SIZE_RATIO);
//        int maxHeight = (int) (screenSize.height * MAX_FRAME_SIZE_RATIO);
//        frame.setMaximumSize(new Dimension(maxWidth, maxHeight));
//        frame.setMinimumSize(FRAME_SIZE);
        return frame;
    }

    private JPanel setupMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setVisible(true);
        // настройки текстового поля
        textArea.setFont(new Font("Courier New", Font.PLAIN, 18));
        textArea.addCaretListener(this);
        textArea.addKeyListener(this);
        textArea.getDocument().addDocumentListener(this);
        // нумерация строк
        lineNumberPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(70, textArea.getPreferredSize().height);
            }
        };
        lineNumberPanel.setBackground(Color.LIGHT_GRAY);
        lineNumberPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
        // настройки панели с текстом
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(lineNumberPanel, BorderLayout.WEST);
        textPanel.add(textArea, BorderLayout.CENTER);
        // настройка скроллинга
        JScrollPane scrollPane = new JScrollPane(textPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setUnitIncrement(20);
//        scrollPane.setRowHeaderView(lineNumberPanel);
        // добавляем все поля (через scrollPane) в главную панель
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel setupRightPanel() {
//        rightPanel.setPreferredSize(RIGHT_PANEL_SIZE);
        rightPanel.setPreferredSize(new Dimension((int) (screenWidth * RIGHT_PANEL_WIDTH_RATIO), 0));

        // настройки панели пиров
//        peersPanel.setPreferredSize(PEERS_SIZE);
        peersPanel.setPreferredSize(new Dimension((int) (screenWidth * RIGHT_PANEL_WIDTH_RATIO), 300));

        peersPanel.setLayout(new BorderLayout());
        // Заголовок "PEERS"
        JLabel peersLabel = new JLabel(getUtfString("PEERS"));
        peersLabel.setFont(peersLabel.getFont().deriveFont(Font.BOLD, 16));
        peersLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        peersLabel.setOpaque(true);
        peersLabel.setBackground(Color.LIGHT_GRAY);
        peersLabel.setHorizontalAlignment(JLabel.CENTER);

        // Панель для списка пиров
        peersListPanel.setLayout(new BoxLayout(peersListPanel, BoxLayout.Y_AXIS));
        peersPanel.add(peersListPanel, BorderLayout.CENTER);

        JScrollPane peersScrollPane = new JScrollPane(peersPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        peersPanel.add(peersLabel, BorderLayout.NORTH);


        // панель логов
        JPanel logPanel = new JPanel();
//        logPanel.setSize(LOG_SIZE);
        logPanel.setLayout(new BorderLayout());
        logArea.setEditable(false);
        // Заголовок "ЛОГ"
        JLabel logLabel = new JLabel(getUtfString("ЖУРНАЛ ЛОГОВ"));
        logLabel.setFont(logLabel.getFont().deriveFont(Font.BOLD, 16));
        logLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        logLabel.setOpaque(true);
        logLabel.setBackground(Color.LIGHT_GRAY);
        logLabel.setHorizontalAlignment(JLabel.CENTER);
        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(logArea, BorderLayout.CENTER);
        JScrollPane logScrollPane = new JScrollPane(logPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        rightPanel.add(peersScrollPane, BorderLayout.NORTH);
        rightPanel.add(logScrollPane, BorderLayout.CENTER);
        return rightPanel;
    }

    private JMenuBar setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(getUtfString("Файл"));
        menuBar.add(fileMenu);
        JMenuItem saveMenuItem = new JMenuItem(getUtfString("Сохранить"));
        JMenuItem loadMenuItem = new JMenuItem(getUtfString("Загрузить"));
        fileMenu.add(saveMenuItem);
        fileMenu.add(loadMenuItem);
        saveMenuItem.addActionListener(e -> saveFile());
        loadMenuItem.addActionListener(e -> loadFile());
        return menuBar;
    }

    private void setupKeyStrokeActions() {
        InputMap inputMap = textArea.getInputMap();
        ActionMap actionMap = textArea.getActionMap();

        KeyStroke copyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(copyKeyStroke, "copy");
        actionMap.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiedText = selectedText;
            }
        });

        KeyStroke cutKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(cutKeyStroke, "cut");
        actionMap.put("cut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiedText = selectedText;
                controller.onLocalDeleteRange(selectStartPos, selectEndPos);
            }
        });

        KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(pasteKeyStroke, "paste");
        actionMap.put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (copiedText != null && !copiedText.isEmpty() && selectStartPos == selectEndPos) {
                    controller.onLocalInsertBlock(textArea.getCaretPosition(), copiedText);
                }
            }
        });

        KeyStroke deleteWordKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(deleteWordKeyStroke, "deleteWord");
        actionMap.put("deleteWord", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private void updateLineNumbers(JPanel lineNumberPanel) {
        lineNumberPanel.removeAll();
        lineNumberPanel.setLayout(new BoxLayout(lineNumberPanel, BoxLayout.Y_AXIS));

        Element root = textArea.getDocument().getDefaultRootElement();
        int lineCount = root.getElementCount();
        for (int i = 1; i <= lineCount; i++) {
            JLabel lineNumber = new JLabel(String.valueOf(i));
            lineNumber.setFont(new Font("Courier New", Font.PLAIN, 14));
            lineNumber.setBorder(new EmptyBorder(2, 5, 2, 5));
            lineNumberPanel.add(lineNumber);
        }
        lineNumberPanel.revalidate();
        lineNumberPanel.repaint();
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        selectStartPos = textArea.getSelectionStart();
        selectEndPos = textArea.getSelectionEnd();
        if (selectStartPos != selectEndPos) {
            selectedText = textArea.getSelectedText();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateLineNumbers(lineNumberPanel);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateLineNumbers(lineNumberPanel);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !(e.isControlDown())) {
            if (selectStartPos != selectEndPos) {
                controller.onlyInDocLocalDeleteRange(selectStartPos, selectEndPos);
            } else {
                controller.onLocalDelete(textArea.getCaretPosition());
            }
        } else if (!e.isControlDown() &&
                e.getKeyCode() != KeyEvent.VK_UP &&
                e.getKeyCode() != KeyEvent.VK_DOWN &&
                e.getKeyCode() != KeyEvent.VK_LEFT &&
                e.getKeyCode() != KeyEvent.VK_RIGHT &&
                e.getKeyCode() != KeyEvent.VK_TAB &&
                e.getKeyCode() != KeyEvent.VK_ALT &&
                e.getKeyCode() != KeyEvent.VK_SHIFT &&
                e.getKeyCode() != KeyEvent.VK_CANCEL &&
                e.getKeyCode() != KeyEvent.VK_CONTROL &&
                e.getKeyCode() != KeyEvent.VK_CAPS_LOCK &&
                e.getKeyCode() != KeyEvent.VK_ESCAPE &&
                e.getKeyCode() != KeyEvent.VK_END &&
                e.getKeyCode() != KeyEvent.VK_HOME
        ) {
            char value;
            value = e.getKeyChar();
            if (selectStartPos != selectEndPos) {
                controller.onLocalDeleteRange(selectStartPos, selectEndPos);
            }
            if (value != '\u0003' && value != '\u0018' && value != '\u0016') {
                controller.onLocalInsert(value, textArea.getCaretPosition());
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }



    public void clearTextArea() {
        textArea.setCaretPosition(0);
        textArea.setText("");
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(selectedFile)) {
                writer.write(textArea.getText());
            } catch (IOException ex) {
                logger.error("Ошибка при сохранении файла: " + ex.getMessage());
            }
        }
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            pause();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                StringBuilder fileContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
                controller.onLocalFileImport(fileContent.toString());
            } catch (IOException ex) {
                logger.error("Ошибка при загрузке файла: " + ex.getMessage());
            }
        }
    }

    public void insertCharToTextEditor(char value, int index) {
        textArea.insert(String.valueOf(value), index);
        if (index <= selectEndPos && index >= selectStartPos) {
            textArea.setSelectionEnd(textArea.getSelectionStart());
        }
        int i = textArea.getCaretPosition();
        if (index <= i) {
            i++;
            textArea.setCaretPosition(i);
        }
    }

    public void deleteCharFromTextEditor(int index) {
        if (index == 0) {
            return;
        }
        if (index <= selectEndPos && index >= selectStartPos) {
            textArea.setSelectionEnd(textArea.getSelectionStart());
        }
        int i = textArea.getCaretPosition();
        if (index <= i) {
            i--;
            textArea.setCaretPosition(i);
        }
        textArea.replaceRange("", index - 1, index);
    }

    public void pause() {
        showPauseWindow();
        new Thread(() -> importDialog.setVisible(true)).start();
        textArea.setEnabled(false);
    }

    private void showPauseWindow() {
        importDialog = new JDialog(this, getUtfString("Загрузка файла"), true);
        importDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JLabel messageLabel = new JLabel(getUtfString("Дождитесь загрузки файла..."));
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.add(messageLabel);
        importDialog.add(panel);
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/logo.png")));
        importDialog.setIconImage(logo.getImage());
        importDialog.setSize(new Dimension(100, 50));
        importDialog.pack();
        importDialog.setLocationRelativeTo(this);
    }

    public void unpause() {
        if (importDialog != null) {
            importDialog.dispose();
            importDialog = null;
        }
        textArea.setEnabled(true);
    }

    public void addPeerName(String name) {
        JLabel label = new JLabel(name);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14));
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрируем лейбл по горизонтали
        peersList.add(label);
        peersListPanel.add(label); // Добавляем лейбл в peersListPanel
        peersListPanel.revalidate();
        peersListPanel.repaint();
    }

    public void removePeerName(String name) {
        for (JLabel label : peersList) {
            if (label.getText().equals(name)) {
                peersPanel.remove(label);
            }
        }
        peersPanel.revalidate();
        peersPanel.repaint();
    }

    public void writeLog(String message) {
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.append(getUtfString(message) + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void writeChat(String author, String message) {
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.append(author + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void sendChatMessage(String message) {
        controller.sendChatMessage(message);
        writeChat(getUtfString("Вы"), message);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;

        // Изменяем размеры панелей
        int rightPanelWidth = (int) (screenWidth * RIGHT_PANEL_WIDTH_RATIO);
        int chatPanelWidth = (int) (screenWidth * CHAT_PANEL_WIDTH_RATIO);

        rightPanel.setPreferredSize(new Dimension(rightPanelWidth, rightPanel.getHeight()));
        chatPanel.setPreferredSize(new Dimension(chatPanelWidth, chatPanel.getHeight()));

        // Перерисовываем интерфейс
        revalidate();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}

