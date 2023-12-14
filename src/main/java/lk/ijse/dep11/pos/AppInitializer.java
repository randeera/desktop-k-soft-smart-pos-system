package lk.ijse.dep11.pos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.ijse.dep11.pos.db.SingleConnectionDataSource;

import java.io.IOException;
import java.sql.SQLException;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        try (var connection = SingleConnectionDataSource.getInstance().getConnection()) {
            launch(args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(this.getClass().getResource("/view/MainForm.fxml"));
        Scene mainScene = new Scene(root);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Smart POS");
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}
