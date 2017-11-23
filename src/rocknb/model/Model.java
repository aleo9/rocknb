package rocknb.model;

public class Model {
    
    private int playerCount;
    private int myId = 0;
    private Player[] player;
    
    public Model(){

        
    }
    
    public Model(int players){
        player = new Player[players];
        for(int i = 0; i<players; i++){
          player[i] = new Player();  
          System.out.println("player " +i +" " +player[i]);
        }
        playerCount = players;
        System.out.println("created players");
        

        
    }
    
    public void resetPlayers(){
        for(int i = 0; i<playerCount; i++){
            player[i].resetRound();
        }
        
    }
    
    public int getMyId(){
        return myId;
    }
    
    
    public void addScore(int id, int score){
        player[id].addScore(score);
         
    }
    
    public boolean hasPicked(int id){
        return player[id].getHasPicked();
    }
    
    public int getPlayerScore(int id){
        return player[id].getScore();
    }
    
    public int getLastPlayerScore(int id){
        return player[id].getLastScore();
    }
    
    public void setMyId(int id){
        this.myId = id;
    }
    
    public int getPlayerCount(){
        return playerCount;
    }
    
    public void setPlayerCount(int players){
        this.playerCount = players;
    }
    
    public String getPlayerChoice(int id){
        return player[id].getChoice();
    }
    
    public String getPlayerLastChoice(int id){
        return player[id].getLastChoice();
    }
    
    public void registerChoice(int id, String choice){
        player[id].setHasPicked(true);
        player[id].registerChoice(choice);
        for(int i = 0; i<playerCount; i++){
        }
        
    }
}
