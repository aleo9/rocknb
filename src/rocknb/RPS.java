/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocknb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import static javafx.application.Application.launch;
import javafx.application.Platform;

public class RPS extends Application {
 
    Scene intro;
    Scene scene;
    Stage stage;
    
    @Override
    public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(true);
        stage.setOnCloseRequest((ae) -> {
        Platform.exit();
        System.exit(0);
        });
        
        Parent root = FXMLLoader.load(getClass().getResource("view/startupFXML.fxml"));
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
  
    }
    
    @Override
    public void stop(){
    }
    public static void main(String[] args) throws IOException{

        launch(args);
        
        
    }
    
}
