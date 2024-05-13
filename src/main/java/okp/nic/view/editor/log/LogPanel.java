package okp.nic.view.editor.log;

import lombok.Getter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import static okp.nic.utils.Utils.getUtfString;

@Getter
public class LogPanel extends JPanel {

    private final JTextArea logArea = new JTextArea();

    private final JScrollPane logScrollPane = new JScrollPane(this);

    public LogPanel(BorderLayout bl) {
        super(bl);
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BorderLayout());
        logArea.setEditable(false);
        // заголовок логи
        JLabel logLabel = new JLabel(getUtfString("ЖУРНАЛ ЛОГОВ"));
        logLabel.setFont(logLabel.getFont().deriveFont(Font.BOLD, 16));
        logLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        logLabel.setOpaque(true);
        logLabel.setBackground(Color.LIGHT_GRAY);
        logLabel.setHorizontalAlignment(JLabel.CENTER);
        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(logArea, BorderLayout.CENTER);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }


}
