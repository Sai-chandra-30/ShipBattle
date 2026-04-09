import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShipBattleGUI extends Application {

    private static final int BASE_FONT = 16;
    private static final int CELL_SIZE = 44;
    private static final int TRAY_CELL = 36;

    private Board playerBoard = new Board();
    private Button[][] playerButtons = new Button[10][10];

    private Board botBoard = new Board();
    private Button[][] botButtons = new Button[10][10];

    // Drag-and-drop state
    private boolean isVertical = false;
    private String draggingShipName = null;
    private boolean dragFromBoard = false;
    private int dragFromRow = -1, dragFromCol = -1;
    private boolean dragIsVertical = false;
    private final List<int[]> highlightedCells = new ArrayList<>();

    // Ship tray
    private final Map<String, VBox> shipTrayItems = new LinkedHashMap<>();
    private FlowPane shipTray;
    private Button orientationToggle;

    @Override
    public void start(Stage stage) {

        Label titleLabel = new Label("SHIP BATTLE");
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT * 3));
        titleLabel.setTextFill(Color.WHITE);

        VBox topBox = new VBox(8, titleLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(16, 0, 10, 0));

        // --- PLAYER side ---
        Label playerLabel = new Label("PLAYER");
        playerLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT));
        playerLabel.setTextFill(Color.WHITE);

        GridPane playerGrid = buildGrid(playerButtons, true);

        // Orientation toggle
        orientationToggle = new Button("HORIZONTAL");
        orientationToggle.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        orientationToggle.setStyle(
            "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
            "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        orientationToggle.setOnAction(e -> {
            isVertical = !isVertical;
            orientationToggle.setText(isVertical ? "VERTICAL" : "HORIZONTAL");
        });

        // Ship tray below player board
        Label trayLabel = new Label("unplaced ships");
        trayLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        trayLabel.setTextFill(Color.rgb(180, 180, 180));

        shipTray = new FlowPane(8, 8);
        shipTray.setPadding(new Insets(8, 0, 8, 0));
        shipTray.setPrefWrapLength(520);
        buildShipTrayItems();

        // Tray is a drop zone — ships dragged off the board return here
        shipTray.setOnDragOver(e -> {
            if (e.getDragboard().hasString() && dragFromBoard)
                e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        });
        shipTray.setOnDragDropped(e -> {
            boolean success = false;
            if (e.getDragboard().hasString() && dragFromBoard && draggingShipName != null) {
                playerBoard.removeShipAt(dragFromRow, dragFromCol);
                updateBoard(playerBoard, playerButtons);
                showInTray(draggingShipName);
                success = true;
            }
            clearPreview();
            e.setDropCompleted(success);
            e.consume();
        });

        HBox trayControls = new HBox(12, trayLabel, orientationToggle);
        trayControls.setAlignment(Pos.CENTER_LEFT);
        trayControls.setPadding(new Insets(8, 0, 0, 0));

        VBox playerSide = new VBox(6, playerLabel, playerGrid, trayControls, shipTray);
        playerSide.setAlignment(Pos.CENTER_LEFT);

        // --- BOT side ---
        Label botLabel = new Label("BOT");
        botLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT));
        botLabel.setTextFill(Color.WHITE);

        GridPane botGrid = buildGrid(botButtons, false);
        VBox botSide = new VBox(6, botLabel, botGrid);
        botSide.setAlignment(Pos.CENTER);

        HBox boardsBox = new HBox(40, playerSide, botSide);
        boardsBox.setAlignment(Pos.TOP_CENTER);
        boardsBox.setPadding(new Insets(10, 24, 20, 24));

        VBox root = new VBox(topBox, boardsBox);
        root.setStyle("-fx-background-color: #232222f5;");

        updateBoard(playerBoard, playerButtons);
        updateBoard(botBoard, botButtons);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Ship Battle");
        stage.show();
    }

    // ── Ship Tray ──────────────────────────────────────────────────────────────

    private void buildShipTrayItems() {
        shipTray.getChildren().clear();
        shipTrayItems.clear();
        for (String ship : new String[]{"Carrier", "Battleship", "Destroyer", "Submarine", "Frigate"}) {
            VBox item = buildTrayShip(ship);
            shipTrayItems.put(ship, item);
            shipTray.getChildren().add(item);
        }
    }

    private VBox buildTrayShip(String shipName) {
        int length = Board.getShipLength(shipName);
        String color = shipNameToColor(shipName);

        HBox cells = new HBox(2);
        cells.setAlignment(Pos.CENTER);

        for (int i = 0; i < length; i++) {
            Pane cell = new Pane();
            cell.setPrefSize(TRAY_CELL, TRAY_CELL);
            String radius = (length == 1) ? "6"
                          : (i == 0)          ? "6 0 0 6"
                          : (i == length - 1) ? "0 6 6 0"
                          : "0";
            cell.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: " + radius + ";"
            );
            cells.getChildren().add(cell);
        }

        Label nameLabel = new Label(shipName);
        nameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        nameLabel.setTextFill(Color.rgb(200, 200, 200));

        VBox box = new VBox(4, cells, nameLabel);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-cursor: hand;");

        box.setOnDragDetected(e -> {
            draggingShipName = shipName;
            dragFromBoard = false;
            dragIsVertical = isVertical;
            Dragboard db = box.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(shipName);
            db.setContent(content);
            e.consume();
        });

        box.setOnDragDone(e -> {
            draggingShipName = null;
            clearPreview();
            e.consume();
        });

        return box;
    }

    private void hideFromTray(String shipName) {
        VBox item = shipTrayItems.get(shipName);
        if (item != null) {
            item.setVisible(false);
            item.setManaged(false);
        }
    }

    private void showInTray(String shipName) {
        VBox item = shipTrayItems.get(shipName);
        if (item != null) {
            item.setVisible(true);
            item.setManaged(true);
        }
    }

    // ── Board Grid ─────────────────────────────────────────────────────────────

    private GridPane buildGrid(Button[][] buttons, boolean interactive) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setPadding(new Insets(4));
        grid.setStyle("-fx-background-color: #000000ff; -fx-background-radius: 4;");

        for (int col = 0; col < 10; col++) {
            Label lbl = new Label(String.valueOf(col + 1));
            lbl.setPrefSize(CELL_SIZE, 20);
            lbl.setAlignment(Pos.CENTER);
            lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
            lbl.setTextFill(Color.WHITE);
            grid.add(lbl, col + 1, 0);
        }

        for (int row = 0; row < 10; row++) {
            char rowChar = (char) ('A' + row);
            Label rowLbl = new Label(String.valueOf(rowChar));
            rowLbl.setPrefSize(20, CELL_SIZE);
            rowLbl.setAlignment(Pos.CENTER);
            rowLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
            rowLbl.setTextFill(Color.WHITE);
            grid.add(rowLbl, 0, row + 1);

            for (int col = 0; col < 10; col++) {
                Button btn = new Button();
                btn.setPrefSize(CELL_SIZE, CELL_SIZE);
                btn.setFont(Font.font("Georgia", FontWeight.BOLD, 13));

                if (interactive) {
                    int r = row, c = col;
                    attachBoardDragSource(btn, r, c);
                    attachDropHandlers(btn, r, c);
                } else {
                    btn.setDisable(true);
                    btn.setOpacity(1.0);
                }

                buttons[row][col] = btn;
                grid.add(btn, col + 1, row + 1);
            }
        }

        return grid;
    }

    // ── Drag Source: picking up a placed ship from the board ───────────────────

    private void attachBoardDragSource(Button btn, int row, int col) {
        btn.setOnDragDetected(e -> {
            Cell cell = playerBoard.getCell(row, col);
            if (cell == Cell.WATER || cell == Cell.HIT || cell == Cell.MISS) {
                e.consume();
                return;
            }
            String shipName = playerBoard.getShipName(row, col);
            if (shipName == null) { e.consume(); return; }

            // Detect orientation: vertical if any neighbor above/below has same cell type
            boolean vertical =
                (row + 1 < 10 && playerBoard.getCell(row + 1, col) == cell) ||
                (row - 1 >= 0 && playerBoard.getCell(row - 1, col) == cell);

            // Find anchor (topmost cell if vertical, leftmost if horizontal)
            int anchorRow = row, anchorCol = col;
            if (vertical) {
                while (anchorRow > 0 && playerBoard.getCell(anchorRow - 1, col) == cell) anchorRow--;
            } else {
                while (anchorCol > 0 && playerBoard.getCell(row, anchorCol - 1) == cell) anchorCol--;
            }

            draggingShipName = shipName;
            dragFromBoard = true;
            dragFromRow = anchorRow;
            dragFromCol = anchorCol;
            dragIsVertical = vertical;

            Dragboard db = btn.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(shipName);
            db.setContent(content);
            e.consume();
        });

        btn.setOnDragDone(e -> {
            draggingShipName = null;
            clearPreview();
            e.consume();
        });
    }

    // ── Drop Target: board cells ───────────────────────────────────────────────

    private void attachDropHandlers(Button btn, int row, int col) {
        btn.setOnDragOver(e -> {
            if (e.getDragboard().hasString())
                e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        });

        btn.setOnDragEntered(e -> {
            if (!e.getDragboard().hasString() || draggingShipName == null) return;
            int length = Board.getShipLength(draggingShipName);

            boolean valid;
            if (dragFromBoard) {
                // Temporarily lift the ship so it doesn't block its own placement check
                playerBoard.removeShipAt(dragFromRow, dragFromCol);
                valid = playerBoard.canPlaceShipPublic(row, col, length, dragIsVertical);
                playerBoard.addShipByNameOriented(
                    playerBoard.toCoord(dragFromRow, dragFromCol), draggingShipName, dragIsVertical
                );
            } else {
                valid = playerBoard.canPlaceShipPublic(row, col, length, dragIsVertical);
            }

            paintPreview(row, col, length, dragIsVertical, valid);
            e.consume();
        });

        btn.setOnDragExited(e -> {
            clearPreview();
            e.consume();
        });

        btn.setOnDragDropped(e -> {
            boolean success = false;
            if (e.getDragboard().hasString() && draggingShipName != null) {
                if (dragFromBoard) {
                    playerBoard.removeShipAt(dragFromRow, dragFromCol);
                }

                boolean placed = playerBoard.addShipByNameOriented(
                    playerBoard.toCoord(row, col), draggingShipName, dragIsVertical
                );

                if (placed) {
                    updateBoard(playerBoard, playerButtons);
                    if (!dragFromBoard) {
                        hideFromTray(draggingShipName);
                    }
                    success = true;
                } else {
                    // Restore ship at original position if it was a board drag
                    if (dragFromBoard) {
                        playerBoard.addShipByNameOriented(
                            playerBoard.toCoord(dragFromRow, dragFromCol), draggingShipName, dragIsVertical
                        );
                        updateBoard(playerBoard, playerButtons);
                    }
                }
            }
            clearPreview();
            e.setDropCompleted(success);
            e.consume();
        });
    }

    // ── Preview Highlighting ───────────────────────────────────────────────────

    private void paintPreview(int anchorRow, int anchorCol, int length, boolean vertical, boolean valid) {
        clearPreview();
        String color = valid ? "#00dd0099" : "#dd000099";
        for (int i = 0; i < length; i++) {
            int r = vertical ? anchorRow + i : anchorRow;
            int c = vertical ? anchorCol : anchorCol + i;
            if (r >= 0 && r < 10 && c >= 0 && c < 10) {
                highlightedCells.add(new int[]{r, c});
                playerButtons[r][c].setStyle(
                    "-fx-background-color: " + color + ";" +
                    "-fx-background-radius: 3;" +
                    "-fx-border-color: rgba(0,0,0,0.15);" +
                    "-fx-border-radius: 3;"
                );
            }
        }
    }

    private void clearPreview() {
        for (int[] cell : highlightedCells) {
            Cell c = playerBoard.getCell(cell[0], cell[1]);
            playerButtons[cell[0]][cell[1]].setStyle(
                "-fx-background-color: " + c.getColor() + ";" +
                "-fx-text-fill: " + c.getTextColor() + ";" +
                "-fx-background-radius: 3;" +
                "-fx-border-color: rgba(0,0,0,0.15);" +
                "-fx-border-radius: 3;"
            );
        }
        highlightedCells.clear();
    }

    // ── Board Rendering ────────────────────────────────────────────────────────

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

    private String shipNameToColor(String name) {
        switch (name) {
            case "Carrier":    return Cell.CARRIER.getColor();
            case "Battleship": return Cell.BATTLESHIP.getColor();
            case "Destroyer":  return Cell.DESTROYER.getColor();
            case "Submarine":  return Cell.SUBMARINE.getColor();
            case "Frigate":    return Cell.FRIGATE.getColor();
            default:           return "#d5f2ffff";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
