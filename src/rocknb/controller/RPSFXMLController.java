package rocknb.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.concurrent.*;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import rocknb.Updater;

public class RPSFXMLController implements Initializable, Updater {
    
    private static Controller controller;
    @FXML
    private AnchorPane rootPane;
    
    @FXML
    private Button rockButton;
    @FXML
    private Button scissorButton;     
    @FXML
    private Button paperButton;  
    @FXML
    private Button connectButton;
    
    @FXML
    private Label p1score;
    @FXML
    public Label p2score;
    @FXML
    private Label p3score;
    
    @FXML
    private Label p1last;
    @FXML
    private Label p2last;
    @FXML
    private Label p3last;
    
    @FXML 
    private Text p1name;
    @FXML 
    private Text p2name;
    @FXML 
    private Text p3name;
    

    @FXML
    private void connect(ActionEvent ae){
    connectButton.setText("waiting");
    connectButton.setDisable(true);
    new ActionService("connect").start();
    }
    
    @FXML
    private void addHandlerRock(ActionEvent ae){
        disableButtons(true);
        new ActionService("rock").start();
    }
    
    @FXML
    private void addHandlerPaper(ActionEvent ae) throws IOException{
        disableButtons(true);
        //controller.startNet();
        new ActionService("paper").start();
    }

    @FXML
    private void addHandlerScissor(ActionEvent ae){
        disableButtons(true);
        new ActionService("scissor").start();
    }
    
    public void connectUpdaterInterface(Controller c){
        c.setUpdater(this);
    }
    
    //run this when score has been counted and it's time for a new round.
    private void activateButtons(){
        disableButtons(false);
    }
    
    private void updateScore(){
        
        Platform.runLater(new Runnable() {
        @Override public void run() {
              
            
            p1score.setText(Integer.toString(controller.getScore(0)));
            p2score.setText(Integer.toString(controller.getScore(1)));
            
            p1last.setText(Integer.toString(controller.getLastScore(0)) +" " +controller.getLastChoice(0));
            p2last.setText(Integer.toString(controller.getLastScore(1)) +" " +controller.getLastChoice(1));
            if(controller.getPlayerCount()==3){
            p3score.setText(Integer.toString(controller.getScore(2)));
            p3last.setText(Integer.toString(controller.getLastScore(2)) +" " +controller.getLastChoice(2));
                }
          
            }
    });
   
    }

    private void disableButtons(boolean disabled){
        rockButton.setDisable(disabled);
        paperButton.setDisable(disabled);
        scissorButton.setDisable(disabled);
        
    }
    
    
   
    private static class ActionService extends Service<Void> {
        String choice;
        
        private ActionService(String input){
                choice = input;
            }

        @Override
        protected Task<Void> createTask(){
            
            return new Task<Void>(){
                
                @Override
                protected Void call() throws InterruptedException{

                    if(choice.equals("connect")){
                        try{ 
                            //System.out.println("task connect");
                        controller.connectToPlayers();
                        }catch(IOException e){
                        System.out.println("failed connecting");
                        }
                    }else{
                        
                        //send "choice" to other player.
                        try {
                        controller.sendInput(choice);
                        
                        }
                        catch(IOException e) {
                        System.out.println("failed sending input");
                        }
                    
                    }
                    return null;
                }
            };
        }
        
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb){
        
        p3score.setDisable(true);
        p3last.setDisable(true);
        try {
        controller = new Controller();
        connectUpdaterInterface(controller);
        //disableButtons(true);
        
        }catch(Exception e) {
             
                    }
        //new ActionService("connect").start();
        
    }   

    @Override
    public void connected(int players, int id){
        //implement view with right amount of players. Change name from player 1-3 to you
        /*
        if(id==0){
            p1name.setText("Me");
        }else if(id==1){
            p2name.setText("Me");
        }else if(id==2){
            p3name.setText("Me");
        }else{
            
        }
          */  
            if(players==3){
                p3name.setText("Player 3");
                p3score.setDisable(false);
                p3last.setDisable(false);
            }
            newRound();
    }
    //@FXML
    //public void exitApplication(ActionEvent event) {
    //Platform.exit();
    //}
    
    @Override
    public void newRound(){
        
    activateButtons();
    updateScore();   
            
    }
    
}