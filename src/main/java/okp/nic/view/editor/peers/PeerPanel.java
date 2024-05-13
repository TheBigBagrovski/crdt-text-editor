package okp.nic.view.editor.peers;

import lombok.Getter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import static okp.nic.utils.Utils.getUtfString;
import static okp.nic.view.editor.texteditor.TextEditorView.RIGHT_PANEL_WIDTH_RATIO;

@Getter
public class PeerPanel extends JPanel {

    private final JPanel peersListPanel = new JPanel();
    private final List<JLabel> peersList = new ArrayList<>();

    @Getter
    private final JScrollPane peersScrollPane = new JScrollPane(this);

    public PeerPanel(BorderLayout bl, int screenWidth) {
        super(bl);
        setPreferredSize(new Dimension((int) (screenWidth * RIGHT_PANEL_WIDTH_RATIO), 300));
        setLayout(new BorderLayout());
        // заголовок пиры
        JLabel peersLabel = new JLabel(getUtfString("PEERS"));
        peersLabel.setFont(peersLabel.getFont().deriveFont(Font.BOLD, 16));
        peersLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        peersLabel.setOpaque(true);
        peersLabel.setBackground(Color.LIGHT_GRAY);
        peersLabel.setHorizontalAlignment(JLabel.CENTER);
        // панель для списка пиров
        peersListPanel.setLayout(new BoxLayout(peersListPanel, BoxLayout.Y_AXIS));
        add(peersListPanel, BorderLayout.CENTER);
        peersScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        peersScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(peersLabel, BorderLayout.NORTH);
    }

}
