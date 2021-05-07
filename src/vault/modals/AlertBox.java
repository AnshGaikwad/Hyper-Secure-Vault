package vault.modals;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

// JavaFX Alert modal to display either caught error messages or various other information
public class AlertBox {

    // Displays an error Alert box to display a caught error during the steganographic process.
    public static void error(String header, String content)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Displays a success prompt after embedding data into an image or extracting a document or an image inside an image.
    public static void information(String header, String content, File file) throws IOException
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType viewButton = new ButtonType("View");
        ButtonType uploadButton = new ButtonType("Upload");
        ButtonType cancelButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(viewButton, uploadButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == viewButton)
        {
            Desktop.getDesktop().open(file);
        }

        Optional<ButtonType> uploadResult = alert.showAndWait();
        if (uploadResult.isPresent() && uploadResult.get() == uploadButton)
        {
            // TODO: Isn't implemented yet
            System.out.println("Upload");
        }
    }

    // Displays a success prompt after extracting a message inside an image
    public static void information(String header, String content, String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane messageView = new GridPane();
        messageView.setMaxWidth(Double.MAX_VALUE);
        messageView.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(messageView);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

}
