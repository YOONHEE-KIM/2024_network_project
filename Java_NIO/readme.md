# 프로젝트 3번

- nio를 사용하여 epoll, iocp의 개념을 사용해서 최대한 구현
-  nio를 사용한 비동기 MP서버 셀렉트 이상 코드 하나
- 비동기 또는 논블록 클라이언트
- 스트림 기반(기존 소켓) 코드 둘

## 자바 NIO란
Java 4부터 새로운 입출력이라는 뜻에서 java.nio 패키지가 포함되었다고 함.   

자바 IO와 자바 NIO의 차이
-----
|방식|IO|NIO|
|---|---|---|
|입출력|스트림|채널|
|버퍼|논버퍼|버퍼|
|비동기|지원 X|지원 O|
|블로킹/논블로킹|블로킹만|둘 다|

channel
=======
소켓을 통해 non-blocking read를 할 수 있도록 지원하는 connection. 
채널은 읽기, 쓰기 둘 다 가능한 양방향식 입출력 클래스다.  
(읽기, 쓰기 하나씩 쓸 수 있는 스트림은 단방향식)   
-> 네이티브 IO, Scatter / Gather 구현으로 효율적인 IO처리가 가능   
   * 시스템 콜 수 줄이기, 모아서 처리하기
파일채널 소켓채널이 있는데 파일채널은 논블로킹이 불가능하다고 함.   

그런 이유로 소켓채널을 사용한다. 그런데 소켓채널도 기본적으로는 블로킹 모드로 설정이 되기 때문에 논블로킹 모드로는 따로 바꿔줘야한다.

```
 SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("host ip", port));
socketChannel.configureBlocking(false);
```

buffer
====
커널에 의해 관리되는 시스템 메모리를 직접 사용할 수 있는 채널에 의해 직접 read 되거나 write 될 수 있는 배열과 같은 객체

Selector
====
네트워크 프로그래밍의 효율을 높이기 위한 것.(?)      
클라이언트 하나당 쓰레드 하나를 생성해서 처리하기 때문에 쓰레드가 많이 생성될수록 급격한 성능 저하를 가졌던 단점을 개선하는 Reacctor 패턴의 구현체 
-> 어느 channel set이 IO event를 가지고 있는지 알려준다.

- Selector.open() Selector 생성
- Selector.select() 는 I/O 이벤트가 발생한 채널 set을 return. return할 채널이 없다면 계속 block 됨.
- Selector.wakeup() block된 것을 바로 return시켜줌.
- Selector.selectedKey() Selection Key를 return해줌. Reactoer는 이 Selection Key를 보고 어떤 handler로 넘겨줄지 결정함.
 
  ```
  int interestSet = selectionKey.interestOps();

    boolean isInterestedInAccept  = SelectionKey.OP_ACCEPT  == (interests & SelectionKey.OP_ACCEPT);
    boolean isInterestedInConnect = SelectionKey.OP_CONNECT == (interests & SelectionKey.OP_CONNECT);
    boolean isInterestedInRead    = SelectionKey.OP_READ    == (interests & SelectionKey.OP_READ);
    boolean isInterestedInWrite   = SelectionKey.OP_WRITE   == (interests & SelectionKey.OP_WRITE);



IO와 NIO 각각의 장단점
=====
NIO는 다수의 연결이나 파일들을 비동기/ 논블로킹 처리할 수 있어서 많은 스레드 생성을 피하고, 스레드를 효과적으로 재사용한다는 장점이있다.   

-> 연결 수가 많고 하나의 입출력 처리 작업이 오래걸리지 않는 경우 사용하면 좋다.

IO는 많은 데이터를 처리하는 경우 좋다. NIO는 모든 입출력 작업에 버퍼를 무조건 사용해야 해서, 버퍼 할당 크기가 오히려 문제가 된다. (즉시 처리하는 IO보다 성능 저하가 있을 수 있다.)   

-> 연결 클라이언트 수가 적고 전송되는 데이터가 대용량이면서 순차적으로 처리될 필요성이 있는 경우 IO로 구현하는 것이 좋다.




-------------------
자료 출처 및 참고자료

<https://brunch.co.kr/@myner/47>

<https://brewagebear.github.io/fundamental-nio-and-io-models/>

<https://jenkov.com/tutorials/java-nio/buffers.html>

<https://jenkov.com/tutorials/java-nio/channel-to-channel-transfers.html#transferto>

------

## 배운 점
(자랑은 아니지만) c++은 고사하고 c 조차 초면이었지만 네트워크 계층을 배우고 직접 실습 해볼 수 있어서 많이 배운 것 같습니다. 물론 채팅서버와 에코서버를 이제야 구분할 수 있게 되었지만 꾸준히 공부해 나가기 위해 좋은 기회가 되었다고 생각합니다. 

(에코서버가 만들어진 걸 보고도 이게 채팅서버인가보다 진짜 착각했었던 인간)