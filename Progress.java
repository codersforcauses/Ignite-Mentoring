import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author Coders For Causes - Jeremiah Pinto (21545883)
 */
public class Progress extends Application 
{		
	@Override
	public void start(final Stage inputStage) 
	{      	
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Ignite Mentoring");
		alert.setContentText("The application is already running");
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("icon.jpeg").toString()));
		alert.showAndWait();
		System.exit(0);
	}	
}
