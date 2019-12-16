package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Controller {
    int width = 400, height = 300;
    int cellSize, cellSizeY;
    Board board;
    private Stage dialogStage;
    @FXML
    CheckBox checkbox;
    @FXML
    Canvas canvas;
    @FXML
    ChoiceBox choiceBox;
    @FXML
    Button startButton;
    @FXML
    Button mcButton;
    @FXML
    TextField xTextField;
    @FXML
    TextField yTextField;
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
    @FXML
    TextField boundaryWidth;
    @FXML
    CheckBox onlySelectedGrains;
    @FXML
    Label percentOfBoundaries;
    @FXML
    TextField mcNucleonsAmount;
    @FXML
    TextField randTextField;
    @FXML
    TextField mcIterationsAmount;
    @FXML
    ChoiceBox mcNumberOfNucleonsType;
    @FXML
    CheckBox mcLocationType;
    @FXML
    TextField mcNumberOfNucleonsAmount, mcJ, mcInsideEnergy, mcGBEnergy;
    @FXML
    Button mcRecBtn;

    GraphicsContext gc;
    Random rand;
    Thread thread, mcThread, mcRecrystallizationThread;
    Color cellColor = Color.SANDYBROWN;
    int greyValue = 245;
    Color backgroundColor = Color.rgb(greyValue,greyValue,greyValue);
    private boolean running = true;
    private boolean mcRunning = true;
    private boolean mcRecrystallizationRunning = true;
    Map<Integer, Color> colors;
    List<Point> points;
    int mcCounter;
    boolean firstIteration;
    int mcNumberOfNucleons;
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
        randTextField.setText("50");
        rule4Probability.setText("10");
        inclusionsAmountTextField.setText("5");
        inclusionsSizeTextField.setText("3");
        inclusionsTypeChoiceBox.getItems().addAll("square","circle");
        inclusionsTypeChoiceBox.setValue("square");
        choiceBox.getItems().addAll("Moore'a", "von Neumann'a");
        choiceBox.setValue("Moore'a");
        caAlgorithmType.getItems().addAll("Substructure", "Dual phase");
        caAlgorithmType.setValue("Substructure");
        boundaryWidth.setText("1");
        mcNucleonsAmount.setText("10");
        mcIterationsAmount.setText("20");
        mcNumberOfNucleonsType.getItems().addAll("Constant", "Increasing", "At the begining of simulation");
        mcNumberOfNucleonsType.setValue("Constant");
        mcLocationType.setSelected(true);
        mcGBEnergy.setText("7");
        mcInsideEnergy.setText("2");

        generate();
    }
    @FXML
    public void generate(){
        if(isInputValid()) {
            cellSize = cellSizeY = 3;
            width = Integer.parseInt(xTextField.getText());
            height = Integer.parseInt(yTextField.getText());

            generateBoard(width,height,cellSize);
            checkbox.setSelected(true);
            startButton.setText("START!");
            percentOfBoundaries.setText("0%");
        }
    }

    @FXML
    public void handleStart() {
        if(isInputValid()) {
            if (startButton.getText() == "START!") {
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
            } else if (startButton.getText() == "STOP!") {
                running = false;
                thread.interrupt();
                startButton.setText("START!");
                drawBoard();
            }
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
            DecimalFormat df = new DecimalFormat("###.##");
            percentOfBoundaries.setText(df.format(board.countPercentOfBoundaries()) + "%");
            System.out.println(board.countPercentOfBoundaries());
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
        percentOfBoundaries.setText("0%");
    }

    @FXML
    public void handleRand(){
        if(isInputValid()) {
            int numberOfCells = Integer.parseInt(randTextField.getText());
            if (numberOfCells > (width * height)) {
                numberOfCells = width * height;
                randTextField.setText(numberOfCells + "");
            }
            randFunc(numberOfCells);
        }
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
                    if (!colors.containsKey(i+1)) {
                        colors.put(i + 1, board.getCellColor(x, y));
                    }
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
                        gc.setFill(colors.get(type));
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
        mcCounter = Integer.parseInt(mcIterationsAmount.getText());
        points = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int type = board.getCellGrainType(i,j);
                if(!board.getSelectedGrains().containsKey(type))
                    points.add(new Point(i,j));
            }
        }
        //startButton.setDisable(false);
        System.out.println(mcButton.getText());
        if(mcButton.getText().startsWith("START")) {
            mcRunning = true;
            mcThread = new Thread(() -> {
                while (mcRunning) {
                    Platform.runLater(() -> monteCarlo(false));

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                        System.out.println("Interrupted");
                    }
                }
            });
            mcThread.start();
            mcButton.setText("STOP");
        }else if(mcButton.getText().startsWith("STOP")){
            mcRunning = false;
            mcThread.interrupt();
            mcButton.setText("START");
        }
    }



    private int countEnergy(int type, int i, int j, List<Integer> neighbors, boolean before, boolean recrystallization) {
        int energy = 0;
        int startX = i - 1;
        int startY = j - 1;
        int endX = i + 1;
        int endY = j + 1;
        boolean isRecrystallized;
        int tmpX, tmpY;
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                tmpX = x;
                tmpY = y;
                if (x == -1) tmpX = width - 1;
                if (x == width) tmpX = 0;
                if (y == -1) tmpY = height - 1;
                if (y == height) tmpY = 0;
                if (!(x == i && y == j)) {
                    int neighborType = board.getCellGrainType(tmpX, tmpY);
                    isRecrystallized = board.isCellGrainRecrystallized(tmpX,tmpY);
                    if(!isRecrystallized && recrystallization)
                        continue;
                    if (type != neighborType ) {
                        energy++;
                        if (!neighbors.contains(neighborType) && !board.getSelectedGrains().containsKey(neighborType) && before)
                            neighbors.add(neighborType);
                    }
                }
            }

        }

        return energy;
    }


    @FXML
    public void handleFillRandomly(){
        //handleClear();
        //randFunc(width*height);
        Map<Integer,Color> colorsMap = new HashMap<>();

        int amount = Integer.parseInt(mcNucleonsAmount.getText());
        List<Integer> chosenTypes = new ArrayList<>();
        for(int k=0; k<amount;k++){
            int randType = rand.nextInt();
            if(!chosenTypes.contains(randType) && !board.getSelectedGrains().containsKey(randType))
                chosenTypes.add(randType);
            else
                k--;
        }
        for(int i=0;i<width;i++) {
            for (int j = 0; j < height; j++) {
                int type = chosenTypes.get(rand.nextInt(chosenTypes.size()));
                System.out.println(type);
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();

                if (!colorsMap.containsKey(type)) {
                    colorsMap.put(type, Color.color(0, g, b));
                }

                //Color randomColor = new Color.(r, g, b);
                if (!board.getCellValue(i, j)) {
                    board.setCellValue(i, j, true);
                    board.setCellGrainType(i, j, type);

                    board.setCellColor(i, j, colorsMap.get(type));
                    gc.setFill(board.getCellColor(i, j));
                    //colors[i]=board.getCellColor(x,y);
                    if (!colors.containsKey(type)) {
                        colors.put(type, board.getCellColor(i, j));
                    }
                    gc.fillRect(i * cellSize , j * cellSizeY , cellSize , cellSizeY );
                }
            }
        }

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


        /*if (xTextField.getText() == null || xTextField.getText().length() == 0) {
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

            } catch (NumberFormatException e) {
                errorMessage += "No valid width (must be an integer)!\n";
            }Integer.parseInt(yTextField.getText());
        }*/

        List<TextField> textFields = new ArrayList<>();
        textFields.add(xTextField);
        textFields.add(yTextField);
        textFields.add(rule4Probability);
        textFields.add(inclusionsAmountTextField);
        textFields.add(inclusionsSizeTextField);
        textFields.add(boundaryWidth);
        for(TextField textField : textFields){
            if (textField.getText() == null || textField.getText().length() == 0) {
                errorMessage += "No valid !\n";
            } else {
                // try to parse the postal code into an int.
                try {
                    Integer.parseInt(textField.getText());
                } catch (NumberFormatException e) {
                    errorMessage += "No valid value (must be an integer)!\n";
                }
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

            colors = new HashMap<>();
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

                        //Color color = board.getCellColor(cell_x, cell_y);

                        int type = board.getCellGrainType(cell_x,cell_y);
                        //Color color = colors.get(type);
                        if(board.getSelectedGrains().get(type) == null) {
                            Color selectingCOlor = Color.DARKMAGENTA;
                            if(!onlySelectedGrains.isSelected()) {
                                if (caAlgorithmType.getValue().toString() == "Substructure") {
                                    board.getSelectedGrains().put(type, 0);
                                    selectingCOlor = Color.MAGENTA;
                                } else {
                                    board.getSelectedGrains().put(type, 1);
                                }
                            }else{
                                selectingCOlor = Color.GOLD;
                                board.getSelectedGrains().put(type, 2);
                            }

                            for (int i = 0; i < width; i++) {
                                for (int j = 0; j < height; j++) {
                                    if (board.getCellGrainType(i, j) == type) {
                                        //board.setCellGrainType(i,j, Integer.MAX_VALUE);
                                        //board.setCellColor(i,j,Color.DARKMAGENTA);
                                        gc.setFill(selectingCOlor);
                                        gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
                                    }
                                }
                            }
                        }else{
                            board.getSelectedGrains().remove(Integer.valueOf(type));
                            for (int i = 0; i < width; i++) {
                                for (int j = 0; j < height; j++) {
                                    if (board.getCellGrainType(i, j) == type && type != Integer.MAX_VALUE) {
                                        //board.setCellGrainType(i,j, Integer.MAX_VALUE);
                                        //board.setCellColor(i,j,Color.DARKMAGENTA);
                                        gc.setFill(colors.get(type-1));
                                        gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
                                    }
                                }
                            }
                        }
                        //colors.add(board.getCellColor(cell_x, cell_y));
                        gc.setFill(board.getCellColor(cell_x, cell_y));
                        gc.fillRect(cell_x * cellSize, cell_y * cellSize, cellSize, cellSize);
                    } else {
                        //board.setCellValue(cell_x, cell_y, true);

                        //gc.fillRect(x + 1 - (x % cellSize), y + 1 - (y % cellSize), cellSize-1, cellSize-1);
                        float r = rand.nextFloat();
                        float g = rand.nextFloat();
                        float b = rand.nextFloat();
                        gc.setFill(Color.color(r, g, b));
                        //Color randomColor = new Color.(r, g, b);
                        board.setCellValue(cell_x, cell_y, true);
                        board.setCellGrainType(cell_x, cell_y, colors.size() + 1);

                        board.setCellColor(cell_x, cell_y, Color.color(r, g, b));
                        gc.setFill(board.getCellColor(cell_x, cell_y));
                        //colors[i]=board.getCellColor(x,y);
                        if (!colors.containsKey(colors.size() + 1)) {
                            colors.put(colors.size() + 1, board.getCellColor(cell_x, cell_y));
                        }
                        gc.fillRect(x * cellSize , y * cellSizeY , cellSize , cellSizeY );


                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

    }

    @FXML
    public void addInclusions() {
        if(isInputValid()) {
            int numberOfInclusion = Integer.parseInt(inclusionsAmountTextField.getText());
            int sizeOfInclusion = Integer.parseInt(inclusionsSizeTextField.getText());

            if (numberOfInclusion > (width * height)) {
                numberOfInclusion = width * height;
                randTextField.setText(numberOfInclusion + "");
            }
            if (!board.isFinished()) {
                randInclusion(numberOfInclusion, sizeOfInclusion, false);
            } else {
                randInclusion(numberOfInclusion, sizeOfInclusion, true);
            }
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
                                if (!colors.containsKey(-1)) {
                                    colors.put(-1, board.getCellColor(k, l));
                                }
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
                if(board.getCellValue(i,j) && board.getSelectedGrains().get(type) == null){
                    board.setCellGrainType(i, j, 0);
                    board.setCellValue(i, j, false);
                    //gc.setFill(Color.BEIGE);
                    //gc.fillRect(i * cellSize , j * cellSize , cellSize , cellSize );
                }else if(board.getCellValue(i,j) && board.getSelectedGrains().get(type) == 0){
                    //gc.setFill(board.getCellColor(i,j));
                    //c.fillRect(i * cellSize , j * cellSize , cellSize , cellSize );
                } else if(board.getCellValue(i,j) && board.getSelectedGrains().get(type) == 1){

                }
            }
        }
        for(int i=0;i<width;i++) {
            for (int j = 0; j < height; j++) {
                if (board.getSelectedGrains().get(board.getCellGrainType(i, j)) != null){
                    if (board.getSelectedGrains().get(board.getCellGrainType(i, j)) == 1) {
                        board.setCellGrainType(i, j, Integer.MAX_VALUE);
                        board.setCellColor(i, j, Color.DARKMAGENTA);
                    }
                }
            }
        }
        //board.getSelectedGrains().clear();
        board.getSelectedGrains().put(Integer.MAX_VALUE, 1);
        drawBoard();


    }

    @FXML
    public void drawBoundaries(){
        if(isInputValid()) {
            int type;
            Map<Integer, Integer> neighbours = new HashMap<>();
            int bw = Integer.parseInt(boundaryWidth.getText());
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (board.isCellOnBorder(i, j)) {
                        int counter = 1;
                        int ijType = board.getCellGrainType(i, j);
                        boolean alone = true;
                        while (counter <= bw) {
                            int left = i - 1 * counter;
                            int up = j - 1 * counter;
                            int right = i + 1 * counter;
                            int down = j + 1 * counter;
                            int[] x = {left, i, right, i};
                            int[] y = {j, up, j, down};
                            int tmpX, tmpY;


                            for (int k = 0; k < 4; k++) {
                                tmpX = x[k];
                                tmpY = y[k];
                                if (x[k] < 0) tmpX = width - 1 * counter;
                                if (x[k] >= width) tmpX = 0 + counter - 1;
                                if (y[k] < 0) tmpY = height - 1 * counter;
                                if (y[k] >= height) tmpY = 0 + counter - 1;

                                type = board.getCellGrainType(tmpX, tmpY);
                                if (type == ijType && !(tmpX == i && tmpY == j) && counter == 1)
                                    alone = false;
                                if (neighbours.containsKey(type)) {
                                    int count = neighbours.get(type);
                                    neighbours.put(type, count + 1);
                                } else if (type != 0 && type != -1) {
                                    neighbours.put(type, 1);
                                }
                            }
                            counter++;
                        }
                        if (onlySelectedGrains.isSelected()){
                            if(board.getSelectedGrains().get(ijType) == null) {
                                neighbours.clear();
                            }else if(board.getSelectedGrains().get(ijType) != 2){
                                neighbours.clear();
                            }
                        }

                        if (neighbours.size() > 1 || alone) {
                            board.setCellGrainType(i, j, -1);
                            gc.setFill(Color.BLACK);
                            gc.fillRect(i * cellSize, j * cellSizeY, cellSize, cellSizeY);
                        }
                        neighbours.clear();
                    }
                }
            }
        }
        DecimalFormat df = new DecimalFormat("###.##");
        percentOfBoundaries.setText(df.format(board.countPercentOfBoundaries()) + "%");
        System.out.println(board.countPercentOfBoundaries());
    }

    @FXML
    public void clearCanvasWithoutBoundaries(){

        for(int i=0; i<width; i++){
            for(int j=0;j<height;j++){
                if(board.getCellGrainType(i,j) != -1){
                    board.setCellValue(i,j,false);
                    board.setCellGrainType(i,j,0);
                    gc.setFill(backgroundColor);
                    gc.fillRect(i*cellSize,j*cellSizeY,cellSize,cellSizeY);
                }else{
                    board.setCellValue(i,j,true);
                    board.setCellGrainType(i,j,-1);
                    gc.setFill(Color.BLACK);
                    gc.fillRect(i*cellSize,j*cellSizeY,cellSize,cellSizeY);
                }
            }
        }
        colors.clear();
        board.getSelectedGrains().clear();

    }


    @FXML
    public void distributeEnergy(){
        board.distributeEnergy(Integer.parseInt(mcGBEnergy.getText()), Integer.parseInt(mcInsideEnergy.getText()));
    }
    @FXML
    public void monteCarloEnergyHandle(){

        firstIteration = true;
        mcNumberOfNucleons = Integer.parseInt(mcNumberOfNucleonsAmount.getText());
        points = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int type = board.getCellGrainType(i,j);
                if(!board.getSelectedGrains().containsKey(type))
                    points.add(new Point(i,j));
            }
        }
        //recrystallization();
        if(mcRecBtn.getText().startsWith("START")) {
            mcRecrystallizationRunning = true;
            mcRecrystallizationThread = new Thread(() -> {
                while (mcRecrystallizationRunning) {
                    Platform.runLater(() -> recrystallization());

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                        System.out.println("Interrupted");
                    }
                }
            });
            mcRecrystallizationThread.start();
            mcRecBtn.setText("STOP MC");
        }else if(mcRecBtn.getText().startsWith("STOP")){
            mcRecrystallizationRunning = false;
            mcRecrystallizationThread.interrupt();
            mcRecBtn.setText("START MC");
        }

    }

    public void recrystallization(){
        boolean atTheBeginning = mcNumberOfNucleonsType.getValue().toString().startsWith("At");
        if(firstIteration || !atTheBeginning){
            board.addNucleons(mcLocationType.isSelected(), mcNumberOfNucleons, colors);
            if(mcNumberOfNucleonsType.getValue().toString().startsWith("Increasing")){
                mcNumberOfNucleons+=mcNumberOfNucleons;
            }else{
                mcNumberOfNucleons = Integer.parseInt(mcNumberOfNucleonsAmount.getText());
            }
        }

        monteCarlo(true);
        drawBoard();
        firstIteration = false;
    }

    @FXML
    public void drawEnergy(){
        for(int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                if(board.getCellValue(i,j)){
                    int energy = board.getCellGrainEnergy(i,j);
                    if(energy == Integer.parseInt(mcInsideEnergy.getText()))
                        gc.setFill(Color.BLUE);
                    else if(energy == Integer.parseInt(mcGBEnergy.getText()))
                        gc.setFill(Color.YELLOW);
                    else if(energy == 0)
                        gc.setFill(Color.RED);
                    else
                        gc.setFill(Color.MAGENTA);
                    gc.fillRect(i*cellSize,j*cellSizeY,cellSize,cellSizeY);

                }
            }
        }
    }

    public void monteCarlo(boolean recrystallization){


        Collections.shuffle(points, new Random(3));
        for(Point point : points) {
            List<Integer> neighbors = new ArrayList();
            int energyBefore = 0;
            int energyAfter = 0;
            int i = point.x;
            int j = point.y;
            int type = board.getCellGrainType(i, j);
            energyBefore = countEnergy(type, i, j, neighbors, true, recrystallization);

            if (neighbors.size() > 0) {
                int chosenType = neighbors.get(rand.nextInt(neighbors.size()));
                energyAfter = countEnergy(chosenType, i, j, neighbors, false, false);
                if(recrystallization){
                    int J = Integer.parseInt(mcJ.getText());
                    energyAfter *= J;
                    energyBefore *= J;
                    energyBefore += board.getCellGrainEnergy(i,j);
                }
                int deltaE = energyAfter - energyBefore;
                if (deltaE <= 0) {
                    board.setCellGrainType(i,j,chosenType);
                    board.setCellColor(i,j,colors.get(chosenType));
                    if(recrystallization) {
                        board.setCellRecrystallized(i,j,true);
                        board.setCellGrainEnergy(i,j,0);
                    }
                }
            }
        }
        mcCounter--;
        if(mcCounter==0){
            mcRunning = false;
            mcButton.setText("START");
            drawBoard();
        }
        //drawBoard();
    }


}
