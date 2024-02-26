import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ChatGameClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private SocketChannel socketChannel;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private String nickname;

    public ChatGameClient(String nickname) {
        this.nickname = nickname;
        try {
            socketChannel = SocketChannel.open(); //소켓 채널 열기
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("서버 연결 성공");
            sendMessage(nickname + " 님 반갑습니다");

            //서버로부터 메시지 비동기적으로 수신하기 위한 별도 스레드
            Thread thread = new Thread(this::receiveMessages);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            socketChannel.write(ByteBuffer.wrap(message.getBytes())); //문자열 ByteBuffer로 변환해서 소켓 전송
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            while (true) {
                buffer.clear();
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                buffer.flip(); // 버퍼읽기 모드
                byte[] bytes = new byte[buffer.remaining()]; //남은 데이터 크기만큼 바이트 배열 생성
                buffer.get(bytes);
                String message = new String(bytes).trim(); //바이크 배열 문자열로 변환-> 공백제거
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); //Scanner객체 생성 : 표준입력 받음
        System.out.print("닉네임을 입력하세요: ");
        String nickname = scanner.nextLine();

        ChatGameClient client = new ChatGameClient(nickname);

        // 사용자 입력 및 메시지 전송 스레드
        Thread senderThread = new Thread(() -> {
            try {
                while (true) {
                    System.out.print("메시지를 입력하세요: ");
                    String message = scanner.nextLine();
                    client.sendMessage(message);
                }
            } finally {
                scanner.close(); //스캐너 닫기
            }
        });
        senderThread.start(); // 스레드 시작
    }
}
