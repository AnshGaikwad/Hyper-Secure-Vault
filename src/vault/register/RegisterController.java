package vault.register;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class RegisterController  implements Initializable {

    // JavaFx Components
    @FXML
    private ImageView registerImage;

    @FXML
    private TextField registerFirstNameTextField, registerLastNameTextField, registerEmailTextField;

    @FXML
    private PasswordField registerPasswordField, registerConfirmPasswordField;

    @FXML
    private Label registerMessageLabel;

    @FXML
    private Button registerBackButton;

    // Initializable => Executes on start
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        File userFile = new File("images/user.jpg");
        Image userImage = new Image(userFile.toURI().toString());
        registerImage.setImage(userImage);
    }

    // When register button is clicked
    // Validates input and goes to register user
    public void registerRegisterButtonOnAction()
    {
        // Validating input
        if(registerFirstNameTextField.getText().isBlank())
            registerMessageLabel.setText("First name isn't entered");
        else if(registerLastNameTextField.getText().isBlank())
            registerMessageLabel.setText("Last name isn't entered");
        else if(registerEmailTextField.getText().isBlank())
            registerMessageLabel.setText("Email isn't entered");
        else if(registerPasswordField.getText().isBlank())
            registerMessageLabel.setText("Password isn't entered");
        else if(registerConfirmPasswordField.getText().isBlank())
            registerMessageLabel.setText("Password isn't entered");
        else if(!registerPasswordField.getText().equals(registerConfirmPasswordField.getText()))
            registerMessageLabel.setText("Password do not match");
        else
        {
            registerMessageLabel.setText("Registering...");

            // Validated, Registering!
            registerUser();
        }
    }

    // Function to register user using postgres database
    // Sends a POST request to backend service API with all details
    private void registerUser()
    {
        try
        {
            URL url = new URL("http://localhost:8080/api/users/register");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            String jsonInputString = "{\"firstName\":\"" + registerFirstNameTextField.getText() + "\",\"lastName\":\"" +registerLastNameTextField.getText()+ "\",\"email\":\"" +registerEmailTextField.getText()+ "\",\"password\":\"" +registerPasswordField.getText()+ "\"}";

            try(OutputStream os = con.getOutputStream())
            {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)))
            {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null)
                {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }
            goToHome();
        }
        catch(Exception e)
        {
            e.getStackTrace();
            e.getCause();
        }
    }

    // Go to home page after registration
    private void goToHome()
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getResource("../home/home.fxml"));
            Stage registerStage = new Stage();
            registerStage.initStyle(StageStyle.UNDECORATED);
            registerStage.setScene(new Scene(root, 900, 600));
            registerStage.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            e.getCause();
        }
    }

    // If back button pressed
    public void registerBackButtonOnAction()
    {
        Stage stage = (Stage) registerBackButton.getScene().getWindow();
        stage.close();
    }
}
