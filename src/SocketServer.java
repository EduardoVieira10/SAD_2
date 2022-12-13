import java.net.*;
import java.io.*;

public class SocketServer {

    private ServerSocket serverSocket;
    private Socket clientSocket;

    private PrintWriter output;
    private BufferedReader input;
    private EncryptionAlgorithm encryptionAlgorithm = new EnhancedCeaser();

    /**
     * start the server and wait for connections on the given port

     * @param port - port to connect the client to
     */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        System.out.println("Server started. Opening port " + port);
        System.out.println("Waiting for client...");

        clientSocket = serverSocket.accept();
        System.out.println("Client accepted :)");
    }

    /**
     * Handle messages received on the server and log them with the respective timestamp
     */
    public void processServerCommunications() throws IOException {
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String message = "";
        long time;

        while(!decrypt(message).equals("BYE")){
            time = System.currentTimeMillis();
            message = input.readLine();
            if(message != null){
                String decryptedMessage = decrypt(message);
                System.out.println("[" + time + "]" + " message received: ( " + message + " ) " + decryptedMessage);
            }
        }
    }

    /**
     * Terminate the service - close ALL I/O
     */
    public void stop() throws IOException {
        serverSocket.close();
        System.out.println("Connection closed. Bye! ;)");
    }

    /**
     * Read the file at `filePath`, create, configure and return a new instance of the EncryptionAlgorithm

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
    private String encrypt(String message) { return this.encryptionAlgorithm.encrypt(message); }

    /**
    * Mostly useful for debug purposes
    */
    @Override
    public String toString() {
        return "Server Configuration{" +
                "server Address=" + serverSocket.getInetAddress() +
                ", clientSocket=" + serverSocket.getLocalPort() +
                '}' +
                "EncryptionAlgorithm: " + encryptionAlgorithm;
    }

    // location in the command line arguments' array where the path is provided
    static int COMMAND_LINE_ARGUMENT_FILE_PATH = 0;

    /**
    * __Reminders:__
    * - validate inputs
    * - Start the server service & configure the encryption algorithm
    * - handle errors
    * - terminate the serviceon demand
    *
    * @param args - command line arguments. args[0] SHOULD contain the absolute path for the configuration file
     * @throws IndexOutOfBoundsException if input is not equals to 1 in position args[0]
     * @throws Exception if XML file isn't correct
    */
    public static void main(String[] args) {
        SocketServer server = new SocketServer();

        try{
            String filePath = args[COMMAND_LINE_ARGUMENT_FILE_PATH];

            server.configureEncryptionAlgorithm(filePath);

            server.start(6666);

            server.processServerCommunications();

            server.stop();

        } catch (IndexOutOfBoundsException e){
            System.out.println("ERROR: should to insert 1 argument");
        } catch (Exception e){
            System.out.println("ERROR: something went wrong, try again later. reason: " + e.getLocalizedMessage());
        }
        
    }
}