package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    private Controller controller;
    @Override
    public void start(Stage primaryStage) throws Exception{

        this.primaryStage = primaryStage;

        initRootLayout();
        showMainOverview();
        /*FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("sample.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Grain growth");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        //dialogStage.setMaximized(true);
        //dialogStage.resizableProperty().setValue(Boolean.FALSE);

        // Set the person into the controller.
        Controller controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setMain(this);
        dialogStage.showAndWait();
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });*/
    }

    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class
                    .getResource("root.fxml"));
            rootLayout = (BorderPane) loader.load();
            rootLayout.setPrefWidth(1200);
            rootLayout.setPrefHeight(700);

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            RootController rootController = loader.getController();
            rootController.setMain(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void showMainOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("sample.fxml"));
            AnchorPane mainOverview = (AnchorPane) loader.load();

            // Set person overview into the center of root layout.
            rootLayout.setCenter(mainOverview);


            // Give the mainOverviewController access to the main app.
            Controller controller = loader.getController();
            this.controller = controller;
            controller.setMain(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        launch(args);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Controller getController() {
        return controller;
    }

}
