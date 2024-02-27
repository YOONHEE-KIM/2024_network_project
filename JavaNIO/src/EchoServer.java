import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            //스레드 풀 생성하는 코드 추가
            ExecutorService executorService = Executors.newFixedThreadPool(10);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        handleAcceptableKey(key, selector);
                    } else if (key.isReadable()) { //읽기 이벤트 처리하는 부분
                        handleReadableKey(key, buffer, executorService);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleAcceptableKey(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }
    private static void handleReadableKey(SelectionKey key, ByteBuffer buffer, ExecutorService executorService) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            buffer.clear();
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                clientChannel.close();
                key.cancel();
                return;
            }
            buffer.flip();
            executorService.submit(() -> {
                try {
                    //처리 시간 오래 걸리는 부분 스레드 풀에서 비동기처리
                    clientChannel.write(buffer);
                    System.out.println("클라이언트에서 보낸 메시지: " + new String(buffer.array()).trim());
                    buffer.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            try {
                clientChannel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
