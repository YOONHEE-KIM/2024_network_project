import java.io.*;
import java.net.*;

public class StreamClient {
    public static void main(String[] args) throws IOException {
        String hostName = "localhost";
        int portNumber = 12345;

        /*  서버와의 통신을 위해 소켓을 설정
            서버에 데이터를 보내고
            서버로부터 데이터를 받기 위한 입력 및 출력 스트림을 생성
         */

        try (Socket socket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("Server: " + in.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

