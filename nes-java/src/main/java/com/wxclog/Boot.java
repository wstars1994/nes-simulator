package com.wxclog;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wxclog.net.NesNetMain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;

/**
 * @description: 主程序入口
 * @author: WStars
 * @date: 2020-04-17 10:38
 */
public class Boot extends Application {

    @FXML
    private Pane modelSelectPanel;
    @FXML
    private Pane multiPanel;
    @FXML
    private Canvas renderCanvas;
    @FXML
    private ListView roomList;
    @FXML
    private WebView webView;
    private boolean webViewIsLoad = false;

    public class JavaConnector {
        public void back(){
            modelSelectPanel.setVisible(true);
            webView.setVisible(false);
        }
        public void createRoom(){
            NesNetMain.send(1,null);
        }
        public void joinRoom(String roomId){
            NesNetMain.send(2,roomId);
        }
        public void quitRoom(){
            NesNetMain.send(3,null);
        }
    }

    public void singlePlayerBtnClick() {
        modelSelectPanel.setVisible(false);
        NesBoot.start(renderCanvas);
    }

    public void multiPlayerBtnClick() {
        webView.setVisible(true);
        modelSelectPanel.setVisible(false);
        WebEngine webEngine = webView.getEngine();
        if(!webViewIsLoad){
            webEngine.load(getClass().getResource("/")+"html/nes.html");
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("app", new JavaConnector());
                    new Thread(()->{
                        NesNetMain.connectServer((type, data) -> {
                            JSONObject nesData = JSONObject.parseObject(data);
                            switch (type){
                                case -1:
                                    String channelId = nesData.getString("channelId");
                                    Platform.runLater(() -> webEngine.executeScript("connected('"+channelId+"')"));
                                    break;
                            }
                        });
                    }).start();
                }
            });
            webViewIsLoad = true;
        }else {
            NesNetMain.send(0,null);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(new URL(getClass().getResource("/")+"RootLayout.fxml"));
        primaryStage.setTitle("My Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public void createRoomClick(ActionEvent actionEvent) {
        NesNetMain.send(1,null);
    }

    public void refreshRoomClick(ActionEvent actionEvent) {
        NesNetMain.send(0,null);
    }

    public void backHomeBtnClick(ActionEvent actionEvent) {
        multiPanel.setVisible(false);
        modelSelectPanel.setVisible(true);
        roomList.getItems().clear();
    }
}
