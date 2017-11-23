package rocknb.model;

public class Player {
    
    public int score = 0;
    private int lastScore = 0;
    private boolean hasPicked = false;
    private String choice;
    private String lastChoice = ""; //set to "", so it doesn't print out null on round 1.
    
    
    public Player(){
    }
    
    public boolean getHasPicked(){
        return hasPicked;
    }
        public void setHasPicked(boolean val){
        hasPicked = val;
    }
    public String getChoice(){
        return this.choice;
    }
    
    public String getLastChoice(){
        return this.lastChoice;
    }
    
    public void registerChoice(String pick){
        this.hasPicked = true;
        this.choice = pick;
    }
    
    public void resetRound(){
        this.lastChoice = choice;
        this.hasPicked = false;
        this.choice = null;
    }
    
    public int getScore(){
        return this.score;
    }
    
    public void addScore(int added){
        this.lastScore = added;
        this.score = this.score+added;
    }
    
    public int getLastScore(){
        return this.lastScore;
    }
    
    
    
}
