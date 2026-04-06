import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ShipBattleGUI extends Application {

    // used for font sizing, title is 3x this
    private static final int BASE_FONT = 16;

    private Board playerBoard = new Board();
    private Button[][] playerButtons = new Button[10][10];

    // bot board is just for display right now
    private Board botBoard = new Board();
    private Button[][] botButtons = new Button[10][10];

    private ComboBox<String> shipSelector = new ComboBox<>();

    @Override
    public void start(Stage stage) {

        Label titleLabel = new Label("SHIP BATTLE");
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT * 3));
        titleLabel.setTextFill(Color.WHITE);

        shipSelector.getItems().addAll("Carrier", "Battleship", "Destroyer", "Submarine", "Frigate");
        shipSelector.setValue("Carrier");
        shipSelector.setPrefWidth(160);
        styleComboBox(shipSelector);

        VBox topBox = new VBox(8, titleLabel, shipSelector);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(16, 0, 10, 0));

        // left side - player
        Label playerLabel = new Label("PLAYER");
        playerLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT));
        playerLabel.setTextFill(Color.WHITE);

        GridPane playerGrid = buildGrid(playerButtons, true);
        VBox playerBox = new VBox(6, playerLabel, playerGrid);
        playerBox.setAlignment(Pos.CENTER);

        // right side - bot
        Label botLabel = new Label("BOT");
        botLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT));
        botLabel.setTextFill(Color.WHITE);

        GridPane botGrid = buildGrid(botButtons, false);
        VBox botBox = new VBox(6, botLabel, botGrid);
        botBox.setAlignment(Pos.CENTER);

        HBox boardsBox = new HBox(40, playerBox, botBox);
        boardsBox.setAlignment(Pos.CENTER);
        boardsBox.setPadding(new Insets(10, 24, 20, 24));

        VBox root = new VBox(topBox, boardsBox);
        root.setStyle("-fx-background-color: #2B2B2B;");

        updateBoard(playerBoard, playerButtons);
        updateBoard(botBoard, botButtons);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Ship Battle");
        stage.show();
    }

    // builds the grid for either board, interactive controls whether clicking does anything
    private GridPane buildGrid(Button[][] buttons, boolean interactive) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setPadding(new Insets(4));
        grid.setStyle("-fx-background-color: #546E7A; -fx-background-radius: 4;");

        // column numbers along the top
        for (int col = 0; col < 10; col++) {
            Label lbl = new Label(String.valueOf(col + 1));
            lbl.setPrefSize(44, 20);
            lbl.setAlignment(Pos.CENTER);
            lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
            lbl.setTextFill(Color.WHITE);
            grid.add(lbl, col + 1, 0);
        }

        // row letters down the side + the actual cells
        for (int row = 0; row < 10; row++) {
            char rowChar = (char) ('A' + row);
            Label rowLbl = new Label(String.valueOf(rowChar));
            rowLbl.setPrefSize(20, 44);
            rowLbl.setAlignment(Pos.CENTER);
            rowLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
            rowLbl.setTextFill(Color.WHITE);
            grid.add(rowLbl, 0, row + 1);

            for (int col = 0; col < 10; col++) {
                Button btn = new Button();
                btn.setPrefSize(44, 44);
                btn.setFont(Font.font("Georgia", FontWeight.BOLD, 13));

                if (interactive) {
                    int r = row, c = col;
                    btn.setOnAction(e -> onPlayerClick(r, c));
                } else {
                    // disable bot cells, opacity fix stops javafx from greying them out
                    btn.setDisable(true);
                    btn.setOpacity(1.0);
                }

                buttons[row][col] = btn;
                grid.add(btn, col + 1, row + 1);
            }
        }

        return grid;
    }

    private void onPlayerClick(int row, int col) {
        playerBoard.addShipByName(playerBoard.toCoord(row, col), shipSelector.getValue());
        updateBoard(playerBoard, playerButtons);
    }

    // redraws all buttons to match the board state, Cell handles its own symbol/color
    private void updateBoard(Board board, Button[][] buttons) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Cell cell = board.getCell(row, col);
                Button btn = buttons[row][col];
                btn.setText(cell.getSymbol());
                btn.setStyle(
                    "-fx-background-color: " + cell.getColor() + ";" +
                    "-fx-text-fill: " + cell.getTextColor() + ";" +
                    "-fx-background-radius: 3;" +
                    "-fx-border-color: rgba(0,0,0,0.15);" +
                    "-fx-border-radius: 3;"
                );
            }
        }
    }

    // colors the dropdown to match each ship, had to do this twice for the open list and closed button
    private void styleComboBox(ComboBox<String> box) {
        box.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setFont(Font.font("Georgia", FontWeight.BOLD, 13));
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: " + shipNameToColor(item) + ";");
            }
        });

        // this one styles the box when its closed
        box.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setFont(Font.font("Georgia", FontWeight.BOLD, 13));
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: " + shipNameToColor(item) + ";");
            }
        });

        // update color when selection changes
        box.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) box.setStyle("-fx-background-color: " + shipNameToColor(newVal) + ";");
        });
        box.setStyle("-fx-background-color: " + shipNameToColor(box.getValue()) + ";");
    }

    // gets the color for a ship name by asking the Cell enum directly
    private String shipNameToColor(String name) {
        switch (name) {
            case "Carrier":    return Cell.CARRIER.getColor();
            case "Battleship": return Cell.BATTLESHIP.getColor();
            case "Destroyer":  return Cell.DESTROYER.getColor();
            case "Submarine":  return Cell.SUBMARINE.getColor();
            case "Frigate":    return Cell.FRIGATE.getColor();
            default:           return "#37474F";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}