package vault.home;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import vault.exceptions.CannotEncodeException;
import vault.exceptions.UnsupportedImageTypeException;
import vault.exceptions.CannotDecodeException;
import vault.algorithms.AESEncryption;
import vault.algorithms.BaseSteganography;
import vault.algorithms.GifSteganography;
import vault.algorithms.HiddenData;
import vault.algorithms.ImageInImageSteganography;
import vault.algorithms.ImageSteganography;
import vault.algorithms.Utils;
import vault.algorithms.ZLibCompression;
import vault.modals.AlertBox;
import vault.modals.PasswordPrompt;
import vault.types.DataFormat;
import vault.types.PasswordType;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.ResourceBundle;

public class HomeController implements Initializable
{

    // JavaFx Components

    // Steganography
    @FXML
    private Menu editMenu;
    @FXML
    private MenuItem newSecretDocument, newSecretImage, cutMenu, copyMenu, pasteMenu, undoMenu, redoMenu, selectAllMenu, deselectMenu, deleteMenu;
    @FXML
    private ImageView secretImageView, coverImageView, steganographicImageView;
    @FXML
    private TextArea secretMessage;
    @FXML
    private Button encodeDocument, encodeImage, decodeImage;
    @FXML
    private Tab secretImageTab, secretMessageTab, secretDocumentTab;
    @FXML
    private VBox coverImagePane, secretImagePane, steganographicImagePane;
    @FXML
    private ListView<String> secretDocumentContent;
    @FXML
    private CheckBox encryptMessage, encryptDocument, compressDocument, compressMessage;
    @FXML
    private ToggleGroup messagePixelsPerByte, documentPixelsPerByte, pixelsPerPixel;
    @FXML
    private HBox messagePixelsPerByteWrapper, documentPixelsPerByteWrapper;
    @FXML
    private ImageView logoImageView;

    // RSA
    @FXML
    private Label nLabel, dLabel, fLabel;
    @FXML
    private TextArea rsaTextField;
    @FXML
    private TextField qField, pField, eField;

    //AES
    @FXML
    private TextField aesTextField;
    @FXML
    private Label aesLabel;

    // Variables

    // Steganography
    private File coverImage, secretImage, secretDocument, steganographicImage, tempFile;
    private String password;
    private Clipboard systemClipboard = Clipboard.getSystemClipboard();

    // RSA
    private static int p;
    private static int q;
    private static int e;

    // AES
    private File aesFile;
    private final FileChooser aesFileChooser = new FileChooser();
    final private static int blockSize;
    private static SecretKey secretKey;


    // Steganography

    // Set cover image from File chooser and add it to the coverImageView
    // Also enable steganographic controls which are disabled
    public void setCoverImage()
    {
        // Choosing the file for cover image
        FileChooser fc = new FileChooser();
        fc.setTitle("Add New Cover Image");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.gif"));
        coverImage = fc.showOpenDialog(null);

        // If cover image selected, setting it to image view and enabling steganographic controls
        if (coverImage != null)
        {
            coverImagePane.setMinSize(0, 0);
            coverImageView.setImage(new Image("file:" + coverImage.getPath()));
            coverImageView.fitWidthProperty().bind(coverImagePane.widthProperty());
            coverImageView.fitHeightProperty().bind(coverImagePane.heightProperty());
            coverImagePane.setMaxSize(900, 900);
            editMenu.setDisable(false);
            newSecretDocument.setDisable(false);
            newSecretImage.setDisable(false);
            secretMessageTab.setDisable(false);
            secretDocumentTab.setDisable(false);
            secretImageTab.setDisable(Utils.getFileExtension(coverImage).equalsIgnoreCase("gif"));
            messagePixelsPerByteWrapper.setVisible(!Utils.getFileExtension(coverImage).equalsIgnoreCase("gif"));
            documentPixelsPerByteWrapper.setVisible(!Utils.getFileExtension(coverImage).equalsIgnoreCase("gif"));
        }
        else
        {
            // If not selected, display alert box
            AlertBox.error("Error while setting cover image", "Please try again!");
        }
    }

    // Set the steganographic image from File Chooser and add it to steganographicImageView
    public void setSteganographicImage()
    {
        // Choosing the file for steganographic image
        FileChooser fc = new FileChooser();
        fc.setTitle("Add New Steganographic Image");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.gif"));
        steganographicImage = fc.showOpenDialog(null);

        // If image is selected, set it to steganographicImageView
        if (steganographicImage != null)
        {
            steganographicImagePane.setMinSize(0, 0);
            steganographicImageView.setImage(new Image("file:" + steganographicImage.getPath()));
            steganographicImageView.fitWidthProperty().bind(steganographicImagePane.widthProperty());
            steganographicImageView.fitHeightProperty().bind(steganographicImagePane.heightProperty());
            steganographicImagePane.setMaxSize(1440, 900);
            decodeImage.setDisable(false);
        }
        else
        {
            // If image not selected, display alert box/
            AlertBox.error("Error while setting steganographic image", "Please try again!");
        }
    }

    // Set the secret document and add it to the secretDocumentContent using getDocumentContent
    public void setSecretDocument()
    {
        // Choose the file for secretDocumentContent
        FileChooser fc = new FileChooser();
        fc.setTitle("Add New Secret Document");
        secretDocument = fc.showOpenDialog(null);

        // If selected, get the content
        if (secretDocument != null)
        {
            encodeDocument.setDisable(false);
            try
            {
                getDocumentContent(secretDocumentContent, secretDocument);
            }
            catch (IOException e)
            {
                // if failed!
                e.printStackTrace();
                AlertBox.error("Error while setting secret document", e.getMessage());
            }
        }
        else
        {
            AlertBox.error("Error while setting secret document", "Please try again!");
        }
    }

    // Set the secret Image using File chooser and add it to the secretImageView
    public void setSecretImage()
    {
        // choose a file for the secretImageView
        FileChooser fc = new FileChooser();
        fc.setTitle("New Secret Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg"));
        secretImage = fc.showOpenDialog(null);

        // if secret image selected, set it to secretImagePane
        if (secretImage != null) {
            secretImagePane.setMinSize(0, 0);
            secretImageView.setImage(new Image("file:" + secretImage.getPath()));
            secretImageView.fitWidthProperty().bind(secretImagePane.widthProperty());
            secretImageView.fitHeightProperty().bind(secretImagePane.heightProperty());
            secretImagePane.setMaxSize(900, 900);
            encodeImage.setDisable(false);

        } else {
            // if not selected display alert
            AlertBox.error("Error while setting secret image", "Please try again!");
        }
    }

    // Encodes  a message in the cover image after compressing then encrypting it (if enabled)
    // Uses ImageSteganography and GifSteganography Respectively
    public void encodeMessageInImage() {

        // Compressing the file using ZLibCompression
        String message = secretMessage.getText();
        byte[] secret = message.getBytes(StandardCharsets.UTF_8);
        if(compressMessage.isSelected())
            secret = ZLibCompression.compress(secret);

        // Encrypting Message using AESEncryption
        if (encryptMessage.isSelected())
            secret = AESEncryption.encrypt(secret, password);

        // Get file extension to differentiate between image and gif
        String imageExtension = Utils.getFileExtension(coverImage).toLowerCase();
        imageExtension = (imageExtension.matches("jpg|jpeg")) ? "png" : imageExtension;

        // Get path for saving image
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Steganographic Image");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        imageExtension.toUpperCase(),
                        "*." + imageExtension));
        steganographicImage = fc.showSaveDialog(null);

        // if image selected, apply steganography
        if (steganographicImage != null)
        {
            BaseSteganography img;
            try
            {
                if (imageExtension.equalsIgnoreCase("gif"))
                    img = new GifSteganography(coverImage, encryptMessage.isSelected(), compressMessage.isSelected());
                else
                    img = new ImageSteganography(coverImage, encryptMessage.isSelected(), compressMessage.isSelected(), getToggleGroupValue(messagePixelsPerByte));
                img.encode(secret, steganographicImage);
                AlertBox.information("Encoding Successful!", "Message encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            }
            catch (IOException | CannotEncodeException | UnsupportedImageTypeException e)
            {
                e.printStackTrace();
                AlertBox.error("Error while encoding", e.getMessage());
            }
        }
    }

    // Encodes a document in an image after compressing then encrypting it (if enabled),
    // then calls either ImageSteganography or GifSteganography based
    // on the cover image extension.
    public void encodeDocumentInImage()
    {
        // Get file extension
        String secretFileExtension = Utils.getFileExtension(secretDocument);
        try
        {
            // Perform Encoding
            if(compressDocument.isSelected() || encryptDocument.isSelected())
            {
                tempFile = File.createTempFile("temp", "." + secretFileExtension);
                tempFile.deleteOnExit();
            }

            if(compressDocument.isSelected() && encryptDocument.isSelected())
            {
                File auxFile = File.createTempFile("aux", "."+secretFileExtension); auxFile.deleteOnExit();
                ZLibCompression.compress(secretDocument, auxFile);
                AESEncryption.encrypt(auxFile, tempFile, password);
            }
            else
            {
                if(compressDocument.isSelected())
                    ZLibCompression.compress(secretDocument, tempFile);
                else if(encryptDocument.isSelected())
                    AESEncryption.encrypt(secretDocument, tempFile, password);
            }
            if(compressDocument.isSelected() || encryptDocument.isSelected())
            {
                secretDocument = tempFile;
            }

            String imageExtension = Utils.getFileExtension(coverImage).toLowerCase();
            imageExtension = (imageExtension.matches("jpg|jpeg")) ? "png" : imageExtension;

            // Save file path
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter(
                            imageExtension.toUpperCase(),
                            "*." + imageExtension));
            steganographicImage = fc.showSaveDialog(null);

            // if image selected, apply steganography
            if (steganographicImage != null)
            {
                BaseSteganography img;
                if (imageExtension.equalsIgnoreCase("gif"))
                    img = new GifSteganography(coverImage, encryptDocument.isSelected(), compressDocument.isSelected());
                else
                    img = new ImageSteganography(coverImage, encryptDocument.isSelected(), compressDocument.isSelected(), getToggleGroupValue(documentPixelsPerByte));
                img.encode(secretDocument, steganographicImage);
                AlertBox.information("Encoding Successful!", "Document encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            }
        }
        catch (IOException | CannotEncodeException | UnsupportedImageTypeException e)
        {
            e.printStackTrace();
            AlertBox.error("Error while encoding", e.getMessage());
        }
    }

    // Encodes an image in another image using ImageInImageSteganography.
    public void encodeImageInImage()
    {
        // Save file path image
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "PNG Image",
                        "*.png"));
        steganographicImage = fc.showSaveDialog(null);

        // if image is selected, apply image in image steganography
        if (steganographicImage != null)
        {
            try
            {
                ImageInImageSteganography img = new ImageInImageSteganography(coverImage, getToggleGroupValue(pixelsPerPixel));
                img.encode(secretImage, steganographicImage);
                AlertBox.information("Encoding Successful!", "Image " + secretImage.getName() + " encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            }
            catch (IOException | CannotEncodeException | UnsupportedImageTypeException e)
            {
                e.printStackTrace();
                AlertBox.error("Error while encoding", e.getMessage());
            }
        }
    }

    // Handles decoding the image by decoding the data using the appropriate class
    // based on the extension ImageSteganography or GifSteganography,
    // then constructs an HiddenData object from the image header,
    // then performs decoding and decompression (if enabled) to return the secret data.
    public void decodeImage()
    {
        // Get image extention and hidden data which has encoding, extension information and other metadata
        String imageExtension = Utils.getFileExtension(steganographicImage);
        HiddenData hiddenData;

        // Init file chooser
        FileChooser fc = new FileChooser();
        File file;
        try
        {
            // Get Image
            BaseSteganography img = (imageExtension.equalsIgnoreCase("gif")) ?
                    new GifSteganography(steganographicImage) : new ImageSteganography(steganographicImage);

            // Set Hidden Data
            hiddenData = new HiddenData(img.getHeader());
            fc.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter(
                            hiddenData.extension.toUpperCase(),
                            "*." + hiddenData.extension));

            // Start Decoding
            if (hiddenData.format == DataFormat.MESSAGE)
            {
                tempFile = File.createTempFile("message", ".txt");
                img.decode(tempFile);
                byte[] secret = Files.readAllBytes(tempFile.toPath());
                String message;

                if (hiddenData.isEncrypted)
                {
                    password = PasswordPrompt.display(PasswordType.DECRYPT);
                    secret = AESEncryption.decrypt(secret, password);
                }

                if (hiddenData.isCompressed)
                    secret = ZLibCompression.decompress(secret);

                assert secret != null;
                message = new String(secret, StandardCharsets.UTF_8);

                if (message.length() > 0)
                    AlertBox.information("Decoding successful!", "Here is the secret message:", message);

                tempFile.deleteOnExit();
            }

            else if(hiddenData.format == DataFormat.DOCUMENT)
            {
                file = fc.showSaveDialog(null);

                if (hiddenData.isCompressed || hiddenData.isEncrypted)
                {
                    tempFile = File.createTempFile("temp", "." + hiddenData.extension);
                    tempFile.deleteOnExit();
                    img.decode(tempFile);
                }
                if (hiddenData.isEncrypted)
                {
                    password = PasswordPrompt.display(PasswordType.DECRYPT);
                }
                if (hiddenData.isEncrypted && hiddenData.isCompressed)
                {
                    File auxFile = File.createTempFile("aux", "." + hiddenData.extension);
                    auxFile.deleteOnExit();
                    AESEncryption.decrypt(tempFile, auxFile, password);
                    ZLibCompression.decompress(auxFile, file);
                }
                else
                {
                    if (hiddenData.isCompressed)
                        ZLibCompression.decompress(tempFile, file);

                    else if (hiddenData.isEncrypted)
                        AESEncryption.decrypt(tempFile, file, password);

                    else
                        img.decode(file);

                }
                if (file != null && file.length() > 0)
                    AlertBox.information("Decoding Successful!", "Document decoded in " + file.getName(), file);
            }

            else if(hiddenData.format == DataFormat.IMAGE)
            {
                ImageInImageSteganography imgInImg = new ImageInImageSteganography(steganographicImage);
                file = fc.showSaveDialog(null);
                imgInImg.decode(file);
                AlertBox.information("Decoding Successful!", "Image decoded in " + file.getName(), file);
            }
        }
        catch (IOException | CannotDecodeException | UnsupportedImageTypeException e)
        {
            e.printStackTrace();
            AlertBox.error("Error while decoding", e.getMessage());
        }
    }

    // Gets the encryption mode from the password prompt.
    public void getEncryptionPassword()
    {
        if (encryptMessage.isSelected() || encryptDocument.isSelected())
        {
            if ((password = PasswordPrompt.display(PasswordType.ENCRYPT)) == null)
            {
                encryptMessage.setSelected(false);
                encryptDocument.setSelected(false);
            }
        }
        else
        {
            password = null;
        }
    }

    // Returns the value of the encryption mode radio buttons.
    private byte getToggleGroupValue(ToggleGroup group)
    {
        RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
        return (byte) Character.getNumericValue(selectedRadioButton.getText().charAt(0));
    }

    // Reads a document line by line and sets its contents into a ListView
    private static void getDocumentContent(ListView<String> documentView, File document) throws IOException
    {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(document));

        //reads the user file
        BufferedReader reader = new BufferedReader(streamReader);
        String line;
        documentView.getItems().clear();

        while ((line = reader.readLine()) != null)
            documentView.getItems().add(line);
    }


    // Undoes the last change to the secretMessage TextArea
    public void undo()
    {
        secretMessage.undo();
    }

    // Redoes the last change to the secretMessage TextArea
    public void redo()
    {
        secretMessage.redo();
    }

    // Cuts the content of the secretMessage TextArea to the system clipboard
    public void cut()
    {
        secretMessage.cut();
    }

    // Copies the content of the secretMessage TextArea to the system clipboard
    public void copy()
    {
        secretMessage.copy();
    }

    // Pastes the content of the system clipboard to the secretMessage TextArea
    public void paste()
    {
        secretMessage.paste();
    }

    // Deletes the selected text of the secretMessage TextArea
    public void delete()
    {
        secretMessage.replaceSelection("");
    }

    // Selects all the content of the secretMessage TextArea
    public void selectAll()
    {
        secretMessage.selectAll();
    }

    // Deselects the current secretMessage TextArea selection
    public void deselect()
    {
        secretMessage.deselect();
    }

    // Handles the state of the menu items in the edit menu
    public void showingEditMenu()
    {
        if( systemClipboard == null)
        {
            systemClipboard = Clipboard.getSystemClipboard();
        }

        pasteMenu.setDisable(!systemClipboard.hasString());

        if(!secretMessage.getSelectedText().equals(""))
        {
            cutMenu.setDisable(false);
            copyMenu.setDisable(false);
            deselectMenu.setDisable(true);
            deleteMenu.setDisable(false);
        }
        else
        {
            cutMenu.setDisable(true);
            copyMenu.setDisable(true);
            deselectMenu.setDisable(true);
            deleteMenu.setDisable(true);
        }

        selectAllMenu.setDisable(secretMessage.getSelectedText().equals(secretMessage.getText()));

        redoMenu.setDisable(!secretMessage.isRedoable());

        undoMenu.setDisable(!secretMessage.isUndoable());
    }

    // Function to open the Backend Source Code
    public void openBackendSource()
    {
        try
        {
            Desktop.getDesktop().browse(new URL("https://github.com/AnshGaikwad/Hyper-Secure-Vault-Backend").toURI());
        }
        catch (IOException | URISyntaxException ioException)
        {
            ioException.printStackTrace();
        }
    }

    // Function to open the Source Code
    public void openSource()
    {
        try
        {
            Desktop.getDesktop().browse(new URL("https://github.com/AnshGaikwad/Hyper-Secure-Vault/").toURI());
        }
        catch (IOException | URISyntaxException ioException)
        {
            ioException.printStackTrace();
        }
    }

    // Function to send me an Email
    public void sendEmail()
    {
        try
        {
            Desktop.getDesktop().browse(new URL("mailto:anshyg2002@gmail.com").toURI());
        }
        catch (IOException | URISyntaxException ioException)
        {
            ioException.printStackTrace();
        }
    }

    // Function to open my Github Profile
    public void openGithub()
    {
        try
        {
            Desktop.getDesktop().browse(new URL("https://github.com/AnshGaikwad").toURI());
        }
        catch (IOException | URISyntaxException ioException)
        {
            ioException.printStackTrace();
        }
    }

    // Function to open my LinkedIn Profile
    public void openLinkedIn()
    {
        try
        {
            Desktop.getDesktop().browse(new URL("https://www.linkedin.com/in/anshgaikwad/").toURI());
        }
        catch (IOException | URISyntaxException ioException)
        {
            ioException.printStackTrace();
        }
    }

    // Function to open my Medium Profile
    public void openMedium()
    {
        try
        {
            Desktop.getDesktop().browse(new URL("https://medium.com/@anshyg2002").toURI());
        }
        catch (IOException | URISyntaxException ioException)
        {
            ioException.printStackTrace();
        }
    }

    // Quit the app
    public void quitApp()
    {
        System.exit(0);
    }

    // RSA

    // RSA Encryption Method
    public void encryptMethod() {

        BigInteger m, n;
        char[] arr;

        if (rsaTextField.getText().equals(""))
        {
            rsaTextField.requestFocus();
        }
        else if (pField.getText().equals(""))
        {
            pField.requestFocus();
        }
        else if (qField.getText().equals(""))
        {
            qField.requestFocus();
        }
        else if (eField.getText().equals(""))
        {
            eField.requestFocus();
        }
        else
        {
            p = Integer.parseInt(pField.getText());
            q = Integer.parseInt(qField.getText());
            e = Integer.parseInt(eField.getText());

            n = new BigInteger(String.valueOf(p * q));

            arr = rsaTextField.getText().toCharArray();
            rsaTextField.clear();

            // RSA Encryption
            for (char c : arr) {
                m = new BigInteger(String.valueOf((int) c));
                rsaTextField.setText(rsaTextField.getText() + (char) (m.pow(e).mod(n).intValue() + 20));
            }

            nLabel.setText("n = " + p * q);
            fLabel.setText("??(n) = " + eulersTotientFunction());
            dLabel.setText("d = " + extendedEuclidAlgorithm());
        }
    }

    // RSA Decryption Method
    public void decryptMethod() {
        BigInteger c, n;
        char[] arr;

        if (rsaTextField.getText().equals(""))
        {
            rsaTextField.requestFocus();
        }
        else if (pField.getText().equals(""))
        {
            pField.requestFocus();
        }
        else if (qField.getText().equals(""))
        {
            qField.requestFocus();
        }
        else if (eField.getText().equals(""))
        {
            eField.requestFocus();
        }
        else
        {
            p = Integer.parseInt(pField.getText());
            q = Integer.parseInt(qField.getText());
            e = Integer.parseInt(eField.getText());

            int d = extendedEuclidAlgorithm();

            n = new BigInteger(String.valueOf(p * q));

            arr = rsaTextField.getText().trim().toCharArray();
            rsaTextField.clear();

            for (char value : arr)
            {
                c = new BigInteger(String.valueOf((int) value - 20));
                rsaTextField.setText(rsaTextField.getText() + (char) ((c.pow(d).mod(n)).intValue()));
            }

            nLabel.setText("n = " + p * q);
            fLabel.setText("??(n) = " + eulersTotientFunction());
            dLabel.setText("d = " + extendedEuclidAlgorithm());

        }

    }

    // Euler's totient function counts the positive integers up to a given integer n
    // that are relatively prime to n.
    public static int eulersTotientFunction() {

        return (p - 1) * (q - 1);
    }

    // Extended Euclidean algorithm for finding 'd' value
    public static int extendedEuclidAlgorithm() {

        int r0, r1, s0, s1, t0, t1, q, a, b, f;
        f = eulersTotientFunction();

        if (e > f)
        {
            a = e;
            b = f;
        }
        else
        {
            a = f;
            b = e;
        }

        r0 = a;
        s0 = 1;
        t0 = 0;
        r1 = b;
        s1 = 0;
        t1 = 1;

        while (true)
        {
            int remainder;

            q = r0 / r1;

            remainder = r0 - (q * r1);
            r0 = r1;
            r1 = remainder;
            if (r1 == 0)
                break;

            remainder = s0 - (q * s1);
            s0 = s1;
            s1 = remainder;

            remainder = t0 - (q * t1);
            t0 = t1;
            t1 = remainder;
        }

        int endValue = e > f ? s1 : t1;

        // If 'endValue' is a negative value, it should be converted to positive
        int k = 1;
        while (endValue < 0)
        {
            if (e > f)
                endValue = s1 + k * b;
            else
                endValue = t1 - (-k * a);
            k++;
        }
        return endValue;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Logo
        File lockFile = new File("images/logo.jpg");
        Image lockImage = new Image(lockFile.toURI().toString());
        logoImageView.setImage(lockImage);

        // RSA Defaults
        pField.setText("71");
        qField.setText("67");
        eField.setText("281");

        // AES Default Key Generated
        aesTextField.setText(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

    // AES

    // Static Variables
    static
    {
        blockSize = 128;
        secretKey = null;
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            secretKey = keyGenerator.generateKey();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    // Encrypt a File using AES Encryption
    public void encryptFile()
    {
        // Init
        initChooseFile();

        // If file object null
        if (aesFile == null)
        {
            return;
        }

        // Choose a file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save encrypted File");
        fileChooser.setInitialDirectory(new File(aesFile.getParent()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter((getFileExtension(aesFile).toUpperCase()), "*."+getFileExtension(aesFile)),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );


        // Output File (Encrypted File)
        File outfile = fileChooser.showSaveDialog(new Stage());
        if (outfile==null)
        {
            return;
        }

        // AES Encryption
        try
        {
            InputStream fis = new FileInputStream(aesFile);
            int read;
            if (!outfile.exists())
            {
                outfile.createNewFile();
            }
            FileOutputStream encryptionFOS = new FileOutputStream(outfile);
            Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            aesCipherOutputStream(fis, encryptionFOS, encryptCipher);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException e)
        {
            e.printStackTrace();
        }
    }

    private void aesCipherOutputStream(InputStream fis, FileOutputStream encryptionFOS, Cipher encryptCipher) throws IOException {
        int read;
        CipherOutputStream cipherOutputStream = new CipherOutputStream(encryptionFOS, encryptCipher);
        byte[] block = new byte[blockSize];
        while ((read = fis.read(block,0,blockSize)) != -1)
        {
            cipherOutputStream.write(block,0, read);
        }
        cipherOutputStream.close();
        fis.close();
    }

    // AES File Decryption
    public void decryptFile()
    {
        try
        {
            // Init
            initChooseFile();

            // If file object is null
            if (aesFile==null)
            {
                return;
            }

            // Select a File
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save decrypted File");
            fileChooser.setInitialDirectory(new File(aesFile.getParent()));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter((getFileExtension(aesFile).toUpperCase()), "*."+getFileExtension(aesFile)),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );

            // Output File
            File outfile = fileChooser.showSaveDialog(new Stage());
            if (outfile==null)
            {
                aesLabel.setText("Choose File to Decrypt");
                return;
            }

            InputStream fis = new FileInputStream(aesFile);
            int read;
            if (!outfile.exists())
                outfile.createNewFile();

            // Start Decryption
            FileOutputStream encryptedFOS = new FileOutputStream(outfile);

            Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.DECRYPT_MODE, secretKey);

            aesCipherOutputStream(fis, encryptedFOS, encryptCipher);

        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | InvalidKeyException e)
        {
            e.printStackTrace();
        }
    }

    // Save the Key locally
    public void saveKey()
    {
        try
        {
            // Choose saving location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save KEY");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(("KEY"), "*.key")
            );
            File outfile = fileChooser.showSaveDialog(new Stage());

            // if null, do nothing
            if (outfile == null)
            {
                return;
            }

            // Save File
            if (!outfile.exists())
            {
                outfile.createNewFile();
            }

            FileOutputStream encryptedFOS = new FileOutputStream(outfile);
            encryptedFOS.write(secretKey.getEncoded());
            encryptedFOS.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Java Fx Function to choose file
    public void initChooseFile()
    {
        aesFile = aesFileChooser.showOpenDialog(new Stage());
    }

    // Use a saved Key
    public void useKey()
    {
        try
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose KEY");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(("KEY"), "*.key")
            );

            File infile = fileChooser.showOpenDialog(new Stage());
            if (infile == null)
            {
                return;
            }

            Path path = Paths.get(infile.getAbsolutePath());
            byte[] encodedKey = Files.readAllBytes(path);
            secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
            aesTextField.setText(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Get File Extension
    private static String getFileExtension(File file)
    {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    // Function to generate a new key
    public void generateNewKey()
    {
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            secretKey = keyGenerator.generateKey();

        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        aesTextField.setText(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

    // When Logout Button pressed go back to login
    public void loginButtonOnAction()
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getResource("../login/login.fxml"));
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