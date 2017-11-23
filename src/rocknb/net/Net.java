/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocknb.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import rocknb.controller.Controller;

public class Net implements Runnable
{
    private final ByteBuffer receivedMessages = ByteBuffer.allocateDirect(8192);
    SocketChannel socketChannel;
    SocketChannel socketChannel2;
    //SocketChannel[] socketChannels = new SocketChannel[2];
    Boolean connected;
    Boolean connected2;
    Selector selector;
    private boolean timeToSend = false;
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private final Queue<ByteBuffer> messagesToSend2 = new ArrayDeque<>();
    //private final List<CommunicationListener> listeners = new ArrayList<>();
    private ServerSocketChannel listeningSocketChannel;
    SocketChannel clientChannel;
    boolean waiting = true;
    public int totalConnections = 0;
    public boolean addPlayer = false;
    private int hostedPlayers = 0;
    
	private DatagramSocket socket;
	private boolean running;
	Controller myController;
        public static int serverPort = 1010;
        public static String serverIp = "localhost";
        public static String serverIp2 = "localhost";
        public static int serverPort2 = 1011;
        
        private String myIp = "localhost";
        private InetAddress inet;
        public InetSocketAddress server;
        public InetSocketAddress server2;
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
            System.out.println("server " +serverIp +" " +serverPort);
            
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
		running = false;
		socket.close();
	}
	@Override
	public void run(){
                    //System.out.println("run");
            try {
                initSocketChannel();
                initListeningSocketChannel();
                        //System.out.println(connected);
                while (connected || !messagesToSend.isEmpty() || !messagesToSend2.isEmpty()) {
                    if (addPlayer){
                        //System.out.println("another player, entering connectTest()");
                        initSocketChannel2();
                        addPlayer = false;
                    }
                
                    if (timeToSend) {
                        System.out.println("if time to send true");
                        //System.out.println("socketchannel connected " +socketChannel.isConnected());
                        System.out.println("socketchannel connected " +socketChannel.isConnected());
                        //System.out.println("socketchannel 2 connected " +socketChannel2.isConnected());
                    //
                        
                            socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                        
                        //socketChannel2.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    
                        //socketChannel2.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    
                        //socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                        System.out.println("connected " +connected);
                        //System.out.println("connected 2" +connected2);
                        System.out.println("msg to send " +!messagesToSend.isEmpty());
                        System.out.println("msg to send 2 " +!messagesToSend2.isEmpty());
                    
                    
                    timeToSend = false;
                }
                //System.out.println("selector select");
                selector.select();
                
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        System.out.println("invalid key");
                        continue;
                    }
                    if (key.isAcceptable()) {
                        System.out.println("accepting");
                        acceptHandler(key);
                    }else if (key.isConnectable()) {
                        System.out.println("trying to connect " +server.getPort());
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        System.out.println("reading");
                        
                        read(key);
                        
                    } else if (key.isWritable()) {
                        
                        System.out.println("writing");
                        sendMessage(key);
                    }else{
                        System.out.println("hmmm");
                    }
                }
            }
        }catch (Exception e) {
            
            }
            
	}
        
        private void acceptHandler(SelectionKey key) throws IOException{
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            hostedPlayers++;
            
            if(totalConnections==1){
            socketChannel2 = serverSocketChannel.accept();
            //Socket socket2 = socketChannel2.socket();
            totalConnections++;
            System.out.println("total connections " +totalConnections);
            socketChannel2.configureBlocking(false);
            //System.out.println("new socket port " +socket2.getPort());
            socketChannel2.register(selector, SelectionKey.OP_READ);
            //socketChannel2.setOption(StandardSocketOptions.SO_LINGER, 5000);   
        
            if(hostedPlayers==2){
                System.out.println("share other contacted player");
                shareContactInfo();
            
            }
            
            myController.updateConnections(2);
            System.out.println("updating amount of connections");
            }else{
            socketChannel = serverSocketChannel.accept();
            //Socket socket = socketChannel.socket();
            totalConnections++;
            System.out.println("total connections " +totalConnections);
            socketChannel.configureBlocking(false);
            //System.out.println("new socket port " +socket.getPort());
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
                
                
                //if(totalConnections>1){
                //messagesToSend2.add(ByteBuffer.wrap(message.getBytes()));
                //System.out.println("message added to bytebuffer " +message);    
                //}
                timeToSend = true;
                //selector.wakeup();  
                
	}
        
        //adds message to bytebuffers and wakeup selector
        public void send(String message){
            
                messagesToSend.add(ByteBuffer.wrap(message.getBytes()));
                System.out.println("message added to bytebuffer " +message);
                if(totalConnections==2){
                messagesToSend2.add(ByteBuffer.wrap(message.getBytes()));
                System.out.println("message added to bytebuffer 2 " +message);
            }
                
                //if(totalConnections>1){
                //messagesToSend2.add(ByteBuffer.wrap(message.getBytes()));
                //System.out.println("message added to bytebuffer " +message);    
                //}
                timeToSend = true;
                //selector.wakeup(); 
                activateSelector();
                
	}
        
        
        public void connecting() throws IOException{
                
            //new Thread(this).start();
            
                //System.out.println("trying to connect to server");
                timeToSend = true;
                activateSelector();
                //selector.wakeup();
                
                
	}
       
        public void initSocketChannel() throws IOException{
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(server);
            connected = true;

            //connect to other player here?
            
            //socketChannel2 = SocketChannel.open();
            //socketChannel2.configureBlocking(false);
            //socketChannel2.connect(new InetSocketAddress(serverPort2));
            
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            //socketChannel2.register(selector, SelectionKey.OP_CONNECT);
            System.out.println("init");
        }
        
            private void initListeningSocketChannel() throws IOException {
                listeningSocketChannel = ServerSocketChannel.open();
                listeningSocketChannel.configureBlocking(false);
        
                listeningSocketChannel.bind(new InetSocketAddress(userPort+10));
                System.out.print("serversocket ");
                System.out.println(userPort+10);
                listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
        
        //public void connect(String host, int port){
        //    server = new InetSocketAddress(host, port);
        //    new Thread(this).start();
        //}
        
        //public void connect(){
        //    new Thread(this).start();
        //}
       
            private void completeConnection(SelectionKey key) throws IOException {
                if(totalConnections==1){
                socketChannel2.finishConnect();
                totalConnections++;
                key.interestOps(SelectionKey.OP_READ);
                System.out.println("reading cc 2nd connection");
                System.out.println("finished 2nd connect");
                }else{
                    socketChannel.finishConnect();
                    totalConnections++;
                    key.interestOps(SelectionKey.OP_READ);
                    System.out.println("reading cc");
                    System.out.println("finished connect");
                //try {
                    //InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                    //System.out.println("to string of socketchannel");
                    //System.out.println(socketChannel.getRemoteAddress().toString());
                    
                    //} catch (IOException e) {
                    
                    //}
                }
    }
   
        private void sendMessage(SelectionKey key) throws IOException {
            
            //timeToSend = true;
            ByteBuffer msg;
            //messagesToSend.add(ByteBuffer.wrap(message.getBytes()));
            System.out.println("sending to server");
            synchronized (messagesToSend) {
                while ((msg = messagesToSend.peek()) != null) {
                                
                    //System.out.println("sending this to c " +msg);
                    socketChannel.write(msg);

                    if (msg.hasRemaining()) {
                    return;
                    }
                
                    messagesToSend.remove();
                    System.out.println("removed message");
                }

            }
        
            if(totalConnections==2){
        
            System.out.println("sending to server 2");
            
            synchronized (messagesToSend2) {
                while ((msg = messagesToSend2.peek()) != null) {
                                

                    System.out.println("sending this to c2 " +msg);
                    socketChannel2.write(msg);

                if (msg.hasRemaining()) {
                    return;
                }
                
                messagesToSend2.remove();
                System.out.println("removed message 2");
            }
            
        }
        
        }
            //timeToSend = false;
            key.interestOps(SelectionKey.OP_READ);
            System.out.println("interest set to read");
            
        
    }        
    
        /*
        
                private void sendMessage(SelectionKey key) throws IOException {
            
                timeToSend = true;
            
        ByteBuffer msg;
        
        //messagesToSend.add(ByteBuffer.wrap(message.getBytes()));
        System.out.println("sending to server");
        synchronized (messagesToSend) {
            while ((msg = messagesToSend.peek()) != null) {
                                
                if(totalConnections==2){
                    messagesToSend2.add(messagesToSend.peek());
                    System.out.println("2 connections");
                        ByteBuffer copy = messagesToSend2.peek();
                        System.out.println("sending this to c2 " +copy);
                        socketChannel2.write(copy);
                        if (copy.hasRemaining()) {
                            
                        return;
                        }
                        messagesToSend2.remove();
                            System.out.println("removed message");
                }
                
                
                    System.out.println("sending this to c " +msg);
                    socketChannel.write(msg);

                if (msg.hasRemaining()) {
                    return;
                }
                
                messagesToSend.remove();
                System.out.println("removed message");
            }
            timeToSend = false;
            key.interestOps(SelectionKey.OP_READ);
            System.out.println("interest set to read");
        }
    }        
        
        */
        
        
        
        public void initSocketChannel2(){
            System.out.println("tries initSocketChannel2()");
            try{
             socketChannel2 = SocketChannel.open();
            socketChannel2.configureBlocking(false);
            System.out.println("port trying " +server2.getPort());
            //server2 = new InetSocketAddress(serverPort2);
            socketChannel2.connect(server2);
            connected2 = true;   
            
            socketChannel2.register(selector, SelectionKey.OP_CONNECT);
            System.out.println("init2");
            
            }catch(IOException e){
                System.out.println("exception when creating channel2");
            }
            
            
        }
                
        public void activateSelector(){
            selector.wakeup();
            System.out.println("selector wakeup");
        }
                
                
        private void read(SelectionKey key) throws IOException {
            System.out.println("incoming msg");
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
                System.out.println(message);
                System.out.println("sender's id " +idSender);
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
        System.out.println("addPlayer port " +port);
        server2 = new InetSocketAddress(ip, port);
        addPlayer = true;
        
        
        
        activateSelector();
        
        
        
    }    
        
      
   
        
}