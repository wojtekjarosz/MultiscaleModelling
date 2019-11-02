package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Controller {
    int width = 400, height = 300;
    int cellSize, cellSizeY;
    //int cellSize=(1200)/((width>height)? width : height);
    Board board;
    private Stage dialogStage;
    @FXML
    CheckBox checkbox;
    @FXML
    Canvas canvas;
    @FXML
    ChoiceBox choiceBox;
    @FXML
    ChoiceBox randChoiceBox;
    @FXML
    Button startButton;
    @FXML
    Button mcButton;
    @FXML
    TextField textField;
    @FXML
    TextField xTextField;
    @FXML
    TextField yTextField;
    @FXML
    TextField rTextField;
    @FXML
    ProgressBar progressBar;
    @FXML
    TextField inclusionsAmountTextField;
    @FXML
    TextField inclusionsSizeTextField;
    @FXML
    ChoiceBox inclusionsTypeChoiceBox;
    @FXML
    TextField rule4Probability;
    @FXML
    ChoiceBox caAlgorithmType;

    GraphicsContext gc;
    Random rand;
    Thread thread, mcThread;
    Color cellColor = Color.SANDYBROWN;
    Color backgroundColor = Color.LIGHTYELLOW;
    private volatile boolean running = true;
    private volatile boolean mcRunning = true;
    //Color[] colors;
    List<Color> colors;


    private Main main;
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    public void setMain(Main main) {
        this.main = main;
    }

    @FXML
    public void initialize() {
        rand = new Random();
        xTextField.setText("150");
        yTextField.setText("150");
        rule4Probability.setText("10");
        inclusionsAmountTextField.setText("5");
        inclusionsSizeTextField.setText("3");
        inclusionsTypeChoiceBox.getItems().addAll("square","circle");
        inclusionsTypeChoiceBox.setValue("square");
        choiceBox.getItems().addAll("Moore'a", "von Neumann'a");
        choiceBox.setValue("Moore'a");
        caAlgorithmType.getItems().addAll("Substructure", "Dual phase");
        caAlgorithmType.setValue("Substructure");
        generate();
    }
    @FXML
    public void generate(){
        if(isInputValid()) {
            cellSize = cellSizeY = 4;
            width = Integer.parseInt(xTextField.getText());
            height = Integer.parseInt(yTextField.getText());

            generateBoard(width,height,cellSize);
            checkbox.setSelected(true);
            textField.setText(10 + "");
            startButton.setText("START!");
        }
    }

    @FXML
    public void handleStart() {

        if(startButton.getText() == "START!") {
            running = true;
            thread = new Thread(() -> {
                while (running) {
                    Platform.runLater(() -> startFunction());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            });
            thread.start();
            startButton.setText("STOP!");
        }else if(startButton.getText() == "STOP!"){
            running = false;
            thread.interrupt();
            startButton.setText("START!");
            drawBoard();
        }
    }

    public void startFunction(){
        boolean isFinished;
        board.setPeriod(checkbox.isSelected());
        board.setProbability(Integer.parseInt(rule4Probability.getText()));
        board.setNeighbourhoodType((String) choiceBox.getValue());
        isFinished = board.nextCycle();
        if(isFinished) {
            drawBoard();
            running = false;
            thread.interrupt();
            startButton.setText("START!");
        }
        progressBar.setProgress(board.getProgress());
    }

    @FXML
    public void handleClear(){
        for(int i=0; i<width; i++){
            for(int j=0;j<height;j++){
                board.setCellValue(i,j,false);
                board.setCellGrainType(i,j,0);
                gc.setFill(backgroundColor);
                gc.fillRect(i*cellSize,j*cellSizeY,cellSize,cellSizeY);
            }
        }
        colors.clear();
        board.getSelectedGrains().clear();
    }

    @FXML
    public void handleRand(){

        int numberOfCells = Integer.parseInt(textField.getText());
        if (numberOfCells > (width * height)) {
            numberOfCells = width * height;
            textField.setText(numberOfCells + "");
        }
        randFunc(numberOfCells);
    }

    public void randFunc(int numberOfCells){
        try {

            int size = colors.size();
            //colors = new Color[numberOfCells];
            for (int i = size; i < numberOfCells + size; i++) {

                int x = rand.nextInt(width);
                int y = rand.nextInt(height);
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();

                //Color randomColor = new Color.(r, g, b);
                if (!board.getCellValue(x, y)) {
                    board.setCellValue(x, y, true);
                    board.setCellGrainType(x, y, i + 1);

                    board.setCellColor(x, y, Color.color(r, g, b));
                    gc.setFill(board.getCellColor(x, y));
                    //colors[i]=board.getCellColor(x,y);
                    colors.add(board.getCellColor(x, y));
                    gc.fillRect(x * cellSize , y * cellSizeY , cellSize , cellSizeY );
                } else i--;

            }

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR Dialog");
            alert.setHeaderText("Error");
            alert.setContentText("Not a number");
            alert.showAndWait();
        }
    }


    public void drawBoard(){
        for(int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                if(board.getCellValue(i,j) && board.getCellGrainType(i,j) != -1 ){
                    int type = board.getCellGrainType(i,j);
                    if(type != Integer.MAX_VALUE)
                        gc.setFill(colors.get(type-1));
                    else
                        gc.setFill(Color.DARKMAGENTA);
                    gc.fillRect(i*cellSize,j*cellSizeY,cellSize,cellSizeY);

                }else{
                    if(board.getCellGrainType(i,j) == -1)
                        gc.setFill(Color.BLACK);
                    else{
                        gc.setFill(backgroundColor);
                    }
                    gc.fillRect(i*cellSize,j*cellSizeY,cellSize,cellSizeY);
                }
            }
        }
    }

    @FXML
    public void monteCarloHandle(){
        //startButton.setDisable(false);
        System.out.println(mcButton.getText());
        if(mcButton.getText().startsWith("MCSTART")) {
            mcRunning = true;
            mcThread = new Thread(() -> {
                while (mcRunning) {
                    Platform.runLater(() -> monteCarlo());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            mcThread.start();
            mcButton.setText("MCSTOP");
        }else if(mcButton.getText().startsWith("MCSTOP")){
            mcRunning = false;
            mcThread.interrupt();
            mcButton.setText("MCSTART");
        }
    }

    public void monteCarlo(){

        Map<Integer,Integer> energies = new HashMap<>();
        Map<Integer,Color> colours = new HashMap<>();
        int i = rand.nextInt(width);
        int j = rand.nextInt(height);
        //System.out.println("Komórka: "+i+","+j);
        //System.out.println("Typ: "+board.getCellGrainType(i,j));
        Color color;
        int type=0;
        if (!board.isPeriod()) {
            int startX = Math.max(i - 1, 0);
            int startY = Math.max(j - 1, 0);
            int endX = Math.min(i + 1, width - 1);
            int endY = Math.min(j + 1, height - 1);

            //liczymy energię komórki
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    type=board.getCellGrainType(x,y);
                    color = board.getCellColor(x,y);
                    if (energies.containsKey(type)) {
                        int count = energies.get(type);
                        energies.put(type, count + 1);

                    } else if (type != 0) {
                        energies.put(type, 1);
                    }
                    colours.put(type,color);
                }
            }
            int k = energies.get(board.getCellGrainType(i,j));
            energies.put(board.getCellGrainType(i,j), k-1);
            type= energies.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
            int energy=energies.get(type);

            board.setCellGrainType(i,j,type);
            board.setCellColor(i,j,colours.get(type));



        } else {
            //periodycznie

            int startX = i - 1;
            int startY = j - 1;
            int endX = i + 1;
            int endY = j + 1;

            int tmpX, tmpY;
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    tmpX = x;
                    tmpY = y;
                    if (x == -1) tmpX = width - 1;
                    if (x == width) tmpX = 0;
                    if (y == -1) tmpY = height - 1;
                    if (y == height) tmpY = 0;

                    type=board.getCellGrainType(tmpX,tmpY);
                    color = board.getCellColor(tmpX,tmpY);
                    if (energies.containsKey(type)) {
                        int count = energies.get(type);
                        energies.put(type, count + 1);

                    } else if (type != 0) {
                        energies.put(type, 1);
                        System.out.println("");
                    }
                    colours.put(type,color);
                }

            }
            int k = energies.get(board.getCellGrainType(i,j));
            energies.put(board.getCellGrainType(i,j), k-1);
            type= energies.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
            System.out.println(type);
            int energy=energies.get(type);

            board.setCellGrainType(i,j,type);
            board.setCellColor(i,j,colours.get(type));


        }
        drawBoard();
    }

    @FXML
    public void handleFillRandomly(){
        handleClear();
        randFunc(width*height);
    }

    public void exportToTXT(){

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to txt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT", "*.txt")
        );
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
            try {
                PrintWriter writer;
                writer = new PrintWriter(file);
                writer.println(width+ " " + height + " " + cellSize);
                for (int i = 0; i < board.getWidth(); i++) {
                    for (int j = 0; j < board.getHeight(); j++) {
                        String cell = i + " " + j + " " + board.getCellGrainType(i, j) + " " + board.getCellValue(i, j) + " ";
                        writer.println(cell);
                    }
                }
                writer.close();

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

    }


    public void importFromTXT(){
        Map<Integer,Color> colorsMap = new HashMap<>();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT", "*.txt")
        );

        try {
            File file = fileChooser.showOpenDialog(dialogStage);
            if (file != null) {
                Scanner myReader = new Scanner(file);
                String data = myReader.nextLine();

                String[] parts = data.split(" ");
                xTextField.setText(parts[0]);
                yTextField.setText(parts[1]);
                generateBoard(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

                while (myReader.hasNextLine()) {
                    data = myReader.nextLine();
                    parts = data.split(" ");
                    int i = Integer.valueOf(parts[0]);
                    int j = Integer.valueOf(parts[1]);
                    int type = Integer.valueOf(parts[2]);
                    boolean isALive = Boolean.valueOf(parts[3]);
                    board.setCellGrainType(i, j, type);
                    board.setCellValue(i, j, isALive);
                    if (colorsMap.containsKey(type)) {
                        board.setCellColor(i, j, colorsMap.get(type));
                    } else {
                        if(type != -1) {
                            float r = rand.nextFloat();
                            float g = rand.nextFloat();
                            float b = rand.nextFloat();
                            board.setCellColor(i, j, Color.color(r, g, b));
                        }else{
                            board.setCellColor(i, j, Color.BLACK);
                        }
                        colorsMap.put(type, board.getCellColor(i, j));
                    }
                    gc.setFill(colorsMap.get(type));
                    gc.fillRect(i * cellSize , j * cellSizeY , cellSize, cellSizeY);
                }
                myReader.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }catch (Exception ex){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(main.getPrimaryStage());
            alert.setTitle("Niewłaściwe dane wejściowe");
            alert.setHeaderText("Niewłaściwe dane wejściowe");
            alert.setContentText("Proszę sprawdzić poprawność danych wejściowych.");

            alert.showAndWait();
        }

    }


    public void exportToBMP(){
        // BufferedImage bimage =new BufferedImage() canvas.snapshot(new SnapshotParameters(), null).;
        WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save canvas");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                //Boolean isFinish = ImageIO.write(SwingFXUtils.fromFXImage(image, null), "bmp", file);

                // create a blank, RGB, same width and height, and a white background
                BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                        bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, java.awt.Color.WHITE, null);

                // write to jpeg file
                Boolean isFinish = ImageIO.write(newBufferedImage, "bmp", file);
                //ImageIO.write(newBufferedImage, "jpg", new File("c:\\javanullpointer.jpg"));
                System.out.println(isFinish);

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }


    public void importFromBMP(){
        Map<Integer,Integer> colorsMap = new HashMap<>();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );

        try {
            File file = fileChooser.showOpenDialog(dialogStage);
            if (file != null) {

                BufferedImage bufferedImage = ImageIO.read(file);
                int width = bufferedImage.getWidth()/cellSize;
                int height = bufferedImage.getHeight()/cellSizeY;
                xTextField.setText(String.valueOf(width));
                yTextField.setText(String.valueOf(height));
                generateBoard(width, height, cellSize);
                int counter = 0;
                for(int i =0; i <bufferedImage.getWidth();i += cellSize){
                    for (int j = 0; j<bufferedImage.getHeight(); j += cellSize){
                        int xi = i/cellSize;
                        int yj = j/cellSizeY;
                        int colorRGB = bufferedImage.getRGB(i,j);
                        int  r   = (colorRGB & 0x00ff0000) >> 16;
                        int  g = (colorRGB & 0x0000ff00) >> 8;
                        int  b  =  colorRGB & 0x000000ff;
                        Color color = Color.rgb(r,g,b);
                        if (colorsMap.containsKey(colorRGB)) {
                            board.setCellColor(xi, yj, color);
                        } else if(colorRGB != -16777216){
                            board.setCellColor(xi, yj, color);
                            colorsMap.put(colorRGB, counter++);
                        } else {
                            board.setCellColor(xi, yj, color);
                            colorsMap.put(colorRGB, -1);
                        }
                        board.setCellGrainType(xi,yj,colorsMap.get(colorRGB));
                        board.setCellValue(xi,yj,true);
                        gc.setFill(color);
                        gc.fillRect(xi * cellSize , yj * cellSizeY , cellSize, cellSizeY);
                    }
                }

            }
        } catch (Exception ex){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(main.getPrimaryStage());
            alert.setTitle("Niewłaściwe dane wejściowe");
            alert.setHeaderText("Niewłaściwe dane wejściowe");
            alert.setContentText("Proszę sprawdzić poprawność danych wejściowych.");

            alert.showAndWait();
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";


        if (xTextField.getText() == null || xTextField.getText().length() == 0) {
            errorMessage += "No valid width!\n";
        } else {
            // try to parse the postal code into an int.
            try {
                Integer.parseInt(xTextField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid width (must be an integer)!\n";
            }
        }

        if (yTextField.getText() == null || yTextField.getText().length() == 0) {
            errorMessage += "No valid width!\n";
        } else {
            // try to parse the postal code into an int.
            try {
                Integer.parseInt(yTextField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid width (must be an integer)!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    void generateBoard(int width, int height, int cellSize){
        /*canvas.setWidth(width*cellSize);
        canvas.setHeight(height*cellSize);*/
            this.cellSize = this.cellSizeY = cellSize;
            this.width = width;
            this.height = height;

            canvas.setWidth(width*cellSize);
            canvas.setHeight(height*cellSize);

            colors = new ArrayList<>();
            board = new Board(width, height);
            gc = canvas.getGraphicsContext2D();
            //drawLines();
            gc.setFill(cellColor);
            canvas.setOnMouseClicked(event -> {
                try {
                    double x = event.getX();
                    double y = event.getY();
                    System.out.println("Clicked on: " + x + ", " + y);

                    int cell_x = (int) x * width / (width * cellSize) - 1;
                    int cell_y = (int) y * height / (height * cellSize) - 1;
                    System.out.println("Cell nr: " + cell_x + ", " + cell_y);
                    if (board.getCellValue(cell_x, cell_y)) {

                        Color color = board.getCellColor(cell_x, cell_y);
                        int type = board.getCellGrainType(cell_x,cell_y);
                        board.getSelectedGrains().add(type);
                        for(int i=0;i<width;i++) {
                            for (int j = 0; j < height; j++) {
                                if(board.getCellGrainType(i, j) == type){
                                    //board.setCellGrainType(i,j, Integer.MAX_VALUE);
                                    //board.setCellColor(i,j,Color.DARKMAGENTA);
                                    gc.setFill(Color.DARKMAGENTA);
                                    gc.fillRect(i * cellSize , j * cellSize , cellSize , cellSize );
                                }
                            }
                        }
                        //colors.add(board.getCellColor(cell_x, cell_y));
                        gc.setFill(board.getCellColor(cell_x, cell_y));
                        gc.fillRect(x  - (x % cellSize), y  - (y % cellSize), cellSize, cellSize);
                    } else {
                        //board.setCellValue(cell_x, cell_y, false);
                        //gc.setFill(backgroundColor);
                        //gc.fillRect(x + 1 - (x % cellSize), y + 1 - (y % cellSize), cellSize-1, cellSize-1);


                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

    }

    @FXML
    public void addInclusions() {
        int numberOfInclusion = Integer.parseInt(inclusionsAmountTextField.getText());
        int sizeOfInclusion = Integer.parseInt(inclusionsSizeTextField.getText());

        if (numberOfInclusion > (width * height)) {
            numberOfInclusion = width * height;
            textField.setText(numberOfInclusion + "");
        }
        if(!board.isFinished()) {
            randInclusion(numberOfInclusion, sizeOfInclusion, false);
        }else{
            randInclusion(numberOfInclusion, sizeOfInclusion, true);
        }


    }

    public void randInclusion(int numberOfCells, int sizeOfInclusion, boolean onBorders){
        try {
            List<Point> grainsOnBorder = null;
            if(onBorders){
                grainsOnBorder = findGrainsOnBorders();
            }

            //colors = new Color[numberOfCells];
            for (int i = 0; i < numberOfCells; i++) {
                int x, y;

                if(!onBorders) {
                    x = rand.nextInt(width);
                    y = rand.nextInt(height);
                }else{
                    int point = rand.nextInt(grainsOnBorder.size());
                    x = grainsOnBorder.get(point).x;
                    y = grainsOnBorder.get(point).y;
                }

                for(int k = x - sizeOfInclusion; k<x+sizeOfInclusion; k++){
                    for(int l = y - sizeOfInclusion; l<y+sizeOfInclusion; l++){
                        if (k>=0 && l>=0 && k<width && l<height) {
                            if(inclusionsTypeChoiceBox.getValue().equals("square") || (inclusionsTypeChoiceBox.getValue().equals("circle") && countDistance(x,y,k,l,sizeOfInclusion))) {
                                board.setCellValue(k, l, true);
                                board.setCellGrainType(k, l, -1);

                                board.setCellColor(k, l, Color.BLACK);
                                gc.setFill(board.getCellColor(k, l));

                                //colors[i]=board.getCellColor(x,y);
                                colors.add(board.getCellColor(k, l));
                                gc.fillRect(k * cellSize , l * cellSizeY , cellSize, cellSizeY);
                            }
                        } //else i--;
                    }
                }
                //Color randomColor = new Color.(r, g, b);


            }

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR Dialog");
            alert.setHeaderText("Error");
            alert.setContentText("Not a number");
            alert.showAndWait();
        }
    }

    private List<Point> findGrainsOnBorders() {
        List<Point> grainsOnBorder = new ArrayList();
        for(int i=0;i<width;i++) {
            for (int j = 0; j < height; j++) {
                if(board.isCellOnBorder(i,j))
                    grainsOnBorder.add(new Point(i,j));
            }
        }
        return grainsOnBorder;
    }

    public boolean countDistance(int x, int y, int k, int l, int size){
        boolean cond=true;
        double distance=0;
        distance= sqrt(pow((k-x),2) + pow((l-y),2));
        if((distance)>(size-1))
            cond=false;
        return cond;
    }

    @FXML
    public void clearUnselectedGrains(){
        for(int i=0;i<width;i++) {
            for (int j = 0; j < height; j++) {
                int type = board.getCellGrainType(i,j);
                if(board.getCellValue(i,j) && !board.getSelectedGrains().contains(type)){
                    board.setCellGrainType(i, j, 0);
                    board.setCellValue(i, j, false);
                    //gc.setFill(Color.BEIGE);
                    //gc.fillRect(i * cellSize , j * cellSize , cellSize , cellSize );
                }else if(board.getCellValue(i,j) && board.getSelectedGrains().contains(type)){
                    //gc.setFill(board.getCellColor(i,j));
                    //c.fillRect(i * cellSize , j * cellSize , cellSize , cellSize );
                }
            }
        }
        if(caAlgorithmType.getValue().equals("Substructure")){
            drawBoard();
        }else{
            for(int i=0;i<width;i++) {
                for (int j = 0; j < height; j++) {
                    if(board.getSelectedGrains().contains(board.getCellGrainType(i,j))){
                        board.setCellGrainType(i,j, Integer.MAX_VALUE);
                        board.setCellColor(i,j,Color.DARKMAGENTA);
                    }
                }
            }
            board.getSelectedGrains().clear();
            board.getSelectedGrains().add(Integer.MAX_VALUE);
            drawBoard();
        }

    }

}
