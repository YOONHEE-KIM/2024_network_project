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
            String serverResponse = in.readLine().trim();
            System.out.println("Server: " + serverResponse);

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput.trim());
                //버퍼 비우고 데이터 전송
                out.flush();

                //클라이언트가 입력하면 즉시 에코 받도록
                serverResponse = in.readLine().trim();
                System.out.println("Server: " + serverResponse);
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
