package vault.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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


public class LoginController implements Initializable
{

    // Java Fx Components

    @FXML
    private Label loginMessageLabel;

    @FXML
    private ImageView brandingImageView, lockImageView;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordField;

    // initialize => Executes on start
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Set Images
        File brandingFile = new File("images/image.png");
        Image brandingImage = new Image(brandingFile.toURI().toString());
        brandingImageView.setImage(brandingImage);

        File lockFile = new File("images/logo.jpg");
        Image lockImage = new Image(lockFile.toURI().toString());
        lockImageView.setImage(lockImage);
    }

    // On Login Button Pressed
    // Validates Email and Password is entered
    public void loginButtonOnAction()
    {
        // Validate input
        if(emailTextField.getText().isBlank())
            loginMessageLabel.setText("Email isn't entered");
        else if(passwordField.getText().isBlank())
            loginMessageLabel.setText("Password isn't entered");
        else
            // Validation successful, send to validateLogin()
            validateLogin();
    }

    // Send a POST request to backend server API with login details
    private void validateLogin()
    {
        try
        {
            URL url = new URL("http://localhost:8080/api/users/login");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            String jsonInputString = "{\"email\":\""+emailTextField.getText()+"\",\"password\":\""+passwordField.getText()+ "\"}";

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

    // On quit button pressed
    public void quitButtonOnAction()
    {
        // A shortcut to home page added, if backend server not running
        goToHome();
        // Stage stage = (Stage) quitButton.getScene().getWindow();
        // stage.close();
    }

    // On register button pressed, go to RegisterPage
    public void registerButtonOnAction()
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getResource("../register/register.fxml"));
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

    // Go to HomePage
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
}
