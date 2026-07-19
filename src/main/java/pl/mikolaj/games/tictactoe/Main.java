package pl.mikolaj.games.tictactoe;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private final Pane root = new Pane();
    private final RealBoard realBoard = new RealBoard();

    @Override
    public void start(Stage primaryStage) {
        realBoard.init(root);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
