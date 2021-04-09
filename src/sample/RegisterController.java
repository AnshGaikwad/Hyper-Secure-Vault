package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController  implements Initializable {

    @FXML
    private ImageView registerImage;

    @FXML
    private TextField registerFirstNameTextField;

    @FXML
    private TextField registerLastNameTextField;

    @FXML
    private TextField registerEmailTextField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private PasswordField registerConfirmPasswordField;

    @FXML
    private Label registerMessageLabel;

    @FXML
    private Button registerRegisterButton;

    @FXML
    private Button registerBackButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File userFile = new File("images/user.jpg");
        Image userImage = new Image(userFile.toURI().toString());
        registerImage.setImage(userImage);
    }

    public void registerRegisterButtonOnAction(ActionEvent event)
    {
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
        else if(registerPasswordField.getText().equals(registerConfirmPasswordField.getText()))
            registerMessageLabel.setText("Password do not match");
        else
            registerMessageLabel.setText("Registering...");
            registerUser();
    }

    private void registerUser()
    {
        try
        {
            URL url = new URL("http://127.0.0.1:8080/api/v1/registration/");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            String jsonInputString = "{\"firstName\":\"" + registerFirstNameTextField.getText() + "\",\"lastName\":\"" +registerLastNameTextField.getText()+ "\",\"email\":\"" +registerEmailTextField.getText()+ "\",\"password\":\"" +registerPasswordField.getText()+ "\"}";

            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }


        }
        catch(Exception e)
        {
            e.getStackTrace();
            e.getCause();
        }
    }

    public void registerBackButtonOnAction(ActionEvent event)
    {
        Stage stage = (Stage) registerBackButton.getScene().getWindow();
        stage.close();
    }
}
