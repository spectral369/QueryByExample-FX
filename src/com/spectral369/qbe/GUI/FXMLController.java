package com.spectral369.qbe.GUI;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import com.spectral369.functionality.DatabasesAvailable;
import com.spectral369.functionality.QueryByExampleAPI;
import com.spectral369.functionality.QueryData;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.StageStyle;

public class FXMLController implements Initializable {

    @FXML
    private TextField usernameForm;
    @FXML
    private TextField portForm;
    @FXML
    private TextField oracleSID;
    @FXML
    private Button loginBtn;
    @FXML
    private Label loginStatus;
    @FXML
    private ComboBox<String> databaseSelect;
    @FXML
    private PasswordField passwordForm;
    @FXML
    private ComboBox<String> databaseCombo;
    @FXML
    private Circle circleStatus;
    @FXML
    private ListView<String> tableList;
    @FXML
    private ListView<String> columnList;
    @FXML
    private TableView<List<String>> queryTable;
    @FXML
    private TextField queryForm;
    @FXML
    private Button queryBtn;
    @FXML
    private TabPane tabPane;
    @FXML
    private Label sidLabel;
    @FXML
    private TextField serverField;
    @FXML
    private MenuItem exportPDF;
    @FXML
    private MenuItem exportXML;

    QueryByExampleAPI api;
    ObservableList observableTableList = null;
    ObservableList observableColumnList = null;
    QueryData queryData = null;

    TableColumn<List<String>, String> tc;
    ObservableList<List<String>> data = null;
    AtomicInteger in = new AtomicInteger(0);
    final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    private Thread loadingThread = null;
    Stage loadingDialog = null;
    // int i = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usernameForm.setTooltip(new Tooltip("Please enter database username..."));
        passwordForm.setTooltip(new Tooltip("Please enter database password..."));
        portForm.setTooltip(new Tooltip("Please enter database port..."));
        portForm.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                portForm.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        sidLabel.setVisible(false);
        oracleSID.setVisible(false);

        tabPane.getTabs().get(tabPane.getTabs().size() - 1).setDisable(true);

        for (DatabasesAvailable s : DatabasesAvailable.values()) {
            databaseSelect.getItems().add(s.toString());
        }
        databaseSelect.getSelectionModel().selectFirst();

        validate(usernameForm, 3);
        validate(portForm, 2);
        validate(serverField, 4);
        validate(passwordForm, 3);

        loginBtn.disableProperty().bind(Bindings.createBooleanBinding(()
                -> usernameForm.getText().trim().isEmpty() || usernameForm.getText().trim().length() < 3,
                usernameForm.textProperty()).or(Bindings.createBooleanBinding(()
                -> portForm.getText().trim().isEmpty() || portForm.getText().trim().length() < 2,
                portForm.textProperty()).or(Bindings.createBooleanBinding(()
                -> passwordForm.getText().trim().isEmpty() || passwordForm.getText().trim().length() < 3,
                passwordForm.textProperty())).or(Bindings.createBooleanBinding(()
                -> serverField.getText().trim().isEmpty() || serverField.getText().trim().length() < 4,
                serverField.textProperty()))
        ));
        //test

        queryBtn.disableProperty().setValue(Boolean.TRUE);//testing
        queryForm.disableProperty().setValue(Boolean.TRUE);

        observableTableList = FXCollections.observableArrayList();

        observableColumnList = FXCollections.observableArrayList();

        //test
        try {
            api = new QueryByExampleAPI();
            api.setDatabase(DatabasesAvailable.MYSQL);
        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        loginBtn.setDefaultButton(true);

        databaseCombo.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            handleDatabaseChange();
        });
        tableList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            handleTableItemChange();
        });

        columnList.getSelectionModel().selectionModeProperty().setValue(SelectionMode.MULTIPLE);
        columnList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            handleColumnChange();
        });
        queryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        queryTable.setEditable(false);

        circleStatus.setFill(Color.GREEN);
        Tooltip tooltip = new Tooltip("Connection Status");
        Tooltip.install(circleStatus, tooltip);

        exportPDF.disableProperty().bind(Bindings.isEmpty(queryTable.getColumns()));
        exportXML.disableProperty().bind(Bindings.isEmpty(queryTable.getColumns()));

    }

    @FXML
    public void handleLogin() {
        if (databaseSelect.getSelectionModel().getSelectedItem().equals(DatabasesAvailable.ORACLE.toString())) {
            if (oracleSID.getText().isEmpty() || oracleSID.getText().length() < 2) {

                oracleSID.pseudoClassStateChanged(errorClass, true);
                oracleSID.requestFocus();
                return;
            } else {
                oracleSID.pseudoClassStateChanged(errorClass, false);

            }
        }
        loading(true);

        switch (databaseSelect.getSelectionModel().getSelectedItem()) {
        //other dbs
            case "MYSQL":
                api.setMYSQLServerInformation(usernameForm.getText(),
                        String.copyValueOf(passwordForm.getText().toCharArray()),
                        serverField.getText(), Integer.parseInt(portForm.getText()));
                api.beginSession();
                if (api.getConnection() == null) {
                    loginStatus.getStyleClass().clear();
                    loginStatus.getStyleClass().add("redS");
                    loginStatus.setText("Connection Failed");
                    loginStatus.setTooltip(new Tooltip("Server could not be reached !"));
                    
                } else if (!api.checkSQLConnection()) {
                    loginStatus.getStyleClass().clear();
                    loginStatus.getStyleClass().add("redS");
                    loginStatus.setText("Connection Failed");
                    loginStatus.setTooltip(new Tooltip("Login info invalid !"));
                    
                } else {
                    
                    try {
                        loginStatus.getStyleClass().clear();
                        loginStatus.getStyleClass().add("greenS");
                        loginStatus.setText("Connection Success");
                        
                        databaseCombo.getItems().addAll(api.showSQLAvailableSchemas());
                        databaseCombo.getSelectionModel().selectFirst();
                    } catch (Exception e) {
                        
                        loading(false);
                        e.getMessage();
                        Popup popup = new Popup();
                        popup.getContent().add(new Label("NO databases available \n for current user"));
                        
                    }
                    
                    tabPane.getTabs().remove(0);
                    tabPane.getTabs().get(tabPane.getTabs().size() - 1).setDisable(false);
                    tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
                    
                }   break;
            case "ORACLE":
                api.setOracleServerInformation(usernameForm.getText(),
                        String.copyValueOf(passwordForm.getText().toCharArray()),
                        serverField.getText(), Integer.parseInt(portForm.getText()), oracleSID.getText());
                api.beginSession();
                if (api.getConnection() == null) {
                    loginStatus.getStyleClass().clear();
                    loginStatus.getStyleClass().add("redS");
                    loginStatus.setText("Connection Failed");
                    loginStatus.setTooltip(new Tooltip("Server could not be reached !"));
                    
                } else if (!api.checkOracleConnection()) {
                    loginStatus.getStyleClass().clear();
                    loginStatus.getStyleClass().add("redS");
                    loginStatus.setText("Connection Failed");
                    loginStatus.setTooltip(new Tooltip("Login info invalid !"));
                    
                } else {
                    
                    try {
                        loginStatus.getStyleClass().clear();
                        loginStatus.getStyleClass().add("greenS");
                        loginStatus.setText("Connection Success");
                        
                        databaseCombo.getItems().addAll(api.getOracleStringArraySchemas());
                        databaseCombo.getSelectionModel().selectFirst();
                    } catch (Exception e) {
                        
                        loading(false);
                        e.getMessage();
                        Popup popup = new Popup();
                        popup.getContent().add(new Label("NO databases available \n for current user"));
                        
                    }
                    
                    tabPane.getTabs().remove(0);
                    tabPane.getTabs().get(tabPane.getTabs().size() - 1).setDisable(false);
                    tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
                    
                }   break;
            case "MONGO":
                api.setMongoServerInformation(usernameForm.getText(),
                        String.copyValueOf(passwordForm.getText().toCharArray()),
                        serverField.getText(), Integer.parseInt(portForm.getText()));
                api.beginSession();
                if (!api.checkMongoServerConnection()) {
                    loginStatus.getStyleClass().clear();
                    loginStatus.getStyleClass().add("redS");
                    loginStatus.setText("Connection Failed(Server)");
                    loginStatus.setTooltip(new Tooltip("Server could not be reached !"));
                    loading(false);
                    return;
                    
                } else if (!api.checkMongoIsLogin()) {
                    loginStatus.getStyleClass().clear();
                    loginStatus.getStyleClass().add("redS");
                    loginStatus.setText("Connection Failed(Login)");
                    loginStatus.setTooltip(new Tooltip("Login info invalid !"));
                    loading(false);
                    return;
                } else {
                    try {
                        loginStatus.getStyleClass().clear();
                        loginStatus.getStyleClass().add("greenS");
                        loginStatus.setText("Connection Success");
                        
                        databaseCombo.getItems().addAll(api.getAvailableMongoDatabases());
                        databaseCombo.getSelectionModel().selectFirst();
                    } catch (Exception e) {
                        
                        loading(false);
                        e.getMessage();
                        Popup popup = new Popup();
                        popup.getContent().add(new Label("NO databases available \n for current user"));
                        
                    }
                    
                    tabPane.getTabs().remove(0);
                    tabPane.getTabs().get(tabPane.getTabs().size() - 1).setDisable(false);
                    tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
                    
                }   break;
            default:
                break;
        }

        //other dbs
        //extra init
        loading(false);

    }

    @FXML
    public void handleChangeDB() {
        loginStatus.getStyleClass().clear();
        loginStatus.setText("");
        if (databaseSelect.getSelectionModel().getSelectedItem().equals(DatabasesAvailable.ORACLE.toString())) {
            oracleSID.setVisible(true);
            sidLabel.setVisible(true);
            api.setDatabase(DatabasesAvailable.ORACLE);

        } else if (databaseSelect.getSelectionModel().getSelectedItem().equals(DatabasesAvailable.MYSQL.toString())) {
            oracleSID.setVisible(false);
            sidLabel.setVisible(false);
            api.setDatabase(DatabasesAvailable.MYSQL);
        } else {
            oracleSID.setVisible(false);
            sidLabel.setVisible(false);
            api.setDatabase(DatabasesAvailable.MONGO);
        }
    }

    public void handleColumnChange() {

        //   colsSelected = !columnList.getSelectionModel().isEmpty();
        queryBtn.disableProperty().setValue(Boolean.FALSE);//testing
        queryForm.disableProperty().setValue(Boolean.FALSE);
        queryBtn.setDefaultButton(true);

    }

    public void handleTableItemChange() {
        queryTable.getItems().clear();
        queryTable.getColumns().clear();

        switch (api.getDatabaseAvailable().toString()) {
            case "MYSQL":
                if (tableList.getSelectionModel().getSelectedItem() != null) {
                    observableColumnList.clear();
                    columnList.getItems().clear();
                    observableColumnList.addAll(api.getSQLColumnsFromTable(tableList.getSelectionModel().getSelectedItem()));
                    columnList.setItems(observableColumnList);
                    if (columnList.getItems().size() > 0) {
                        columnList.getSelectionModel().selectFirst();
                    } else {
                        
                        queryBtn.disableProperty().setValue(Boolean.TRUE);//testing
                        queryForm.disableProperty().setValue(Boolean.TRUE);
                    }
                    
                }   break;
        //other dbs
            case "ORACLE":
                if (tableList.getSelectionModel().getSelectedItem() != null) {
                    observableColumnList.clear();
                    columnList.getItems().clear();
                    observableColumnList.addAll(api.getOracleColumns(tableList.getSelectionModel().getSelectedItem()));
                    columnList.setItems(observableColumnList);
                    if (columnList.getItems().size() > 0) {
                        columnList.getSelectionModel().selectFirst();
                    } else {
                        
                        queryBtn.disableProperty().setValue(Boolean.TRUE);//testing
                        queryForm.disableProperty().setValue(Boolean.TRUE);
                    }
                    
                }   break;
            case "MONGO":
                if (tableList.getSelectionModel().getSelectedItem() != null) {
                    observableColumnList.clear();
                    columnList.getItems().clear();
                    observableColumnList.addAll(api.getMongoColumnsFromTable(tableList.getSelectionModel().getSelectedItem()));
                    columnList.setItems(observableColumnList);
                    if (columnList.getItems().size() > 0) {
                        columnList.getSelectionModel().selectFirst();
                    } else {
                        
                        queryBtn.disableProperty().setValue(Boolean.TRUE);//testing
                        queryForm.disableProperty().setValue(Boolean.TRUE);
                    }
                    
                }   break;
            default:
                break;
        }
    }

    public void handleDatabaseChange() {

        if (api.getDatabaseAvailable().toString().equals("MYSQL")) {
            try {

                api.changeSQLDatabase(databaseCombo.getSelectionModel().getSelectedItem());
                observableTableList.clear();
                tableList.getItems().clear();
                observableTableList.addAll(api.getSQLArrayListTables());
                tableList.setItems(observableTableList);
                if (tableList.getItems().size() > 0) {
                    tableList.getSelectionModel().selectFirst();
                }
            } catch (SQLException e) {
                e.getMessage();
            }

        } else if (api.getDatabaseAvailable().toString().equals("ORACLE")) {
            try {

                api.getOracleTables(databaseCombo.getSelectionModel().getSelectedItem());
                observableTableList.clear();
                tableList.getItems().clear();
                observableTableList.addAll(api.getSQLArrayListTables());
                tableList.setItems(observableTableList);
                if (tableList.getItems().size() > 0) {
                    tableList.getSelectionModel().selectFirst();
                }
            } catch (Exception e) {
                e.getMessage();
            }

        }
        if (api.getDatabaseAvailable().toString().equals("MONGO")) {
            try {

                api.changeMongoDatabase(databaseCombo.getSelectionModel().getSelectedItem());
                observableTableList.clear();
                tableList.getItems().clear();
                observableTableList.addAll(api.getMongoCollectionList());
                tableList.setItems(observableTableList);
                if (tableList.getItems().size() > 0) {
                    tableList.getSelectionModel().selectFirst();
                }
            } catch (Exception e) {
                e.getMessage();
            }

        }
    }

    @FXML
    public void handleQBE() {
        loading(true);

        try {
            if (api.getDatabaseAvailable().equals(DatabasesAvailable.MONGO)) {
               
            } else if (!api.getConnection().isValid(2000)) {
                circleStatus.setFill(Color.RED);
            } else {
                circleStatus.setFill(Color.GREEN);
            }
        } catch (SQLException e1) {
            loading(false);
            circleStatus.setFill(Color.RED);
            e1.getMessage();
        }
        switch (api.getDatabaseAvailable().toString()) {
            case "MYSQL":
                queryTable.getItems().clear();
                queryTable.getColumns().clear();
                queryTable.getSortOrder().clear();
                if (columnList.getItems().size() < 1) {
                    queryForm.setText("table contains no columns");
                    queryForm.disableProperty().setValue(Boolean.TRUE);
                    queryBtn.disableProperty().setValue(Boolean.TRUE);
                }   queryData = new QueryData();
                if (columnList.getSelectionModel().getSelectedItems().size() > 0) {
                    queryData = api.SQLQBE(databaseCombo.getSelectionModel().getSelectedItem(),
                            tableList.getSelectionModel().getSelectedItem(),
                            queryForm.getText().trim(),
                            columnList.getSelectionModel().getSelectedItems());
                } else {
                    queryData = api.SQLQBE(databaseCombo.getSelectionModel().getSelectedItem(),
                            tableList.getSelectionModel().getSelectedItem(),
                            queryForm.getText().trim(),
                            columnList.getItems());
                }   data = FXCollections.observableArrayList();
                for (List<String> z : queryData.getData()) {
                    List<String> a1 = new ArrayList<>();
                    for (String x : z) {
                        
                        a1.add(x);
                    }
                    data.add(a1);
                }   for (int i = 0; i < queryData.getQBECols().size(); i++) {
                    final int finalIdx = i;
                    tc = new TableColumn<>(queryData.getQBECols().get(i));
                    //  tc.setSortable(false);
                    
                    tc.setCellValueFactory((TableColumn.CellDataFeatures<List<String>, String> param) -> {
                        if (in.get() >= queryData.getQBECols().size()) {
                            in.set(0);
                        }
                        
                        return new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx));
                    });
                    
                    queryTable.getColumns().add(tc);
                    
                }   for (int i = 0; i < data.size(); i++) {
                    queryTable.getItems().add(
                            FXCollections.observableArrayList(
                                    data.get(i)
                            )
                    );
                }
                
                //   queryTable.getSortOrder().add(queryTable.getColumns().get(0));
                break;
            case "ORACLE":
                queryTable.getItems().clear();
                queryTable.getColumns().clear();
                queryTable.getSortOrder().clear();
                if (columnList.getItems().size() < 1) {
                    queryForm.setText("table contains no columns");
                    queryForm.disableProperty().setValue(Boolean.TRUE);
                    queryBtn.disableProperty().setValue(Boolean.TRUE);
                }   queryData = new QueryData();
                if (columnList.getSelectionModel().getSelectedItems().size() > 0) {
                    queryData = api.OracleQBE(databaseCombo.getSelectionModel().getSelectedItem(),
                            tableList.getSelectionModel().getSelectedItem(),
                            queryForm.getText().trim(),
                            columnList.getSelectionModel().getSelectedItems());
                } else {
                    queryData = api.OracleQBE(databaseCombo.getSelectionModel().getSelectedItem(),
                            tableList.getSelectionModel().getSelectedItem(),
                            queryForm.getText().trim(),
                            columnList.getItems());
                }   data = FXCollections.observableArrayList();
                for (List<String> z : queryData.getData()) {
                    List<String> a1 = new ArrayList<>();
                    for (String x : z) {

                        a1.add(x);
                    }
                    data.add(a1);
                }   for (int i = 0; i < queryData.getQBECols().size(); i++) {
                    final int finalIdx = i;
                    tc = new TableColumn<>(queryData.getQBECols().get(i));
                    //  tc.setSortable(false);

                    tc.setCellValueFactory((TableColumn.CellDataFeatures<List<String>, String> param) -> {
                        if (in.get() >= queryData.getQBECols().size()) {
                            in.set(0);
                        }

                        return new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx));
                    });

                    queryTable.getColumns().add(tc);
                    
                }   for (int i = 0; i < data.size(); i++) {
                    queryTable.getItems().add(
                            FXCollections.observableArrayList(
                                    data.get(i)
                            )
                    );
                }
                
                //   queryTable.getSortOrder().add(queryTable.getColumns().get(0));
                break;
            case "MONGO":
                try {
                    queryTable.getItems().clear();
                    queryTable.getColumns().clear();
                    queryTable.getSortOrder().clear();
                    if (columnList.getItems().size() < 1) {
                        queryForm.setText("table contains no columns");
                        queryForm.disableProperty().setValue(Boolean.TRUE);
                        queryBtn.disableProperty().setValue(Boolean.TRUE);
                    }
                    queryData = new QueryData();
                    
                    if (columnList.getSelectionModel().getSelectedItems().size() > 0) {
                        queryData = api.mongoQBE(databaseCombo.getSelectionModel().getSelectedItem(),
                                tableList.getSelectionModel().getSelectedItem(),
                                queryForm.getText().trim(),
                                columnList.getSelectionModel().getSelectedItems());
                    } else {
                        queryData = api.mongoQBE(databaseCombo.getSelectionModel().getSelectedItem(),
                                tableList.getSelectionModel().getSelectedItem(),
                                queryForm.getText().trim(),
                                columnList.getItems());
                    }
                    
                    data = FXCollections.observableArrayList();
                    
                    for (List<String> z : queryData.getData()) {
                        List<String> a1 = new ArrayList<>();
                        for (String x : z) {
                            
                            a1.add(x);
                        }
                        data.add(a1);
                    }
                    
                    for (int i = 0; i < queryData.getQBECols().size(); i++) {
                        final int finalIdx = i;
                        tc = new TableColumn<>(queryData.getQBECols().get(i));
                        //  tc.setSortable(false);
                        
                        tc.setCellValueFactory((TableColumn.CellDataFeatures<List<String>, String> param) -> {
                            if (in.get() >= queryData.getQBECols().size()) {
                                in.set(0);
                            }
                            
                            return new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx));
                        });
                        
                        queryTable.getColumns().add(tc);
                        
                    }
                    
                    for (int i = 0; i < data.size(); i++) {
                        queryTable.getItems().add(
                                FXCollections.observableArrayList(
                                        data.get(i)
                                )
                        );
                    }
                } catch (Exception e) {
                    loading(false);
                }
                
                //   queryTable.getSortOrder().add(queryTable.getColumns().get(0));
                break;
            default:
                break;
        }

        loading(false);
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }

    @FXML
    public void handleRestart() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
        Platform.runLater(() -> {
            try {
                new MainApp().start(new Stage());
            } catch (Exception ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    @FXML
    public void handlePDF() {
        api.ExportToPDFDefault(queryData);
    }

    @FXML
    public void handleXML() {
        api.ExportToXMLDefault(queryData);
    }

    @FXML
    public void handleAbout() {
        final Stage aboutStage = new Stage();

        aboutStage.initModality(Modality.APPLICATION_MODAL);
        aboutStage.initOwner(tabPane.getScene().getWindow());
        aboutStage.resizableProperty().setValue(Boolean.FALSE);

        aboutStage.setResizable(false);
        aboutStage.setMaximized(false);
        aboutStage.setFullScreen(false);

        aboutStage.iconifiedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            aboutStage.close();
        });
        VBox dialogVbox = new VBox(8);
        HBox hn = new HBox(10);
        HBox ha = new HBox(10);
        HBox he = new HBox(10);
        Label name = new Label("Query By Example");
        hn.getChildren().add(name);

        Label author = new Label("Spectral369");
        ha.getChildren().add(author);
        Button exit = new Button("Exit");
        he.getChildren().add(exit);
        exit.setOnAction((ActionEvent e) -> {
            aboutStage.close();
        });
        hn.setPadding(new Insets(5, 0, 2, 10));
        ha.setPadding(new Insets(5, 0, 2, 20));
        he.setPadding(new Insets(5, 0, 2, 92));
        dialogVbox.getChildren().addAll(hn, ha, he);

        Scene infoScene = new Scene(dialogVbox, 140, 110);

        aboutStage.setScene(infoScene);
        aboutStage.show();
    }

    private void validate(TextField form, int par) {

        form.textProperty().addListener((observable, oldValue, newValue) -> {
            if (form.getText().trim().isEmpty() || form.getText().trim().length() < par) { // we only care about loosing focus
                form.pseudoClassStateChanged(errorClass, true);
            } else {
                form.pseudoClassStateChanged(errorClass, false);
            }

        });
    }

    private void loading(boolean x) {

        if (x) {
            loadingDialog = new Stage(StageStyle.UNDECORATED);

            loadingThread = new Thread(new Task() {
                @Override
                protected Object call() throws Exception {

                    Platform.runLater(() -> {

                        ProgressIndicator p1 = new ProgressIndicator();
                        p1.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                        VBox vbox = new VBox();

                        Scene dScene = new Scene(vbox, 300, 200);
                        //  dialog.initOwner(usernameForm.getScene().getWindow());
                        if (loadingDialog.getOwner() == null) {
                            loadingDialog.initOwner(circleStatus.getScene().getWindow());
                            loadingDialog.initModality(Modality.APPLICATION_MODAL);
                        }
                        vbox.setPrefSize(160, 100);
                        vbox.getChildren().add(p1);
                        Label l = new Label("Loading...");
                        l.setPadding(new Insets(10, 0, 2, 10));

                        vbox.getChildren().add(l);
                        vbox.setAlignment(Pos.CENTER);
                        loadingDialog.setScene(dScene);
                        loadingDialog.setTitle("Loading...");

                        loadingDialog.show();

                    });

                    return null;
                }
            });
            loadingThread.setPriority(Thread.MAX_PRIORITY);//testing...
            loadingThread.start();

        } else {
            Platform.runLater(() -> {
              
                if (loadingDialog != null) {
                    loadingDialog.close();

                }
                if (loadingThread != null) {
                    loadingThread.interrupt();

                }

            });

            //
        }

    }

}
