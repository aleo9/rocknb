/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocknb.net;

import java.nio.channels.SocketChannel;

   
public class Client{
    private SocketChannel clientChannel;

    public Client (SocketChannel channel){
    this.clientChannel = channel;
    }

    public SocketChannel getChannel(){
    return clientChannel;
    }
}


