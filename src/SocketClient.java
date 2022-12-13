
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class SocketClient {
    private Socket socket;

    private Scanner inputScanner;
    private PrintWriter output;
    private static BufferedReader input;
    private EncryptionAlgorithm encryptionAlgorithm;

    final String SERVER_IP = "127.0.0.1";
    final String CLOSE_COMMAND = "BYE";
    static int COMMAND_LINE_ARGUMENT_FILE_PATH = 0;

    /**
     * Constructor
     */
    public SocketClient(){
    }

    /**
     * Connect the client to the server at `SERVER_IP` on the given port
     *
     * @param port - port to connect the client to
     */
    public void startConnection(int port) throws IOException {
        socket = new Socket(SERVER_IP, port);
        System.out.println("Client started. Start typing your messages :)");
    }

    /**
     * Handle the client's communications
     */
    public void handleClientCommunications(String clientInput) throws IOException {
        output = new PrintWriter(socket.getOutputStream(),true);
        String encryptMessage = encrypt(clientInput.toUpperCase());
        output.println(encryptMessage);
    }

    /**
     * Notify the server to close the session
     */
    private void sendCloseCommand() throws IOException {
        output = new PrintWriter(socket.getOutputStream(),true);
        output.println(encrypt(CLOSE_COMMAND));
        System.out.println("Connection closed. Bye ;)");
    }

    /**
     * Close ALL client I/O
     */
    public void stop() throws IOException {
        socket.close();
    }

    /**
     * Read the file on the given `filePath`, create, configure and return a new instance of the EncryptionAlgorithm
     *
     * @param filePath - path where the configuration file is located
     */
    private void configureEncryptionAlgorithm(String filePath) throws Exception {
        EnhancedCeaser enhancedCeaser = new EnhancedCeaser();
        enhancedCeaser.configure(filePath);
        this.encryptionAlgorithm = enhancedCeaser;
    }

    /**
     * Decrypts the given message using the instance's encryption algorithm
     *
     * @param message - message to be decrypted
     * @return the decrypted (original) message
     */
    private String decrypt(String message) {
        return this.encryptionAlgorithm.decrypt(message);
    }

    /**
     * Encrypts the given message using the instance's encryption algorithm
     *
     * @param message - Message to be encrypted
     * @return the encrypted message
     */
    private String encrypt(String message) {
        return this.encryptionAlgorithm.encrypt(message);
    }

    /**
     * Mostly useful for debug purposes
     */
    public String toString() {
        return "Client Configuration{" +
                "socket Address=" + socket.getInetAddress() +
                ", clientSocket=" + socket.getPort() +
                '}' +
                "EncryptionAlgorithm: " + encryptionAlgorithm;
    }

    /**
     * __Reminders:__
     * - validate inputs
     * - connect to the server & configure the encryption algorithm
     * - handle errors
     *
     * @param args - command line arguments. args[0] SHOULD contain the absolute path for the configuration file
     * @throws IndexOutOfBoundsException if input is not equals to 1 in position args[0]
     * @throws Exception if XML file isn't correct
     */
    public static void main(String[] args) {
        SocketClient socketClient = new SocketClient();
        input = new BufferedReader(new InputStreamReader(System.in));

        try {
            String filePath = args[COMMAND_LINE_ARGUMENT_FILE_PATH];

            socketClient.configureEncryptionAlgorithm(filePath);

            socketClient.startConnection(6666);

            String userInput;
            while ((userInput = input.readLine()) != null) {
                if(!userInput.trim().toUpperCase().equals(socketClient.CLOSE_COMMAND)){
                    socketClient.handleClientCommunications(userInput);
                } else {
                    socketClient.sendCloseCommand();
                    break;
                }
            }
            socketClient.stop();
        } catch (IndexOutOfBoundsException e) {
            System.out.println("ERROR: should to insert 1 argument");
        } catch (Exception e){
            System.out.println("ERROR :something went wrong, try again later. reason: " + e.getLocalizedMessage());
        }
    }
}
