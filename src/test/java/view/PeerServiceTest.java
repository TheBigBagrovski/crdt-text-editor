package view;

import okp.nic.view.editor.peers.PeerPanel;
import okp.nic.view.editor.peers.PeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PeerServiceTest {

    @Mock
    private PeerPanel peerPanel;

    private PeerServiceImpl peerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        List<JLabel> peersList = new ArrayList<>();
        when(peerPanel.getPeersList()).thenReturn(peersList);
        JPanel peersListPanel = mock(JPanel.class);
        when(peerPanel.getPeersListPanel()).thenReturn(peersListPanel);

        peerService = new PeerServiceImpl(peerPanel);
    }

    @Test
    void addPeerName() {
        String peerName = "Test Peer";

        peerService.addPeerName(peerName);

        verify(peerPanel, times(1)).getPeersList();
//        verify(peerPanel, times(2)).getPeersListPanel();
        verify(peerPanel.getPeersListPanel()).add(any(JLabel.class));
        verify(peerPanel.getPeersListPanel()).revalidate();
        verify(peerPanel.getPeersListPanel()).repaint();
        assertFalse(peerPanel.getPeersList().isEmpty());
        assertEquals(peerName, peerPanel.getPeersList().get(0).getText());
    }

    @Test
    void removePeerName() {
        String peerName = "Test Peer";
        JLabel label = new JLabel(peerName);
        peerPanel.getPeersList().add(label);
        peerPanel.getPeersListPanel().add(label);

        peerService.removePeerName(peerName);

        verify(peerPanel, atLeastOnce()).getPeersList();
        verify(peerPanel.getPeersListPanel()).remove(label);
        verify(peerPanel.getPeersListPanel()).revalidate();
        verify(peerPanel.getPeersListPanel()).repaint();
    }

}