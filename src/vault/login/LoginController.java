package vault.login;

import javafx.event.ActionEvent;
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
import java.util.ResourceBundle;


public class LoginController implements Initializable {

    @FXML
    private Button quitButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label loginMessageLabel;

    @FXML
    private ImageView brandingImageView;

    @FXML
    private ImageView lockImageView;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File brandingFile = new File("images/image.png");
        Image brandingImage = new Image(brandingFile.toURI().toString());
        brandingImageView.setImage(brandingImage);

        File lockFile = new File("images/logo.jpg");
        Image lockImage = new Image(lockFile.toURI().toString());
        lockImageView.setImage(lockImage);
    }

    public void loginButtonOnAction(ActionEvent event)
    {
        if(emailTextField.getText().isBlank())
            loginMessageLabel.setText("Email isn't entered");
        else if(passwordField.getText().isBlank())
            loginMessageLabel.setText("Password isn't entered");
        else
            validateLogin();
    }

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

            goToHome();
        }
        catch(Exception e)
        {
            e.getStackTrace();
            e.getCause();
        }

    }


    public void quitButtonOnAction(ActionEvent event)
    {
        goToHome();
//        Stage stage = (Stage) quitButton.getScene().getWindow();
//        stage.close();
    }

    public void registerButtonOnAction(ActionEvent event)
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

    private void goToHome() {
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
