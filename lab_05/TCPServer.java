import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class TCPServer {

    private static final int PORT_NUMBER = 6789;
    private static final int TIMEOUT_INTERVAL = 50000;

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT_NUMBER);
            server.setSoTimeout(TIMEOUT_INTERVAL);
            while(true) {
                try {
                    System.out.println("Waiting for client on port " + server.getLocalPort() + "...");
                    Socket connectionSocket = server.accept();
                    System.out.println("Just connected to " + connectionSocket.getRemoteSocketAddress() + ".");
                    handleConnection(connectionSocket);
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timed out.");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to open receiving socket: " + e.getMessage());
            return;
        } finally {
            try { if (server != null) { server.close(); } } catch (IOException e) {}
        }
    }

    private static void handleConnection(Socket connectionSocket) {
        DataInputStream inputFromClient = null;
        DataOutputStream outputToClient = null;
        try {
            connectionSocket.setSoTimeout(TIMEOUT_INTERVAL);
            inputFromClient = new DataInputStream(connectionSocket.getInputStream());
            outputToClient = new DataOutputStream(connectionSocket.getOutputStream());
            while (true) {
                int length = inputFromClient.readInt();
                System.out.println(length);
                if (length == 0) {
                    break;
                } else {
                    byte[] bytes = new byte[length];
                    inputFromClient.read(bytes, 0, length);
                    String message = new String(bytes);
                    System.out.println(message);
                    sendResponse(outputToClient, message);
                }
            }

        } catch (IOException e) {
            System.out.print("Failed to handle the connection: " + e.getMessage());
        } finally {
            try { if (inputFromClient != null) { inputFromClient.close(); } } catch (IOException e) {}
            try { if (outputToClient != null) { outputToClient.close(); } } catch (IOException e) {}
            try { if (connectionSocket != null) { connectionSocket.close(); } } catch (IOException e) {}
        }
    }

    private static void sendResponse(DataOutputStream outputToClient, String message) throws IOException {
        Map<String, Integer> occurrenceMap = getOccurrences(message);
        outputToClient.writeInt(occurrenceMap.size());
        for (Map.Entry<String, Integer> entry: occurrenceMap.entrySet()) {
            int times = entry.getValue();
            int length = entry.getKey().length();
            outputToClient.writeInt(length);
            byte[] bytes = entry.getKey().getBytes();
            outputToClient.write(bytes, 0, length);
            outputToClient.writeInt(times);
        }
    }

    private static Map<String, Integer> getOccurrences(String message) {
        Map<String, Integer> occurrenceMap = new TreeMap<String, Integer>();
        // read only the alphabetic Strings
        Scanner fileScanner = new Scanner(message).useDelimiter("[^a-zA-Z]+");
        while (fileScanner.hasNext()) {
            String word = fileScanner.next().toLowerCase();
            Integer wordCount = occurrenceMap.get(word);
            if (wordCount == null) {
                wordCount = 0;
            }
            occurrenceMap.put(word, wordCount + 1);
        }
        fileScanner.close();
        return occurrenceMap;
    }

}
