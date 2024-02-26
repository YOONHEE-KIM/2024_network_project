import java.io.*;
import java.net.*;

/*
NIO를 사용한 단방향 통신도 가능한데, 스트림 기반의 기존 소켓을 사용한 단방향 통신과는 몇가지 차이점이 있음.
    기존 스트림 기반 소켓을 사용한 통신 -> 전통적인 동기화된 입출력 방식임
    각 클라이언트 연결에 대해 쓰레드를 생성하여 IO 수행함. 쓰레드당 한 번에 하나의 작업이기 때문에
    -> 확장성 낮음

    NIO는 비동기적이고 이벤크 기반의 입출력 모델 사용함.(논블록 설정도 가능.)
    한 쓰레드에서 여러 개의 연결을 관리하고 다중 연결에 대한 입출력을 비동기적으로 처리할 수 있음.
    -> Selector, 버퍼, 채널, 비동기 이벤트 처리 등
 */
public class StreamServer {
    // 단일 클라이언트 연결만 가능한 에코서버
    public static void main(String[] args) throws IOException {
        int portNumber = 12345;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("서버연결 성공");

            while (true) {
                //무한 루프 내부에서 클라이언트 연결을 수락하는 내용 작성 : 단일 클라이언트 지원
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Client: " + inputLine);
                        out.println("Server received: " + inputLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


