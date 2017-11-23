package rocknb.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import rocknb.net.Net;


public class StartupFXMLController implements Initializable {

    @FXML
    private Button playButton;
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private TextField ipField2;
    @FXML
    private TextField portField2;
    @FXML
    private AnchorPane rootPane;

    @FXML
    private void play(ActionEvent ae) throws IOException{
        //set ip for host
        Net n = new Net();
        if(!(ipField.getText().equals(""))){
            n.serverIp = ipField.getText();
        }
        if(!(portField.getText().equals(""))){
            n.serverPort = Integer.parseInt(portField.getText());
        }    
        if(!(ipField2.getText().equals(""))){
            n.serverIp2 = ipField2.getText();
        }
        if(!(portField2.getText().equals(""))){
            n.serverPort2 = Integer.parseInt(portField2.getText());
        }    
    
        //switch to other view
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/rocknb/view/RPSFXML.fxml"));
        rootPane.getChildren().setAll(pane);

    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }    
    
}
