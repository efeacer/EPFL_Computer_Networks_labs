import java.io.*;
import java.net.Socket;
import java.util.Map;

public class TCPClient {

    private static final String SERVER_NAME = "localhost";
    private static final int PORT_NUMBER = 6789;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        BufferedReader inputFromUser = null;
        Socket client = null;
        DataOutputStream outputToServer = null;
        DataInputStream inputFromServer = null;
        try {
            inputFromUser = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connecting to " + SERVER_NAME + " on port " + PORT_NUMBER + "...");
            client = new Socket(SERVER_NAME, PORT_NUMBER);
            System.out.println("Just connected to " + client.getRemoteSocketAddress() + ".");
            outputToServer = new DataOutputStream(client.getOutputStream());
            inputFromServer = new DataInputStream(client.getInputStream());
            while (true) {
                System.out.print("Enter a file name: ");
                String fileName = inputFromUser.readLine();
                if (sendFile(outputToServer, fileName)) {
                    handleResponse(inputFromServer);
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to process request : " + e.getMessage());
        } finally {
            try { if (inputFromServer != null) { inputFromUser.close(); } } catch (IOException e) {}
            try { if (client != null) { client.close(); } } catch (IOException e) {}
            try { if (outputToServer != null) { outputToServer.close(); } } catch (IOException e) {}
            try { if (inputFromServer != null) { inputFromServer.close(); } } catch (IOException e) {}
        }
    }

    private static void handleResponse(DataInputStream inputFromServer) throws IOException {
        int numValues = inputFromServer.readInt();
        System.out.println("There are " + numValues + " unique words in the document.\n");
        for (int i = 0; i < numValues; i++) {
            int length = inputFromServer.readInt();
            byte[] bytes = new byte[length];
            inputFromServer.read(bytes, 0, length);
            String word = new String(bytes);
            int times = inputFromServer.readInt();
            System.out.println(word + ": " + times);
        }
    }

    private static boolean sendFile(DataOutputStream outputStream, String fileName) throws IOException {
        boolean sent = false;
        if (fileName.isEmpty()) {
            outputStream.write(0);
        } else {
            int length = getFileLength(fileName);
            System.out.println("The file has length: " + length + " bytes.");
            outputStream.writeInt(length);
            sendBytes(new FileInputStream(fileName), outputStream);
            sent = true;
        }
        return sent;
    }

    private static void sendBytes(FileInputStream inputStream, DataOutputStream outputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes;
        while ((bytes = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytes);
        }
    }

    private static FileInputStream getFileReader(String fileName) {
        FileInputStream inputStream = null;
        boolean fileExists = true;
        try {
            inputStream = new FileInputStream(fileName);
        } catch (FileNotFoundException exception) {
            fileExists = false;
        }
        return inputStream;
    }

    private static int getFileLength(String fileName) {
        File file = new File(fileName);
        return (int) file.length();
    }

    private static void printHashMap(Map<String, Integer> occurrenceMap) {
        for (Map.Entry<String, Integer> entry: occurrenceMap.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue().toString());
        }
    }

}
