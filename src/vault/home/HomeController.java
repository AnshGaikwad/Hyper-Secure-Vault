package vault.home;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
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
import vault.modals.AboutPage;
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

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    // JavaFX Components
    @FXML
    private Menu editMenu;
    @FXML
    private MenuItem newSecretDocument, newSecretImage, cutMenu, copyMenu, pasteMenu, undoMenu, redoMenu, selectAllMenu, deselectMenu, deleteMenu;
    @FXML
    private RadioMenuItem darkTheme, lightTheme;
    @FXML
    private ImageView secretImageView, coverImageView, steganographicImageView;
    @FXML
    private TextArea secretMessage;
    @FXML
    private Button encodeDocument, encodeImage, decodeImage;
    @FXML
    private Tab secretImageTab, secretMessageTab, secretDocumentTab;
    @FXML
    private VBox root, coverImagePane, secretImagePane, steganographicImagePane;
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

    // RSA Cipher
    private static int p;
    private static int q;
    private static int e;

    @FXML
    private Label n_label;
    @FXML
    private Label f_label;
    @FXML
    private Label d_label;
    @FXML
    private Button en_button;
    @FXML
    private Button de_button;
    @FXML
    private TextArea text_field;
    @FXML
    private TextField p_field;
    @FXML
    private TextField q_field;
    @FXML
    private TextField e_field;

    // Files;
    private File coverImage, secretImage, secretDocument, steganographicImage, tempFile;
    // Password
    private String password;
    // Clipboard
    private Clipboard systemClipboard = Clipboard.getSystemClipboard();

    /**
     * Sets the cover image from the <code>JavaFX FileChooser</code> and adds it to the {@link #coverImageView}
     * then enables the disabled secret data controls.
     */
    public void setCoverImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Cover Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.gif"));
        coverImage = fc.showOpenDialog(null);
        if (coverImage != null) {
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
        } else {
            AlertBox.error("Error while setting cover image", "Try again...");
        }
    }

    /**
     * Sets the steganographic image from the <code>JavaFX FileChooser</code> and adds it to the {@link #steganographicImageView}.
     */
    public void setSteganographicImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Steganographic Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.gif"));
        steganographicImage = fc.showOpenDialog(null);
        if (steganographicImage != null) {
            steganographicImagePane.setMinSize(0, 0);
            steganographicImageView.setImage(new Image("file:" + steganographicImage.getPath()));
            steganographicImageView.fitWidthProperty().bind(steganographicImagePane.widthProperty());
            steganographicImageView.fitHeightProperty().bind(steganographicImagePane.heightProperty());
            steganographicImagePane.setMaxSize(1440, 900);
            decodeImage.setDisable(false);
        } else {
            AlertBox.error("Error while setting steganographic image", "Try again...");
        }
    }

    /**
     * Sets the secret document from the <code>JavaFX FileChooser</code> then adds its content
     * line by line in the {@link #secretDocumentContent} using {@link #getDocumentContent(ListView, File)}
     */
    public void setSecretDocument() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Secret Document...");
        secretDocument = fc.showOpenDialog(null);
        if (secretDocument != null) {
            encodeDocument.setDisable(false);
            try {
                getDocumentContent(secretDocumentContent, secretDocument);
            } catch (IOException e) {
                e.printStackTrace();
                AlertBox.error("Error while setting secret document", e.getMessage());
            }
        } else {
            AlertBox.error("Error while setting secret document", "Try again...");
        }
    }

    /**
     * Sets the secret image from the <code>JavaFX FileChooser</code> and adds it to the {@link #secretImageView}.
     */
    public void setSecretImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Secret Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg"));
        secretImage = fc.showOpenDialog(null);
        if (secretImage != null) {
            secretImagePane.setMinSize(0, 0);
            secretImageView.setImage(new Image("file:" + secretImage.getPath()));
            secretImageView.fitWidthProperty().bind(secretImagePane.widthProperty());
            secretImageView.fitHeightProperty().bind(secretImagePane.heightProperty());
            secretImagePane.setMaxSize(900, 900);
            encodeImage.setDisable(false);

        } else {
            AlertBox.error("Error while setting secret image", "Try again...");
        }
    }

    /**
     * Encodes a message in an image after compressing then encrypting it (if enabled),
     * then calls either {@link ImageSteganography} or {@link GifSteganography} based
     * on the cover image extension.
     */
    public void encodeMessageInImage() {
        String message = secretMessage.getText();
        byte[] secret = message.getBytes(StandardCharsets.UTF_8);
        if(compressMessage.isSelected())
            secret = ZLibCompression.compress(secret);
        if (encryptMessage.isSelected())
            secret = AESEncryption.encrypt(secret, password);
        String imageExtension = Utils.getFileExtension(coverImage).toLowerCase();
        imageExtension = (imageExtension.matches("jpg|jpeg")) ? "png" : imageExtension;
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Steganographic Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        imageExtension.toUpperCase(),
                        "*." + imageExtension));

        steganographicImage = fc.showSaveDialog(null);
        if (steganographicImage != null) {
            BaseSteganography img;
            try {
                if (imageExtension.toLowerCase().equals("gif"))
                    img = new GifSteganography(coverImage, encryptMessage.isSelected(), compressMessage.isSelected());
                else
                    img = new ImageSteganography(coverImage, encryptMessage.isSelected(), compressMessage.isSelected(), getToggleGroupValue(messagePixelsPerByte));
                img.encode(secret, steganographicImage);
                AlertBox.information("Encoding Successful!", "Message encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            } catch (IOException | CannotEncodeException | UnsupportedImageTypeException e) {
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

    /** Handles switching between themes for the main scene. */
    public void setTheme(){
        if(darkTheme.isSelected())
            root.getScene().getStylesheets().add(getClass().getResource("resources/modena_dark.css").toExternalForm());
        if(lightTheme.isSelected())
            root.getScene().getStylesheets().remove(getClass().getResource("resources/modena_dark.css").toExternalForm());
    }

    /** Displays the About Page. */
    public void showAboutPage() {
        AboutPage.display();
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

        if (text_field.getText().equals("")) {
            text_field.requestFocus();
        } else if (p_field.getText().equals("")) {
            p_field.requestFocus();
        } else if (q_field.getText().equals("")) {
            q_field.requestFocus();
        } else if (e_field.getText().equals("")) {
            e_field.requestFocus();
        } else {
            p = Integer.parseInt(p_field.getText());
            q = Integer.parseInt(q_field.getText());
            e = Integer.parseInt(e_field.getText());

            n = new BigInteger(String.valueOf(p * q));

            arr = text_field.getText().toCharArray();
            text_field.clear();

            for (int i = 0; i < arr.length; i++) {
                m = new BigInteger(String.valueOf((int) arr[i]));
                text_field.setText(text_field.getText() + (char) (m.pow(e).mod(n).intValue() + 20));
            }

            n_label.setText("n = " + p * q);
            f_label.setText("φ(n) = " + EulersTotientFunction());
            d_label.setText("d = " + ExtendedEuclidAlgorithm());

        }
    }

    @FXML
    void decyptMethod(ActionEvent event) {
        BigInteger c, n;
        char[] arr;

        if (text_field.getText().equals("")) {
            text_field.requestFocus();
        } else if (p_field.getText().equals("")) {
            p_field.requestFocus();
        } else if (q_field.getText().equals("")) {
            q_field.requestFocus();
        } else if (e_field.getText().equals("")) {
            e_field.requestFocus();
        } else {
            p = Integer.parseInt(p_field.getText());
            q = Integer.parseInt(q_field.getText());
            e = Integer.parseInt(e_field.getText());

            int d = ExtendedEuclidAlgorithm();

            n = new BigInteger(String.valueOf(p * q));

            arr = text_field.getText().trim().toCharArray();
            text_field.clear();

            for (int i = 0; i < arr.length; i++) {
                c = new BigInteger(String.valueOf((int) arr[i] - 20));
                text_field.setText(text_field.getText() + (char) ((c.pow(d).mod(n)).intValue()));
            }

            n_label.setText("n = " + p * q);
            f_label.setText("φ(n) = " + EulersTotientFunction());
            d_label.setText("d = " + ExtendedEuclidAlgorithm());

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

        File lockFile = new File("images/logo.jpg");
        Image lockImage = new Image(lockFile.toURI().toString());
        logoImageView.setImage(lockImage);

        p_field.setText("71");
        q_field.setText("67");
        e_field.setText("281");
    }
}
