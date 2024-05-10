package okp.nic.gui.editor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okp.nic.network.Controller;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
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

@Slf4j
public class TextEditor extends JFrame implements CaretListener, DocumentListener, KeyListener {

    private static final int FRAME_WIDTH = 1100;
    private static final int FRAME_HEIGHT = 700;
    private static final Dimension FRAME_SIZE = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
    private static final int PEERS_WIDTH = 250;
    private static final int PEERS_HEIGHT = 300;
    private static final Dimension PEERS_SIZE = new Dimension(PEERS_WIDTH, PEERS_HEIGHT);
    private static final int LOG_WIDTH = 250;
    private static final int LOG_HEIGHT = 400;
    private static final Dimension LOG_SIZE = new Dimension(LOG_WIDTH, LOG_HEIGHT);
    private static final int RIGHT_PANEL_WIDTH = 250;
    private static final int RIGHT_PANEL_HEIGHT = 700;
    private static final Dimension RIGHT_PANEL_SIZE = new Dimension(RIGHT_PANEL_WIDTH, RIGHT_PANEL_HEIGHT);

    private final Controller controller;

    @Getter
    private final JTextArea textArea = new JTextArea();

    private final JPanel peersPanel = new JPanel();
    private final List<JLabel> peersList = new ArrayList<>();

    private JDialog importDialog;

    @Getter
    @Setter
    private int cursorPos;

    public TextEditor(Controller controller) {
        this.controller = controller;
        // настройки фрейма
        JFrame frame = new JFrame("CRDT");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/logo.png")));
        frame.setIconImage(logo.getImage());
        frame.setMinimumSize(FRAME_SIZE);
        frame.setVisible(true);
        // настройки основной панели
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setVisible(true);
        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(scrollPane);
        // настройки текстового поля
        textArea.setFont(new Font("Courier New", Font.PLAIN, 18));
        textArea.addCaretListener(this);
        textArea.addKeyListener(this);
        textArea.getDocument().addDocumentListener(this);
        // настройки панели логов
        JPanel logPanel = new JPanel();
        logPanel.setSize(LOG_SIZE);
        JScrollPane logScrollPane = new JScrollPane(logPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // настройки панели пиров
        peersPanel.setPreferredSize(PEERS_SIZE);
        JScrollPane peersScrollPane = new JScrollPane(peersPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // правая панель
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(RIGHT_PANEL_SIZE);
        rightPanel.add(peersScrollPane, BorderLayout.NORTH);
        rightPanel.add(logScrollPane, BorderLayout.CENTER);
        // настройки меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(getUtfString("Файл"));
        menuBar.add(fileMenu);
        JMenuItem saveMenuItem = new JMenuItem(getUtfString("Сохранить"));
        JMenuItem loadMenuItem = new JMenuItem(getUtfString("Загрузить"));
        fileMenu.add(saveMenuItem);
        fileMenu.add(loadMenuItem);
        saveMenuItem.addActionListener(e -> saveFile());
        loadMenuItem.addActionListener(e -> loadFile());
        frame.setJMenuBar(menuBar);
        // финальные настройки
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        addKeyListener(this);
        frame.setVisible(true);
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        cursorPos = e.getDot();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            controller.onDelete(this.getCursorPos());
        } else if (e.getKeyCode() != KeyEvent.VK_UP &&
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
            controller.onInsert(value, this.getCursorPos());
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
                log.error("Ошибка при сохранении файла: " + ex.getMessage());
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
                controller.clear();
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
                controller.importTextFromFile(fileContent.toString());
            } catch (IOException ex) {
                log.error("Ошибка при загрузке файла: " + ex.getMessage());
            }
        }
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
        importDialog.setSize(new Dimension(100,50));
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
        peersList.add(label);
        peersPanel.add(label);
        peersPanel.revalidate();
        peersPanel.repaint();
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

}

