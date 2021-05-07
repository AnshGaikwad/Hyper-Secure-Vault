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

public class HomeController implements Initializable {

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
    private File file;
    private final FileChooser fileChooser = new FileChooser();
    final private static int mBlocksize;
    private static SecretKey secretKey;

    // Set cover image from Filechooser and add it to the coverImageView
    // Also enable steganographic controls which are disbled
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
            secretImageTab.setDisable(Utils.getFileExtension(coverImage).toLowerCase().equals("gif"));
            messagePixelsPerByteWrapper.setVisible(!Utils.getFileExtension(coverImage).toLowerCase().equals("gif"));
            documentPixelsPerByteWrapper.setVisible(!Utils.getFileExtension(coverImage).toLowerCase().equals("gif"));
        }
        else
        {
            // If not selceted, dispaly alert box
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

    // Set the secret Image using Filechooser and add it to the secretImageView
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
                if (imageExtension.toLowerCase().equals("gif"))
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

    /**
     * Encodes a document in an image after compressing then encrypting it (if enabled),
     * then calls either {@link ImageSteganography} or {@link GifSteganography} based
     * on the cover image extension.
     */
    public void encodeDocumentInImage() {
        String secretFileExtension = Utils.getFileExtension(secretDocument);
        try {
            if(compressDocument.isSelected() || encryptDocument.isSelected()) {tempFile = File.createTempFile("temp", "." + secretFileExtension); tempFile.deleteOnExit();}
            if(compressDocument.isSelected() && encryptDocument.isSelected()) {
                File auxFile = File.createTempFile("aux", "."+secretFileExtension); auxFile.deleteOnExit();
                ZLibCompression.compress(secretDocument, auxFile);
                AESEncryption.encrypt(auxFile, tempFile, password);
            }else{
                if(compressDocument.isSelected())
                    ZLibCompression.compress(secretDocument, tempFile);
                else if(encryptDocument.isSelected())
                    AESEncryption.encrypt(secretDocument, tempFile, password);
            }
            if(compressDocument.isSelected() || encryptDocument.isSelected()) { secretDocument = tempFile; }
            String imageExtension = Utils.getFileExtension(coverImage).toLowerCase();
            imageExtension = (imageExtension.matches("jpg|jpeg")) ? "png" : imageExtension;
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter(
                            imageExtension.toUpperCase(),
                            "*." + imageExtension));
            steganographicImage = fc.showSaveDialog(null);
            if (steganographicImage != null) {
                BaseSteganography img;
                if (imageExtension.toLowerCase().equals("gif"))
                    img = new GifSteganography(coverImage, encryptDocument.isSelected(), compressDocument.isSelected());
                else
                    img = new ImageSteganography(coverImage, encryptDocument.isSelected(), compressDocument.isSelected(), getToggleGroupValue(documentPixelsPerByte));
                img.encode(secretDocument, steganographicImage);
                AlertBox.information("Encoding Successful!", "Document encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            }
        } catch (IOException | CannotEncodeException | UnsupportedImageTypeException e) {
            e.printStackTrace();
            AlertBox.error("Error while encoding", e.getMessage());
        }
    }

    /**
     * Encodes an image in another image using {@link ImageInImageSteganography}.
     */
    public void encodeImageInImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "PNG Image",
                        "*.png"));
        steganographicImage = fc.showSaveDialog(null);
        if (steganographicImage != null) {
            try {
                ImageInImageSteganography img = new ImageInImageSteganography(coverImage, getToggleGroupValue(pixelsPerPixel));
                img.encode(secretImage, steganographicImage);
                AlertBox.information("Encoding Successful!", "Image " + secretImage.getName() + " encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            } catch (IOException | CannotEncodeException | UnsupportedImageTypeException e) {
                e.printStackTrace();
                AlertBox.error("Error while encoding", e.getMessage());
            }
        }
    }

    /**
     * Handles decoding the image by decoding the data using the appropriate class
     * based on the extension ({@link ImageSteganography} or {@link GifSteganography}),
     * then constructs an {@link HiddenData} object from the image header,
     * then performs decoding and decompression (if enabled) to return the secret data.
     */
    public void decodeImage() {
        String imageExtension = Utils.getFileExtension(steganographicImage);
        HiddenData hiddenData;
        FileChooser fc = new FileChooser();
        File file;
        try {
            BaseSteganography img = (imageExtension.toLowerCase().equals("gif")) ? new GifSteganography(steganographicImage) : new ImageSteganography(steganographicImage);
            hiddenData = new HiddenData(img.getHeader());
            fc.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter(
                            hiddenData.extension.toUpperCase(),
                            "*." + hiddenData.extension));

            if (hiddenData.format == DataFormat.MESSAGE) {
                tempFile = File.createTempFile("message", ".txt");
                img.decode(tempFile);
                byte[] secret = Files.readAllBytes(tempFile.toPath());
                String message;
                if (hiddenData.isEncrypted) {
                    password = PasswordPrompt.display(PasswordType.DECRYPT);
                    secret = AESEncryption.decrypt(secret, password);
                }
                if (hiddenData.isCompressed)
                    secret = ZLibCompression.decompress(secret);
                message = new String(secret, StandardCharsets.UTF_8);
                if (message.length() > 0)
                    AlertBox.information("Decoding successful!", "Here is the secret message:", message);
                tempFile.deleteOnExit();
            }

            else if(hiddenData.format == DataFormat.DOCUMENT) {
                file = fc.showSaveDialog(null);
                if (hiddenData.isCompressed || hiddenData.isEncrypted) {
                    tempFile = File.createTempFile("temp", "." + hiddenData.extension);
                    tempFile.deleteOnExit();
                    img.decode(tempFile);
                }
                if (hiddenData.isEncrypted) {
                    password = PasswordPrompt.display(PasswordType.DECRYPT);
                }
                if (hiddenData.isEncrypted && hiddenData.isCompressed) {
                    File auxFile = File.createTempFile("aux", "." + hiddenData.extension);
                    auxFile.deleteOnExit();
                    AESEncryption.decrypt(tempFile, auxFile, password);
                    ZLibCompression.decompress(auxFile, file);
                } else {
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

            else if(hiddenData.format == DataFormat.IMAGE){
                ImageInImageSteganography imgInImg = new ImageInImageSteganography(steganographicImage);
                file = fc.showSaveDialog(null);
                imgInImg.decode(file);
                AlertBox.information("Decoding Successful!", "Image decoded in " + file.getName(), file);
            }
        } catch (IOException | CannotDecodeException | UnsupportedImageTypeException e) {
            e.printStackTrace();
            AlertBox.error("Error while decoding", e.getMessage());
        }
    }


    /**
     * Gets the encryption mode from the password prompt.
     */
    public void getEncryptionPassword() {
        if (encryptMessage.isSelected() || encryptDocument.isSelected()) {
            if ((password = PasswordPrompt.display(PasswordType.ENCRYPT)) == null) {
                encryptMessage.setSelected(false);
                encryptDocument.setSelected(false);
            }
        } else {
            password = null;
        }
    }

    /** Returns the value of the encryption mode radio buttons.
     *
     * @param group radio button group.
     * @return      encryption mode (1 or 2).
     */
    private byte getToggleGroupValue(ToggleGroup group){
        RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
        return (byte) Character.getNumericValue(selectedRadioButton.getText().charAt(0));
    }

    /**
     * Reads a document line by line and sets its contents into a <code>ListView</code>.
     *
     * @param documentView <code>JavaFX ListView</code> that will hold the document line by line
     * @param document     document to display in the ListView
     * @throws IOException if an error occurs while reading the document
     */
    private static void getDocumentContent(ListView<String> documentView, File document) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(document));
        BufferedReader reader = new BufferedReader(streamReader);//reads the user file
        String line;
        documentView.getItems().clear();
        while ((line = reader.readLine()) != null)
            documentView.getItems().add(line);
    }

    /** Undoes the last change to the {@link #secretMessage} <code>TextArea</code>. */
    public void undo() { secretMessage.undo(); }
    /** Redoes the last change to the {@link #secretMessage} <code>TextArea</code>. */
    public void redo() { secretMessage.redo(); }
    /** Cuts the content of the {@link #secretMessage} <code>TextArea</code> to the system clipboard. */
    public void cut() { secretMessage.cut(); }
    /** Copies the content of the {@link #secretMessage} <code>TextArea</code> to the system clipboard. */
    public void copy(){ secretMessage.copy();}
    /** Pastes the content of the system clipboard to the {@link #secretMessage} <code>TextArea</code>. */
    public void paste(){ secretMessage.paste(); }
    /** Deletes the selected text of the {@link #secretMessage} <code>TextArea</code>. */
    public void delete(){ secretMessage.replaceSelection(""); }
    /** Selects all the content of the {@link #secretMessage} <code>TextArea</code>. */
    public void selectAll(){ secretMessage.selectAll(); }
    /** Deselects the current {@link #secretMessage} <code>TextArea</code> selection. */
    public void deselect() { secretMessage.deselect(); }

    /** Handles the state of the menu items in the edit menu. */
    public void showingEditMenu() {
        if( systemClipboard == null ) {systemClipboard = Clipboard.getSystemClipboard();}

        if(systemClipboard.hasString()) { pasteMenu.setDisable(false); }
        else {pasteMenu.setDisable(true);}

        if(!secretMessage.getSelectedText().equals("")) {cutMenu.setDisable(false); copyMenu.setDisable(false); deselectMenu.setDisable(true); deleteMenu.setDisable(false);}
        else { cutMenu.setDisable(true); copyMenu.setDisable(true); deselectMenu.setDisable(true); deleteMenu.setDisable(true);}

        if (secretMessage.getSelectedText().equals(secretMessage.getText())) { selectAllMenu.setDisable(true); }
        else { selectAllMenu.setDisable(false); }

        if(secretMessage.isRedoable()) { redoMenu.setDisable(false); }
        else { redoMenu.setDisable(true); }

        if(secretMessage.isUndoable()) { undoMenu.setDisable(false); }
        else { undoMenu.setDisable(true); }
    }

    /** Displays the About Page. */
    public void openBackendSource() {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/AnshGaikwad/Hyper-Secure-Vault-Backend").toURI());
        } catch (IOException | URISyntaxException ioException) {
            ioException.printStackTrace();
        }
    }

    /** Displays the About Page. */
    public void openSource() {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/AnshGaikwad/Hyper-Secure-Vault/").toURI());
        } catch (IOException | URISyntaxException ioException) {
            ioException.printStackTrace();
        }
    }

    /** Displays the About Page. */
    public void sendEmail() {
        try {
            Desktop.getDesktop().browse(new URL("mailto:anshyg2002@gmail.com").toURI());
        } catch (IOException | URISyntaxException ioException) {
            ioException.printStackTrace();
        }
    }

    /** Displays the About Page. */
    public void openGithub() {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/AnshGaikwad").toURI());
        } catch (IOException | URISyntaxException ioException) {
            ioException.printStackTrace();
        }
    }

    /** Displays the About Page. */
    public void openLinkedIn() {
        try {
            Desktop.getDesktop().browse(new URL("https://www.linkedin.com/in/anshgaikwad/").toURI());
        } catch (IOException | URISyntaxException ioException) {
            ioException.printStackTrace();
        }
    }

    /** Displays the About Page. */
    public void openMedium() {
        try {
            Desktop.getDesktop().browse(new URL("https://medium.com/@anshyg2002").toURI());
        } catch (IOException | URISyntaxException ioException) {
            ioException.printStackTrace();
        }
    }

    /** Quits the app. */
    public void quitApp() {
        System.exit(0);
    }

    // RSA Cipher
    @FXML
    void encyptMethod(ActionEvent event) {
        BigInteger m, n;
        char[] arr;

        if (rsaTextField.getText().equals("")) {
            rsaTextField.requestFocus();
        } else if (pField.getText().equals("")) {
            pField.requestFocus();
        } else if (qField.getText().equals("")) {
            qField.requestFocus();
        } else if (eField.getText().equals("")) {
            eField.requestFocus();
        } else {
            p = Integer.parseInt(pField.getText());
            q = Integer.parseInt(qField.getText());
            e = Integer.parseInt(eField.getText());

            n = new BigInteger(String.valueOf(p * q));

            arr = rsaTextField.getText().toCharArray();
            rsaTextField.clear();

            for (int i = 0; i < arr.length; i++) {
                m = new BigInteger(String.valueOf((int) arr[i]));
                rsaTextField.setText(rsaTextField.getText() + (char) (m.pow(e).mod(n).intValue() + 20));
            }

            nLabel.setText("n = " + p * q);
            fLabel.setText("φ(n) = " + EulersTotientFunction());
            dLabel.setText("d = " + ExtendedEuclidAlgorithm());

        }
    }

    @FXML
    void decyptMethod(ActionEvent event) {
        BigInteger c, n;
        char[] arr;

        if (rsaTextField.getText().equals("")) {
            rsaTextField.requestFocus();
        } else if (pField.getText().equals("")) {
            pField.requestFocus();
        } else if (qField.getText().equals("")) {
            qField.requestFocus();
        } else if (eField.getText().equals("")) {
            eField.requestFocus();
        } else {
            p = Integer.parseInt(pField.getText());
            q = Integer.parseInt(qField.getText());
            e = Integer.parseInt(eField.getText());

            int d = ExtendedEuclidAlgorithm();

            n = new BigInteger(String.valueOf(p * q));

            arr = rsaTextField.getText().trim().toCharArray();
            rsaTextField.clear();

            for (int i = 0; i < arr.length; i++) {
                c = new BigInteger(String.valueOf((int) arr[i] - 20));
                rsaTextField.setText(rsaTextField.getText() + (char) ((c.pow(d).mod(n)).intValue()));
            }

            nLabel.setText("n = " + p * q);
            fLabel.setText("φ(n) = " + EulersTotientFunction());
            dLabel.setText("d = " + ExtendedEuclidAlgorithm());

        }

    }

    // Euler's totient function counts the positive integers up to a given integer n
    // that are relatively prime to n.
    public static int EulersTotientFunction() {

        return (p - 1) * (q - 1);
    }

    // Extended Euclidean algorithm for finding 'd' value
    public static int ExtendedEuclidAlgorithm() {

        int r0, r1, s0, s1, t0, t1, q, a, b, f;
        f = EulersTotientFunction();

        if (e > f) {
            a = e;
            b = f;
        } else {
            a = f;
            b = e;
        }

        r0 = a;
        s0 = 1;
        t0 = 0;
        r1 = b;
        s1 = 0;
        t1 = 1;

        while (true) {
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

        // If 'endValue' is a negative value, it should be converted to positiv
        int k = 1;
        while (endValue < 0) {
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

        // AES
        aesTextField.setText(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

        // button1.setOnAction(event -> {textfield.setText("DGDHDJ");});
    }

    // AES File Encrypter
    static {
        mBlocksize = 128;
        secretKey = null;
        String res = "0";
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            secretKey = kgen.generateKey();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    @FXML
    protected void handle1(ActionEvent event) {

        chooseFile();
        if (file==null){return;}
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save encrypted File");
        fileChooser.setInitialDirectory(new File(file.getParent()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter((getFileExtension(file).toUpperCase()), "*."+getFileExtension(file)),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );


            /*//Randomize number with Random From security
            SecureRandom srand = new SecureRandom();
            long randLong = (srand.nextInt()%10000000);*/
        File outfile = fileChooser.showSaveDialog(new Stage());
        if (outfile==null){return;}
        /*AES Encryption*/
        try{
            InputStream fis = new FileInputStream(file);
            int read = 0;
            if (!outfile.exists()) {
                outfile.createNewFile();
            }
            FileOutputStream encfos = new FileOutputStream(outfile);
            Cipher encipher = Cipher.getInstance("AES");
            encipher.init(Cipher.ENCRYPT_MODE, secretKey);
            CipherOutputStream cipheoutstream = new CipherOutputStream(encfos, encipher);
            byte[] block = new byte[mBlocksize];
            while ((read = fis.read(block,0,mBlocksize)) != -1) {
                cipheoutstream.write(block,0, read);
            }
            cipheoutstream.close();
            fis.close();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handle2 (ActionEvent event){
        //AES Crypto decrypted
        try{
            chooseFile();
            if (file==null){

                return;}
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save decrypted File");
            fileChooser.setInitialDirectory(new File(file.getParent()));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter((getFileExtension(file).toUpperCase()), "*."+getFileExtension(file)),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );
            File outfile = fileChooser.showSaveDialog(new Stage());
            if (outfile==null){ aesLabel.setText("Choose File to Decrypt");return;}
            InputStream fis = new FileInputStream(file);
            int read = 0;
            if (!outfile.exists())
                outfile.createNewFile();

            FileOutputStream encfos = new FileOutputStream(outfile);

            Cipher encipher = Cipher.getInstance("AES");
            encipher.init(Cipher.DECRYPT_MODE, secretKey);
            CipherOutputStream cipheoutstream = new CipherOutputStream(encfos, encipher);

            byte[] block = new byte[mBlocksize];
            while ((read = fis.read(block,0,mBlocksize)) != -1) {
                cipheoutstream.write(block,0, read);
            }
            cipheoutstream.close();
            fis.close();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            e.printStackTrace();
        }

    }


    public void handle3(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save KEY");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(("KEY"), "*.key")
            );
            File outfile = fileChooser.showSaveDialog(new Stage());
            if (outfile == null) {
                return;
            }
            int read = 0;
            if (!outfile.exists()) {
                outfile.createNewFile();
            }

            FileOutputStream encfos = new FileOutputStream(outfile);

            encfos.write(secretKey.getEncoded());

            encfos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void chooseFile(){
        file = fileChooser.showOpenDialog(new Stage());
    }

    public void handle4(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose KEY");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(("KEY"), "*.key")
            );
            File infile = fileChooser.showOpenDialog(new Stage());
            if (infile == null) {
                return;
            }
            Path path = Paths.get(infile.getAbsolutePath());
            byte[] encodedKey = Files.readAllBytes(path);
            secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
            aesTextField.setText(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    public void genereteNewKey(ActionEvent event) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            secretKey = kgen.generateKey();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        aesTextField.setText(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

    public void loginButtonOnAction(ActionEvent event)
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
