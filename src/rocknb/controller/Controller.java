/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocknb.controller;

import rocknb.net.Net;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import rocknb.model.Model;
import rocknb.Updater;

public class Controller{
    Model model;
    Net net;
    Updater updater;
    
    public Controller(){
        
        model = new Model(2);
        
        net = new Net();
        giveNetAccess();
        net.bind();    
        net.start(); // Start Receive
        
    }
    
    public void restartGame(int players){
        model = new Model(players);
        updater.newRound();
    }
    public void startNet() throws IOException{
        net.initSocketChannel();
        
    }
    
    private void giveNetAccess(){
        net.setMyController(this);
    }
    
    //Interface to tell FXMLController when to update scores etc.
    public void setUpdater(Updater c){
        this.updater =c;
    }
    
    public void interpretMessage(String message, int idSender){
        //format of messages
        //
        //user messages
        //ask server to connect to players - connect myip myport 
        //send your choice to players - pick playerid choice 
        //
        //server/matchmaker messages
        //server setup a game between 2-3 players - setup playercount yourid player1ip player1port player2ip player2port (player3ip player3port)
        //id is given to player, assigned by matchmaker
        String[] arr = message.split(" ");   
        if(arr[0].equals("otherplayer")){
            System.out.println("got add otherplayer request");
                String ip = arr[1];
                int port = Integer.parseInt(arr[2]);
                //int playerCount = Integer.parseInt(arr[1]);
                net.addPlayer(ip, port);
                restartGame(3);
        }else{
            
        
        CompletableFuture.runAsync(() -> {
        
        //String[] arr = message.split(" ");    
        //int messageLength = arr.length;
        
        //means a already connected player sends a pick message
        if(arr[0].equals("pick")){
            //int id = Integer.parseInt(arr[1]);
            String choice = arr[1];
            recordInput(idSender, choice);
        
        //means server setup message
        }else if(arr[0].equals("setup")){
            
            int playerCount = Integer.parseInt(arr[1]);
            
            InetSocketAddress[] connections = new InetSocketAddress[playerCount];
            
            connections[0] = new InetSocketAddress(arr[3], Integer.parseInt(arr[4]));
            connections[1] = new InetSocketAddress(arr[5], Integer.parseInt(arr[6]));
            if(playerCount==3){
                connections[2] = new InetSocketAddress(arr[7], Integer.parseInt(arr[8]));
            }
                //sends array with addresses to players, including your own data, and your id, which tells which index is you.
                model.createPlayers(connections, Integer.parseInt(arr[2]));
                
                //tells FXMLController how many players there are and your id.
                updater.connected(playerCount, model.getMyId());
            }
        }); //end CompleteableFuture
        }
    }
    
    public void updateConnections(int nr){
        restartGame(nr+1);
    }
    
    //connect to server who then connects you to queued up players.
    public void connectToPlayers() throws IOException{
        //String message = "connect " +net.getMyIp() +" " +net.getMyPort();
        System.out.println("connecting");
        //net.send(net.server, message);
        net.connecting();
        //System.out.println("sent request");
        
    }
    
    public int getPlayerCount(){
        return model.getPlayerCount();
    }
    
    public void close(){
        
        net.stop();
    }
    
    public int getScore(int id){
        
    return model.getPlayerScore(id);
    }
    
    public int getLastScore(int id){
        
    return model.getLastPlayerScore(id);
    }
    
    public String getLastChoice(int id){
        
    return model.getPlayerLastChoice(id);
    }
    
    
    //remove
    public void addScore(int id, int value){
        model.addScore(id, value);
    }
    
    public void sendInput() throws IOException{
        
        System.out.println("my port " +net.getMyPort());
                net.send("iiiiiiiii");
           
        
    }
    
    public void sendInput(String choice) throws IOException{
        //System.out.println("my id " +model.getMyId());
        recordInput(model.getMyId(), choice);
        
        String message = "pick " +choice;
        net.send(message);
        
        
        /*
                recordInput(model.getMyId(), choice);
        if(choice.equals("paper")){
            net.addPlayer=true;
            net.activateSelector();
        }else if(choice.equals("scissor")){
            net.shareInfo();
        }else{
        String message = "pick " +choice;
        net.send(message);
        }
        */
        
        //message format - pick playerid choice 
        //String message = "pick " +model.getMyId() +" " +choice;
        //for(int i = 0; i<model.getPlayerCount(); i++){
        //    if(i!=model.getMyId()){
        //        net.send(model.getSocketAddress(i), message);
        //    }
        //}
        //recordInput(model.getMyId(), choice);
        
    }
    
    //stores what you or another player have inputted.
    private void recordInput(int id, String choice){
        
        //System.out.println("id & choice" +id +" " +choice);
        model.registerChoice(id, choice);

        if(haveAllInputs()){
            countScore();
        }
    }
    
    //count and apply scores of a round, then call to reset and call FXMLController to update.
    public void countScore(){
        //rock = 1 paper = 2 scissor = 3      but 1 is supposed to be greater than 3.
        int players = model.getPlayerCount();
        int[] pick = new int[players];
        int[] points = new int[players];
        
        for(int i = 0; i<players; i++){
            System.out.println(pick[i]);
            if(model.getPlayerChoice(i).equals("rock")){
                pick[i] = 1;
            }else if(model.getPlayerChoice(i).equals("paper")){
                pick[i] = 2;
            }else{
                pick[i] = 3;
            }
        }
        
        if(players==2){
            
            if(pick[0]==pick[1]){
                points[0] = 0;
                points[1] = 0;
            }else if(pick[0]>pick[1] && !(pick[0]==3 && pick[1]==1) || (pick[0]<pick[1] && (pick[0]==1 && pick[1]==3))){
                //either pick 0 is larger and it's not true that pick 0 is 3 and pick 1 is 1.
                //or pick 0 is lower, and it's true that both pick 0 = 1, and pick 1 = 3
                //this means that pick 0 wins. Either by 3vs2, 2vs1 or 1vs3.
                points[0] = 1;  
                points[1] = 0;
            }else{
                //all cases of a tie has been eliminated
                //all cases where pick 0 wins have also been eliminated
                points[0] = 0;  
                points[1] = 1; 
            }
            
  
        }else if(players == 3){
            
            
            if(pick[0]==pick[1]){
                if(pick[0]==pick[2]){
                    //all is the same
                points[0] = 0;  
                points[1] = 0;  
                points[2] = 0; 
                }else if(pick[0]>pick[2] && !(pick[0]==3 && pick[2]==1) || (pick[0]==1 && pick[2]==3)){
                    //pick 0 is larger AND the 3 vs 1 case is not true, or pick 0 is 1 and pick 2 is 3, means that pick 0 wins.
                    //since pick 0 and pick 1 is the same, they both get 1 point.
                points[0] = 1;  
                points[1] = 1;  
                points[2] = 0;                    
                    
                }else{
                    //pick 2 wins, and they get 2 points since they beat both pick 0 and 1.
                points[0] = 0;  
                points[1] = 0;  
                points[2] = 2; 
                }
            }else if(pick[0]==pick[2]){//pick 1 is different, others are the same.
                   if(pick[0]>pick[1] && !(pick[0]==3 && pick[1]==1) || (pick[0]==1 && pick[1]==3)){
                    //pick 0 is larger AND the 3 vs 1 case is not true, means that pick 0 wins.
                    //since pick 0 and pick 2 is the same, they both get 1 point.
                points[0] = 1;  
                points[1] = 0;  
                points[2] = 1;  
                   }else{
                points[0] = 0;  
                points[1] = 2;  
                points[2] = 0; 
                   }
                
            }else if(pick[1]==pick[2]){//pick 0 is different, others are the same.
                if(pick[0]>pick[1] && !(pick[0]==3 && pick[1]==1) || (pick[0]==1 && pick[1]==3)){
                points[0] = 2;  
                points[1] = 0;  
                points[2] = 0;               
                }else{
                points[0] = 0;  
                points[1] = 1;  
                points[2] = 1; 
                }
            }else{//all are unequal
                points[0] = 1;  
                points[1] = 1;  
                points[2] = 1; 
            }
            
            
        }
        
        for(int i = 0; i<players; i++){
        model.addScore(i, points[i]);       
        }     
        reset();
        
    }
    
    private void reset(){
        model.resetPlayers();
        updater.newRound();
    }
    
    public boolean haveAllInputs(){
        boolean all = true;
        //assumed to have all inputs. If you find anyone without inputs, it's set to false. Then returned.
        for(int i = 0; i<model.getPlayerCount(); i++){
            
            if(!(model.hasPicked(i))){
                
                all = false;
                
            }
        }
        
        return all;
    }
   

    
}
