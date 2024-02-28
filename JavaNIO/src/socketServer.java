import java.io.*;
import java.net.*;

/*
블로킹 I/O를 사용해서 클라이언트와의 통신을 처리하는 코드.(다중 클라이언트 지원)
클라이언트 연결 수락을 위해 ServerSocket.accept() 메서드를 호출
-> 클라이언트가 연결될 때 까지 블로킹 됨.
-> 각 클라이언트 통신을 위해서 쓰레드를 별도로 생성하고 블로킹 작업 처리함
NIO 비동기 논블로킹 방식에서는 이벤트 기장의 처리 + 셀렉터로 블로킹 되지 않는 비동기적 IO를 수행함.
 */
public class socketServer {
    public static void main(String[] args){
        int port = 7777;
        int clientCount = 0;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결을 기다리고 블로킹
                System.out.println("New client connected: " + clientSocket);

                clientCount++;

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("Welcome, you are client #" + clientCount + ".");

                //각 클라이언트에 대해 새로운 스레드를 생성하여 통신을 처리함
                // 각 클라이언트와의 통신을 처리하는 스레드 시작
                new ClientHandler(clientSocket, clientCount).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 각 클라이언트와의 통신을 처리하는 스레드
    private static class ClientHandler extends Thread {
        private Socket socket;
        private int clientNumber;

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
        }
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 블로킹된 입력 스트림
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true) // 블로킹된 출력 스트림
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) { // 클라이언트로부터의 메시지를 읽고 블로킹됨
                    System.out.println("Message from client #" + clientNumber + ": " + inputLine);
                    out.println("Server received: " + inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Client #" + clientNumber + " disconnected.");
            }
        }
    }
}
