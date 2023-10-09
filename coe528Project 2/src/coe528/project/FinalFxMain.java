 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coe528.project;

/**
 *
 * @author danie
 */

import javafx.application.Application; 
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets; 
import javafx.geometry.Pos; 
import java.io.IOException;
import javafx.scene.Scene; 
import javafx.scene.control.Button; 
import javafx.scene.control.TextField; 
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import coe528.project.Customer;
import coe528.project.Books;
import coe528.project.SilverCustomer;
import coe528.project.Owner;
import java.util.function.Supplier;
import javafx.scene.Parent;

public class FinalFxMain extends Application {
    private Customer customer = null;
    private Owner owner = null;
    private double totalPrice;
    private int loadDataCount = 0;
    private static final int BUTTON_MAX_WIDTH = 100;
    private static final int SCENE_WIDTH = 400;
    private static final int SCENE_HEIGHT = 100;
    private static final String APP_TITLE = "BookStore App";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
   
    @Override
    public void start(Stage primaryStage) {      
        GridPane grid = createGridPane();
        Label welcome = new Label("Welcome to the BookStore App");
        Label user_id = new Label("User ID:");
        Label pass = new Label("Password:");  
        TextField usernameField = new TextField();  
        TextField passwordField = new TextField();  
        Button loginButton = new Button("Login");   
        grid.addRow(0, welcome);
        grid.addRow(1, user_id, usernameField);  
        grid.addRow(2, pass, passwordField);  
        grid.addRow(3, loginButton);
        loginButton.setMaxWidth(BUTTON_MAX_WIDTH);
        
        Scene scene = new Scene(grid, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        loginButton.setOnAction(event -> {
            String userName = usernameField.getText();
            String password = passwordField.getText();
            check(userName, password, primaryStage);
        });
        close(primaryStage); //if 'X' is clicked in top right, save data before close
    }
   private GridPane createGridPane() {
        GridPane grid = new GridPane();
        // Padding: top, right, bottom, left
        grid.setPadding(new Insets(0, 10, 10, 50));
        return grid;
    }
    @Override
    
    public void stop(){ //when 'X' is clicked, this method is called before termination
        System.out.println("Stage is closing");
        try { //filewriter throws exception
            owner.saveData(); //save the current customer and owner data
        } catch (IOException ex) {
            System.out.println("Error: Saving Data");
        }
        System.out.println("Saved Data");
    }
    public void close(Stage primaryStage){
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                //System.out.println("Saved Data");
                primaryStage.close(); //close main window
            }
        });
    }
    public void check(String username, String password, Stage primaryStage){
        System.out.println(username + " " + password);
        if(loadDataCount ==0){  //only load data once.
            owner = Owner.getOwnerInstance();  //only one owner instance and load data once
            try {
                owner.loadData();  //load customer and books previous data.
            } catch (IOException ex) {
                ex.getStackTrace();
            }
            loadDataCount++;
        }
        if(username.equals(DEFAULT_USERNAME) && password.equals(DEFAULT_PASSWORD)){ //check is it owner?
            ownerStage(primaryStage);   //owner main screen
        }else{ //check if its a cutomer, and get current customer instance
            customer = checkCustomer (username, password, owner.getCustomersList());
            if (customer == null){
                System.out.println("Invalid Username or Password");
                Alert invalidUser = new Alert(Alert.AlertType.ERROR);
                invalidUser.setContentText("Invalid username or password.");
                invalidUser.show();
            }
            else{
                System.out.println("Customer Username is: " + customer.getUsername());
                customerScreen(primaryStage);
            }
        }
    }
   
    public Customer checkCustomer (String username, String password, ObservableList<Customer> customers){
         //loop through list of customers
        for(Customer c: customers ){
            //if it matches return the customer
            if(c.getUsername().equals(username) && c.getPassword().equals(password)){
                return c;
            }
        }
        //if it doesn't match return null
        return null;
    }
    public void customerScreen(Stage primaryStage) {
    ObservableList<Books> books = owner.getBooks();

    Label label = createWelcomeLabel();
    TableView<Books> table = setupBooksTable(books);
    GridPane grid = setupButtonsGrid(primaryStage, this::buyBooksUsingCash, this::buyBooksUsingPoints);

    VBox root = new VBox(label, table, grid);
    setupScene(primaryStage, root, "BookStore App");

    close(primaryStage);
}

private Label createWelcomeLabel() {
    String welcomeText = String.format(
        "Welcome %s. You have %f points. Your Status is %s.",
        customer.getUsername(), customer.getPoints(), customer.getStatus().currentStatus()
    );
    return setupLabel(welcomeText);
}

private TableView<Books> setupBooksTable(ObservableList<Books> books) {
    TableView<Books> table = new TableView<>();
    table.setItems(books);
    table.getColumns().addAll(createTableColumn("Book Name", "bookName"),
        createTableColumn("Book Price", "bookPrice"),
        createTableColumn("Select", "Select")
    );
    return table;
}

private TableColumn<Books, ?> createTableColumn(String text, String property) {
    TableColumn<Books, ?> column = new TableColumn<>(text);
    column.setCellValueFactory(new PropertyValueFactory<>(property));
    column.setMinWidth(266);
    return column;
}

private GridPane setupButtonsGrid(Stage primaryStage, Supplier<Double> buyHandler, Supplier<Double> redeemHandler) {
    Button buy = createButton("Buy", event -> {
        double totalPrice = buyHandler.get();
        buyScreen(primaryStage, totalPrice);
    });

    Button redeem = createButton("Redeem Points & Buy", event -> {
        double totalPrice = redeemHandler.get();
        buyScreen(primaryStage, totalPrice);
    });

    Button logout = createButton("Logout", event -> start(primaryStage));

    return setupGridPane(buy, redeem, logout);
}

private Button createButton(String text, EventHandler<ActionEvent> handler) {
    Button button = new Button(text);
    button.setOnAction(handler);
    return button;
}

private void setupScene(Stage stage, Parent root, String title) {
    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.setTitle(title);
    stage.show();
}

private double buyBooksUsingCash() {
    double totalPrice = calculateTotalPrice(owner.getBooks());
    customer.buyCash(totalPrice);
    return totalPrice;
}

private double buyBooksUsingPoints() {
    double totalPrice = calculateTotalPrice(owner.getBooks());
    totalPrice = customer.buyPoints(totalPrice);
    return totalPrice;
}

private double calculateTotalPrice(ObservableList<Books> books) {
    return books.stream()
        .filter(book -> book.getSelect().isSelected())
        .mapToDouble(Books::getBookPrice)
        .sum();
}

public void buyScreen(Stage primaryStage, double totalPrice) {
    String labelText = String.format(
        "\n\n\nTotal Cost: %.2f. \nYou have %f points. \nYour Status is %s.",
        totalPrice, customer.getPoints(), customer.getStatus().currentStatus()
    );
    Label label = setupLabel(labelText);

    Button logout = createButton("Logout", event -> start(primaryStage));

    GridPane grid = setupGridPane(logout);
    VBox root = new VBox(label, grid);

    setupScene(primaryStage, root, "BookStore");

    close(primaryStage);
}

private Label setupLabel(String text) {
    Label label = new Label(text);
    label.setMaxWidth(Double.MAX_VALUE);
    AnchorPane.setLeftAnchor(label, 0.0);
    AnchorPane.setRightAnchor(label, 0.0);
    label.setAlignment(Pos.CENTER);
    return label;
}
public void ownerStage(Stage primaryStage) {
    GridPane root = setupGridPane(
        createButton("Customer", event -> ownerCustomerScreen(primaryStage), 200),
        createButton("Books", event -> rendersBooksScreen(primaryStage), 200),
        createButton("Logout", event -> start(primaryStage), 200)
    );

    setupScene(primaryStage, root, "BookStore", 300, 100);

    close(primaryStage);
}

public void rendersBooksScreen(Stage primaryStage) {
    ObservableList<Books> books = owner.getBooks();

    TableView<Books> booksTable = new TableView<>();
    booksTable.setItems(books);
    booksTable.getColumns().addAll(createTableColumn("Book Name", "bookName"),
        createTableColumn("Book Price", "bookPrice")
    );

    TextField inputName = createInputField("Name");
    TextField inputPrice = createInputField("Price");

    HBox inputsBox = new HBox(
        new Label("Name: "), inputName,
        new Label("Price: "), inputPrice,
        createButton("Add", event -> addBook(inputName, inputPrice, booksTable, books))
    );
    inputsBox.setPadding(new Insets(25, 25, 15, 25));
    inputsBox.setSpacing(10);

    HBox actionsBox = new HBox(
        createButton("Delete", event -> deleteSelectedBook(booksTable)),
        createButton("Back", event -> ownerStage(primaryStage))
    );
    actionsBox.setPadding(new Insets(0, 25, 25, 20));
    actionsBox.setSpacing(10);

    VBox root = new VBox(booksTable, inputsBox, actionsBox);

    setupScene(primaryStage, root, "BookStore");

    close(primaryStage);
}

private GridPane setupGridPane(Button... buttons) {
    GridPane grid = new GridPane();
    for (int i = 0; i < buttons.length; i++) {
        grid.addRow(i, buttons[i]);
    }
    grid.setPadding(new Insets(0, 0, 0,50));
    grid.setHgap(10);
    grid.setVgap(10);
    return grid;
}

private Button createButton(String text, EventHandler<ActionEvent> handler, int maxWidth) {
    Button button = new Button(text);
    button.setOnAction(handler);
    button.setMaxWidth(maxWidth);
    return button;
}

private void setupScene(Stage stage, Parent root, String title, int width, int height) {
    Scene scene = new Scene(root, width, height);
    stage.setScene(scene);
    stage.setTitle(title);
    stage.show();
}

private TableColumn<Books, ?> createTableColumn(String text, String property, int minWidth) {
    TableColumn<Books, ?> column = new TableColumn<>(text);
    column.setCellValueFactory(new PropertyValueFactory<>(property));
    column.setMinWidth(minWidth);
    return column;
}

private TextField createInputField(String placeholder) {
    TextField textField = new TextField();
    textField.setPromptText(placeholder);
    textField.setMinWidth(100);
    return textField;
}

private void addBook(TextField inputName, TextField inputPrice, TableView<Books> table, ObservableList<Books> books) {
    String bookName = inputName.getText();
    String bookPrice = inputPrice.getText();
    try {
        if (isBookUnique(bookName, Double.parseDouble(bookPrice), books)) {
            Books b = new Books(bookName, Double.parseDouble(bookPrice));
            table.getItems().add(b);
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Invalid Input");
    } catch (NumberFormatException e) {
        showAlert(Alert.AlertType.ERROR, "Invalid Input.");
    } finally {
        inputName.clear();
        inputPrice.clear();
    }
}

private void deleteSelectedBook(TableView<Books> table) {
    ObservableList<Books> allBooks = table.getItems();
    ObservableList<Books> selectedBook = table.getSelectionModel().getSelectedItems();
    selectedBook.forEach(allBooks::remove);
}

private void showAlert(Alert.AlertType type, String content) {
    Alert alert = new Alert(type);
    alert.setContentText(content);
    alert.show();
}

private TableColumn<Customer, ?> createTableColumn2(String text, String property) {
    TableColumn<Customer, ?> column = new TableColumn<>(text);
    column.setCellValueFactory(new PropertyValueFactory<>(property));
    return column;
}
public void ownerCustomerScreen(Stage primaryStage) {
    ObservableList<Customer> customers = owner.getCustomersList();
    TableView<Customer> table = new TableView<>();
    table.setItems(customers);
    table.getColumns().addAll(createTableColumn2("Username", "username"),
        createTableColumn2("Password", "password"),
        createTableColumn2("Points", "points")
    );

    TextField addUsername = createInputField("Username");
    TextField addPassword = createInputField("Password");

    GridPane grid = new GridPane();
    grid.addRow(0, new Label("Username"), addUsername, new Label("Password"), addPassword,
        createButton("Add", event -> addCustomer(addUsername, addPassword, table, customers)));
    grid.addRow(1,
        createButton("Delete", event -> deleteSelectedCustomer(table)),
        createButton("Back", event -> goBackToOwnerStage(primaryStage, table)));
    grid.setPadding(new Insets(10,10,10,10));
    grid.setHgap(10);
    grid.setVgap(10);

    VBox root = new VBox(table, grid);

    setupScene(primaryStage, root, "BookStore");

    close(primaryStage);
}

private void addCustomer(TextField addUsername, TextField addPassword, TableView<Customer> table, ObservableList<Customer> customers) {
    String name = addUsername.getText();
    String password = addPassword.getText();
    if (validateCustomerUsername(name, password, customers)) {
        Customer c = new Customer(name, password, 0);
        c.setState(new SilverCustomer());
        table.getItems().add(c);
        owner.setCustomersList(table.getItems());
        addUsername.clear();
        addPassword.clear();
    } else {
        showAlert(Alert.AlertType.ERROR, "Invalid Input");
        addUsername.clear();
        addPassword.clear();
    }
}

private void deleteSelectedCustomer(TableView<Customer> table) {
    ObservableList<Customer> allCustomers = table.getItems();
    ObservableList<Customer> selectedCustomer = table.getSelectionModel().getSelectedItems();
    selectedCustomer.forEach(allCustomers::remove);
    owner.setCustomersList(table.getItems());
}

private void goBackToOwnerStage(Stage primaryStage, TableView<Customer> table) {
    owner.setCustomersList(table.getItems());
    ownerStage(primaryStage);
}

public boolean validateCustomerUsername(String name, String password, ObservableList<Customer> customers) {
    if (name.equals("") || password.equals("")) {
        return false;
    }
    return customers.stream().noneMatch(c -> c.getUsername().equals(name));
}

public boolean isBookUnique(String bookName, double bookPrice, ObservableList<Books> books) {
    if (bookName.equals("") || bookPrice <= 0) {
        return false;
    }
    return books.stream().noneMatch(book -> book.getBookName().equals(bookName));
}
 /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}