import java.io.*;
import java.net.*;

public class socketClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 7777;

        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to server: " + serverAddress + ":" + serverPort);

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("Server: " + in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
