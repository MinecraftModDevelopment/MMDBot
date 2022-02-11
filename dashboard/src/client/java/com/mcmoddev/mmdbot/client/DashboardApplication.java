package com.mcmoddev.mmdbot.client;

import com.mcmoddev.mmdbot.client.scenes.LoginScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardApplication extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final Scene loginScene = LoginScene.createAddressSelectionScreen(primaryStage);
        primaryStage.setTitle("MMDBot Dashboard");
        primaryStage.setScene(loginScene);
        primaryStage.requestFocus();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DashboardClient.shutdown();
    }
}
