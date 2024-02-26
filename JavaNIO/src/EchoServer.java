import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
    /*
    IOCP/epoll의 개념을 살려서 구현(비동기 및 논블로킹)
    자바 NIO에서 구현하기 위해서는 Selector와 Channel을 사용하면 된다.
     */
public class EchoServer {
    private static final int PORT = 9999;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            /*
            Selector(->비동기 이벤트 모니터링하는 역할)와 ServerSocketChannel생성
            생성한 ServerSocketChannel을 포트에 바인딩
            소켓채널을 논블로킹 모드로 설정
             */
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //클라이언트 연결 수락

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept(); //연결 수락
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ); //읽기 등록
                    } else if (key.isReadable()) { //읽기 이벤트 처리하는 부분
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        buffer.clear();
                        int bytesRead = clientChannel.read(buffer);
                        if (bytesRead == -1) { //클라이언트가 연결 끊은 경우(끝내기를 입력하거나 접속을 끊은 경우)
                            clientChannel.close();
                            key.cancel();
                            continue;
                        }
                        buffer.flip();
                        //클라이언트에 다시 데이터 보냄
                        clientChannel.write(buffer);

                        // 연결 유지하고 다시 읽기이벤트 대기
                        key.interestOps(SelectionKey.OP_READ);
                        //클라이언트의 메시지 서버에 출력
                        System.out.println("클라이언트에서 보낸 메시지: " + new String(buffer.array()).trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
