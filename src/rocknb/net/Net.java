package rocknb.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import rocknb.controller.Controller;

public class Net implements Runnable
{
    private final ByteBuffer receivedMessages = ByteBuffer.allocateDirect(8192);
        private SocketChannel socketChannel;
        private SocketChannel socketChannel2;
    
        private Boolean connected;
        private Boolean connected2;
        private Selector selector;
        private boolean timeToSend = false;
        private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
        private final Queue<ByteBuffer> messagesToSend2 = new ArrayDeque<>();
    
        private ServerSocketChannel listeningSocketChannel;
    
        private int totalConnections = 0;
        private boolean addPlayer = false;
        private int hostedPlayers = 0;
        private boolean setup = true;
    
	private DatagramSocket socket;
	
	private Controller myController;
        public static int serverPort = 1010;
        public static String serverIp = "localhost";
        public static String serverIp2 = "localhost";
        public static int serverPort2 = 1011;
        
        private String myIp = "localhost";
        private InetAddress inet;
        private InetSocketAddress server;
        private InetSocketAddress server2;
        private int userPort = 1000;
        
        public int getConnections(){
            return totalConnections;
        }
        
        public void setMyController(Controller c){
            this.myController = c;
        }
        
        public int getMyPort(){
            return userPort;
        }
        
        public String getMyIp(){
            return myIp;
        }
        
        public DatagramSocket getSocket(){
        return socket;
        }
        
	public int bind(){
            
            server = new InetSocketAddress(serverIp, serverPort);

            try{
            inet = InetAddress.getLocalHost();
            myIp = inet.getHostAddress();
            }catch(UnknownHostException e){
            }
            
            int finalPort = userPort;
            
            boolean foundPort = false;
            int counter = 0;
            while(!foundPort && counter<20){//counter that tests 20 ports in order from 1000.
                
                try{
		this.socket = new DatagramSocket(userPort);
                finalPort=userPort;
                foundPort=true;
                }            
                catch (SocketException e){
         
		}
                userPort++;
                counter++; 
                
            }
                userPort = finalPort;
                return finalPort;
  
            
	}
	
	public void start(){
            
		Thread thread = new Thread(this);
		thread.start();
                
	}
	
	public void stop(){
		setup = false;
		socket.close();
	}
	@Override
	public void run(){
            try {
                initSocketChannel();
                initListeningSocketChannel();
            }catch(IOException e){
            }
            
            while(setup){
                try{
                
                while (connected || !messagesToSend.isEmpty() || !messagesToSend2.isEmpty()) {
                    if (addPlayer){
                        initSocketChannel2();
                        addPlayer = false;
                    }
                
                    if (timeToSend) {
                        
                        socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                        timeToSend = false;
                }
                
                selector.select();
                
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        acceptHandler(key);
                    }else if (key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        sendMessage(key);
                    }else{
                    }
                }
            }
        }catch (Exception e) {
            
            }
        }
            
	}
        
        private void acceptHandler(SelectionKey key) throws IOException{
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            hostedPlayers++;
            
            if(totalConnections==1){
            socketChannel2 = serverSocketChannel.accept();
            
            totalConnections++;
            socketChannel2.configureBlocking(false);
            socketChannel2.register(selector, SelectionKey.OP_READ);
        
            //share other contacted player
            if(hostedPlayers==2){
                shareContactInfo();
            
            }
            
            //updating the amount of connections so the controller knows the amount of players
            myController.updateConnections(2);
            }else{
            socketChannel = serverSocketChannel.accept();
            totalConnections++;
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);          
            }
                
        }
     

        public void shareContactInfo(){
            
                String message = "otherplayer ";
                message +=serverIp;
                message +=" ";
                message +=serverPort;
                
                messagesToSend2.add(ByteBuffer.wrap(message.getBytes()));
                System.out.println("message added to bytebuffer 2 " +message);
                
                
                timeToSend = true;
                //selector.wakeup();  
                
	}
        
        //adds message to bytebuffers
        public void send(String message){
            
                messagesToSend.add(ByteBuffer.wrap(message.getBytes()));
                System.out.println("message added to bytebuffer " +message);
                if(totalConnections==2){
                messagesToSend2.add(ByteBuffer.wrap(message.getBytes()));
                System.out.println("message added to bytebuffer 2 " +message);
            }
                
                timeToSend = true;
                activateSelector();
                
	}
        
        //not in use
        public void connecting() throws IOException{
                
                timeToSend = true;
                activateSelector();
                
	}
       
        public void initSocketChannel() throws IOException{
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(server);
            connected = true;
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
        
        public void initSocketChannel2() throws IOException{
            
            socketChannel2 = SocketChannel.open();
            socketChannel2.configureBlocking(false);
            socketChannel2.connect(server2);
            connected2 = true;
            socketChannel2.register(selector, SelectionKey.OP_CONNECT);
            
        }
        
        private void initListeningSocketChannel() throws IOException {
            listeningSocketChannel = ServerSocketChannel.open();
            listeningSocketChannel.configureBlocking(false);
        
            listeningSocketChannel.bind(new InetSocketAddress(userPort+10));
            listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
        
        
       
            private void completeConnection(SelectionKey key) throws IOException {
                if(totalConnections==1){
                socketChannel2.finishConnect();
                totalConnections++;
                key.interestOps(SelectionKey.OP_READ);
                }else{
                    socketChannel.finishConnect();
                    totalConnections++;
                    key.interestOps(SelectionKey.OP_READ);
           
                    //setup = false;
                }
    }
   
        private void sendMessage(SelectionKey key) throws IOException {
            
            ByteBuffer msg;
            synchronized (messagesToSend) {
                while ((msg = messagesToSend.peek()) != null) {
                                
                    socketChannel.write(msg);

                    if (msg.hasRemaining()) {
                    return;
                    }
                
                    messagesToSend.remove();
                }

            }
        
            if(totalConnections==2){
        
            synchronized (messagesToSend2) {
                while ((msg = messagesToSend2.peek()) != null) {
                                
                    socketChannel2.write(msg);

                if (msg.hasRemaining()) {
                    return;
                }
                
                messagesToSend2.remove();
                }
            
            }
        
            }
            //timeToSend = false;
            key.interestOps(SelectionKey.OP_READ);
            
        
        }        
      
        public void activateSelector(){
            selector.wakeup();
        }
                
                
        private void read(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            int idSender = 0;
            if(channel==socketChannel){
                idSender = 1;
            }else if(channel==socketChannel2){
                idSender = 2;
            }
        
            receivedMessages.clear();
            int numOfReadBytes = channel.read(receivedMessages);
            
            if (numOfReadBytes == -1) {
            throw new IOException();
            }
            
                String message = extractFromBuffer();
                myController.interpretMessage(message, idSender);
                activateSelector();
    }               
        private String extractFromBuffer() {
            receivedMessages.flip();
            byte[] bytes = new byte[receivedMessages.remaining()];
            receivedMessages.get(bytes);
            return new String(bytes);

        } 
        
        
    public void addPlayer(String ip, int port){
        serverIp2 = ip;
        serverPort2 = port;
        server2 = new InetSocketAddress(ip, port);
        addPlayer = true;
        activateSelector();

    }    
   
}