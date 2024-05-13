package okp.nic.view.editor.texteditor;

import lombok.Getter;
import okp.nic.logger.Logger;
import okp.nic.presenter.Presenter;
import okp.nic.view.editor.chat.ChatPanel;
import okp.nic.view.editor.chat.ChatService;
import okp.nic.view.editor.chat.ChatServiceImpl;
import okp.nic.view.editor.log.LogPanel;
import okp.nic.view.editor.log.LogService;
import okp.nic.view.editor.log.LogServiceImpl;
import okp.nic.view.editor.peers.PeerPanel;
import okp.nic.view.editor.peers.PeerService;
import okp.nic.view.editor.peers.PeerServiceImpl;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JDialog;
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
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;

import static okp.nic.utils.Utils.getUtfString;

public class TextEditorView extends JFrame implements CaretListener, DocumentListener, KeyListener, ComponentListener, TextEditor {

    public static final double RIGHT_PANEL_WIDTH_RATIO = 0.2; // 20% от ширины экрана
    public static final double CHAT_PANEL_WIDTH_RATIO = 0.2; // 20% от ширины экрана
    public static final int MAX_CHAT_MESSAGE_LENGTH = 250;
    private final int screenWidth;

    private final Presenter presenter;

    @Getter
    private final JTextArea textArea = new JTextArea();
    private final JPanel rightPanel = new JPanel(new BorderLayout());
    private JDialog importDialog = new JDialog();

    @Getter
    private final ChatService chatService;
    @Getter
    private final PeerService peerService;
    @Getter
    private final LogService logService;

    private final ChatPanel chatPanel;
    private final LogPanel logPanel;
    private final PeerPanel peerPanel;

    private String selectedText;
    private String copiedText;
    private int selectStartPos;
    private int selectEndPos;

    //    private JPanel lineNumberPanel;

    public TextEditorView(Presenter presenter) {
        this.presenter = presenter;
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
        chatPanel = new ChatPanel(new BorderLayout(), presenter, screenWidth);
        chatService = new ChatServiceImpl(chatPanel);
        // настройки панели пиров
        peerPanel = new PeerPanel(new BorderLayout(), screenWidth);
        peerService = new PeerServiceImpl(peerPanel);
        // панель логов
        logPanel = new LogPanel(new BorderLayout());
        logService = new LogServiceImpl(logPanel);
        Logger.setLogService(logService);
        // окно паузы
        setupPauseWindow();
        // финальные настройки
        mainPanel.add(chatPanel, BorderLayout.EAST);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.addComponentListener(this);
        addKeyListener(this);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);
    }

    private JFrame setupFrame() {
        JFrame frame = new JFrame("CRDT");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/logo.png")));
        frame.setIconImage(logo.getImage());
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
//        lineNumberPanel = new JPanel() {
//            @Override
//            public Dimension getPreferredSize() {
//                return new Dimension(70, textArea.getPreferredSize().height);
//            }
//        };
//        lineNumberPanel.setBackground(Color.LIGHT_GRAY);
//        lineNumberPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
        // настройки панели с текстом
        JPanel textPanel = new JPanel(new BorderLayout());
//        textPanel.add(lineNumberPanel, BorderLayout.WEST);
        textPanel.add(textArea, BorderLayout.CENTER);
        // настройка скроллинга
        JScrollPane scrollPane = new JScrollPane(textPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setUnitIncrement(20);
        // добавляем все поля (через scrollPane) в главную панель
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel setupRightPanel() {
        rightPanel.setPreferredSize(new Dimension((int) (screenWidth * RIGHT_PANEL_WIDTH_RATIO), 0));
        // добавление элементов на панель
        rightPanel.add(peerPanel.getPeersScrollPane(), BorderLayout.NORTH);
        rightPanel.add(logPanel.getLogScrollPane(), BorderLayout.CENTER);
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
        saveMenuItem.addActionListener(e -> presenter.saveFile());
        loadMenuItem.addActionListener(e -> presenter.loadFile());
        return menuBar;
    }

    private void setupPauseWindow() {
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

    private void setupKeyStrokeActions() {
        InputMap inputMap = textArea.getInputMap();
        ActionMap actionMap = textArea.getActionMap();
        // копирование
        KeyStroke copyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(copyKeyStroke, "copy");
        actionMap.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiedText = selectedText;
            }
        });
        // вырезание
        KeyStroke cutKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(cutKeyStroke, "cut");
        actionMap.put("cut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiedText = selectedText;
                presenter.onLocalDeleteRange(selectStartPos, selectEndPos);
            }
        });
        // вставка
        KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(pasteKeyStroke, "paste");
        actionMap.put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (copiedText != null && !copiedText.isEmpty() && selectStartPos == selectEndPos) {
                    insertTextBlock(copiedText, textArea.getCaretPosition());
                    presenter.onLocalInsertBlock(textArea.getCaretPosition(), copiedText);
                }
            }
        });
        // лочим ctrl+backspace
        KeyStroke deleteWordKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(deleteWordKeyStroke, "deleteWord");
        actionMap.put("deleteWord", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
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
//        updateLineNumbers(lineNumberPanel);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
//        updateLineNumbers(lineNumberPanel);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !(e.isControlDown())) {
            if (selectStartPos != selectEndPos) {
                deleteRange(selectStartPos + 1 , selectEndPos);
                presenter.onLocalDeleteRange(selectStartPos, selectEndPos);
            } else {
                presenter.onLocalDelete(textArea.getCaretPosition());
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
                presenter.onLocalDeleteRange(selectStartPos, selectEndPos);
            }
            if (value != '\u0003' && value != '\u0018' && value != '\u0016') {
                presenter.onLocalInsert(value, textArea.getCaretPosition());
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void clear() {
        textArea.setCaretPosition(0);
        textArea.setText("");
    }

    @Override
    public void insertChar(char value, int index) {
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

    @Override
    public void deleteChar(int index) {
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

    @Override
    public void insertTextBlock(String text, int pos) {
        textArea.insert(text, pos);
    }

    @Override
    public void deleteRange(int startPos, int endPos) {
        textArea.replaceRange("", startPos, endPos);
    }

    @Override
    public void setCaretPosition(int pos) {
        textArea.setCaretPosition(pos);
    }

    @Override
    public int getCaretPosition() {
        return textArea.getCaretPosition();
    }

    @Override
    public String getText() {
        return textArea.getText();
    }

    @Override
    public void pause() {
        new Thread(() -> {
            System.out.println("hey");
            importDialog.setVisible(true);
        }).start();
        textArea.setEnabled(false);
    }

    @Override
    public void unpause() {
        if (importDialog != null) {
            importDialog.dispose();
        }
        textArea.setEnabled(true);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int rightPanelWidth = (int) (screenWidth * RIGHT_PANEL_WIDTH_RATIO);
        int chatPanelWidth = (int) (screenWidth * CHAT_PANEL_WIDTH_RATIO);
        rightPanel.setPreferredSize(new Dimension(rightPanelWidth, rightPanel.getHeight()));
        chatPanel.setPreferredSize(new Dimension(chatPanelWidth, chatPanel.getHeight()));
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


//    private void updateLineNumbers(JPanel lineNumberPanel) {
//        lineNumberPanel.removeAll();
//        lineNumberPanel.setLayout(new BoxLayout(lineNumberPanel, BoxLayout.Y_AXIS));
//        Element root = textArea.getDocument().getDefaultRootElement();
//        int lineCount = root.getElementCount();
//        for (int i = 1; i <= lineCount; i++) {
//            JLabel lineNumber = new JLabel(String.valueOf(i));
//            lineNumber.setFont(new Font("Courier New", Font.PLAIN, 14));
//            lineNumber.setBorder(new EmptyBorder(2, 5, 2, 5));
//            lineNumberPanel.add(lineNumber);
//        }
//        lineNumberPanel.revalidate();
//        lineNumberPanel.repaint();
//    }