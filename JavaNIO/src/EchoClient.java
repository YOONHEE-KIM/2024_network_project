import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
    /*
    IOCP/epoll의 개념을 살려서 구현
    자바 NIO에서 구현하기 위해서는 Selector와 Channel을 사용하면 된다.
    Selector -> 비동기 이벤트처리(소켓채널의 이벤트 모니터링, 관리)
    */
public class EchoClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999;

    public static void main(String[] args) {
        try {
            //소켓채널 생성하고 서버연결
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //서버로 데이터 보내기
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("메시지 입력: ");
                String message = scanner.nextLine();
                if ("끝내기".equalsIgnoreCase(message)) {
                    break;
                }
                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                while (buffer.hasRemaining()) {
                    socketChannel.write(buffer); //비동기로 서버에 데이터 전송
                }

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
                System.out.println("서버응답: " + new String(buffer.array()).trim());
            }

            socketChannel.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
