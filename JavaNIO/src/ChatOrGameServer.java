import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Set;
    /*
    비동기 MP서버 셀렉트 이상 코드 -> 채팅서버 + 간단한 놀이
     */
public class ChatOrGameServer {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private static int userCount = 0;
    private static boolean gameStarted = false;
        private static Selector selector;

        public static void main(String[] args) {
        try{
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            System.out.println("서버 시작. 포트: " + PORT);

            while(true){
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while(keyIterator.hasNext()){
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if(key.isAcceptable()){
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);
                        userCount++;
                        broadcastMessage("사용자" + userCount + "입장");
                        System.out.println("클라이언트 연결됨: " + clientChannel.getRemoteAddress());
                    } else if(key.isReadable()){
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        buffer.clear();
                        int bytesRead = clientChannel.read(buffer);
                        if(bytesRead == -1){
                            System.out.println("클라이언트 연결 해제: " + clientChannel.getRemoteAddress());
                            userCount--;
                            broadcastMessage("사용자 퇴장");
                            clientChannel.close();
                            key.channel();
                            continue;
                        }
                        buffer.flip();
                        byte[] data = new byte[bytesRead];
                        buffer.get(data, 0, bytesRead);
                        String message = new String(data);
                        System.out.println("상대방의 메시지: " + message);

                        if(!gameStarted && message.equalsIgnoreCase("게임 시작") && userCount >= 2){
                            broadcastMessage("베스킨라빈스 31 게임 시작!");
                            gameStarted = true;
                        } else if(gameStarted && message.equals("31")){
                            broadcastMessage("당첨! 게임 종료");
                            gameStarted = false;
                        }else {
                            broadcastMessage(message);
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private  static void broadcastMessage(String message)throws IOException{
        for(SelectionKey key : selector.keys()){
            if(key.isValid() && key.channel() instanceof SocketChannel){
                SocketChannel clientChannel = (SocketChannel) key.channel();
                clientChannel.write(ByteBuffer.wrap(message.getBytes()));
            }
        }
    }
}
