package utils;

import okp.nic.utils.PortChecker;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;

class PortCheckerTest {

    @Test
    void findAvailablePort_ReturnsValidPort() {
        int port = PortChecker.findAvailablePort();

        assertTrue(port > 0);
    }

    @Test
    void isPortAvailable_ValidPort_ReturnsTrue() throws IOException {
        int port = findAvailablePort();

        assertTrue(PortChecker.isPortAvailable(port));
    }

    @Test
    void isPortAvailable_InvalidPort_ReturnsFalse() {
        // Порты < 1024 зарезервированы и считаются недоступными
        assertFalse(PortChecker.isPortAvailable(80));
    }

    @Test
    void isPortAvailable_OccupiedPort_ReturnsFalse() throws IOException {
        int port = findAvailablePort();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            assertFalse(PortChecker.isPortAvailable(port));
        }
    }

    // Вспомогательный метод для поиска доступного порта
    private int findAvailablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}