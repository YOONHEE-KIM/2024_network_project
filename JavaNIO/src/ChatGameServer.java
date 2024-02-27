import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class ChatGameServer {
    private static final int PORT = 8080;
    private static final int MAX_PLAYERS = 4;
    private static final String SERVER_HOST = "localhost";

    private Selector selector; //Selector 선언해주기
    private ByteBuffer buffer = ByteBuffer.allocate(1024); //버퍼 할당
    private List<SocketChannel> players = new ArrayList<>(); //클라이언트 소켓 채널 리스트
    private int currentPlayerIndex = 0;
    private int currentNumber = 0;

    private boolean isGameRunning = false; // 게임 진행 여부

    public ChatGameServer() {
        try {
            selector = Selector.open(); //.open() -> 생성
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(SERVER_HOST, PORT));
            serverSocketChannel.configureBlocking(false); //논블로킹
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //Accept 이벤트 Selector에 등록
            System.out.println("서버 연결 완료");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                int readyChannels = selector.select(); // 준비된 채널 수 확인
                //준비된 채널 없으면 대기
                if (readyChannels == 0) {
                    continue;
                }
                //키  가져옴
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                //선택 키들 반복문(이벤트 처리하는 기능)
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        handleAccept(key); // Accept 이벤트 처리
                    }

                    if (key.isReadable()) {
                        if (isGameRunning) {
                            handleGameRead(key); // 게임 중인 경우 읽기 이벤트 처리
                        } else {
                            handleChatRead(key); // 게임 중이 아닌 경우 채팅 읽기 이벤트 처리
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        players.add(clientChannel); //클라이언트 추가
        System.out.println("새로운 플레이어 접속: " + clientChannel.getRemoteAddress());
        if (players.size() == MAX_PLAYERS) {
            startGame(); //최대플레이어 4명 되면 시작
        }
    }

    private void handleGameRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        buffer.clear();
        int bytesRead = clientChannel.read(buffer); // 클라이언트 데이터 읽기
        if (bytesRead == -1) {
            clientChannel.close();
            return;
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String message = new String(bytes).trim();
        System.out.println("게임 메시지: " + message);

        try {
            int num = Integer.parseInt(message);
            if (num >= 1 && num <= 3) {
                currentNumber += num;
                broadcast("현재 값: " + currentNumber);
                if (currentNumber >= 31) {
                    broadcast("Game Over! " + clientChannel.getRemoteAddress() + " 당첨!");
                    endGame();
                } else {
                    nextTurn();
                }
            } else {
                clientChannel.write(ByteBuffer.wrap("1부터 3 사이의 수 입력".getBytes()));
            }
        } catch (NumberFormatException e) {
            clientChannel.write(ByteBuffer.wrap("숫자를 입력하세요".getBytes()));
        }
    }


    private void handleChatRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        buffer.clear();
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            clientChannel.close();
            return;
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String message = new String(bytes).trim();
        System.out.println("Received chat message from client: " + message);
        broadcastChat("[" + clientChannel.getRemoteAddress() + "]: " + message);
    }

    private void startGame() throws IOException {
        isGameRunning = true;
        Collections.shuffle(players, new Random());
        broadcast("게임시작! 게임 순서:");
        for (int i = 0; i < players.size(); i++) {
            SocketChannel player = players.get(i);
            player.write(ByteBuffer.wrap(("Player " + (i + 1) + "\n").getBytes()));
        }
        nextTurn();
    }

    private void broadcast(String message) throws IOException {
        for (SocketChannel player : players) {
            player.write(ByteBuffer.wrap(message.getBytes()));
        }
    }

    private void broadcastChat(String message) throws IOException {
        for (SocketChannel player : players) {
            player.write(ByteBuffer.wrap(message.getBytes()));
        }
    }

    private void nextTurn() throws IOException {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        SocketChannel currentPlayer = players.get(currentPlayerIndex);
        currentPlayer.write(ByteBuffer.wrap(("입력할 차례입니다 Player " + (currentPlayerIndex + 1) + "!\n").getBytes()));
        broadcast("Player " + (currentPlayerIndex + 1) + "의 차례입니다. 현재 값은: " + currentNumber);
    }

    private void endGame() {
        isGameRunning = false;
        players.clear();
        currentPlayerIndex = 0;
        currentNumber = 0;
    }

    public static void main(String[] args) {
        ChatGameServer server = new ChatGameServer();
        server.run();
    }
}
