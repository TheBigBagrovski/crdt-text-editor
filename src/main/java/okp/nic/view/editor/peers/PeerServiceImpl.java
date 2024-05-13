package okp.nic.view.editor.peers;

import lombok.AllArgsConstructor;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Font;
import java.util.Iterator;

@AllArgsConstructor
public class PeerServiceImpl implements PeerService{

    private final PeerPanel peerPanel;

    @Override
    public void addPeerName(String name) {
        JLabel label = new JLabel(name);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        peerPanel.getPeersList().add(label);
        peerPanel.getPeersListPanel().add(label);
        peerPanel.getPeersListPanel().revalidate();
        peerPanel.getPeersListPanel().repaint();
    }

    @Override
    public void removePeerName(String name) {
        for (int i = 0; i < peerPanel.getPeersList().size(); i++) {
            if (peerPanel.getPeersList().get(i).getText().equals(name)) {
                peerPanel.getPeersListPanel().remove(peerPanel.getPeersList().get(i));
                peerPanel.getPeersList().remove(i);
                break;
            }
        }
        peerPanel.getPeersListPanel().revalidate();
        peerPanel.getPeersListPanel().repaint();
    }

}
