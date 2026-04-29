import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

import java.util.*;

public class ShipBattleGUI extends Application {

    private static final int BASE_FONT = 16;
    private static final int CELL_SIZE = 44;
    private static final int TRAY_CELL = 36;
    private static final int ACTION_BTN_WIDTH = 180;

    private Board playerBoard = new Board();
    private Button[][] playerButtons = new Button[10][10];

    private Board botBoard = new Board();
    private Button[][] botButtons = new Button[10][10];
    private boolean[][] markedBot = new boolean[10][10];

    private Board initialPlayerBoard = new Board();

    //powerup/ability states
    private Set<String> sunkShips = new HashSet<>(); //this will track what ships the bot has sunk
    private ShipAbilities abilities = new ShipAbilities(sunkShips);
    int radarCounter = 0;
    int shieldCounter = 0;
    int reinforcementsCounter = 0;
    int communicationDisruptionCounter = 0;
    int blackoutCounter = 0;
    int rebuildCounter = 0;
    int repositionCounter = 0;

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
    private Button randomizeButton;
    private Button startButton;

    //powerup/ability buttons
    private Button carrierButton;
    private Button frigateButton;
    private Button submarineButton;
    private Button radarButton;
    private Button shieldButton;
    private Button reinforcementsButton;
    private Button communicationDisruptionButton;
    private Button blackoutButton;
    private Button rebuildButton;
    private Button repositionButton;

    // Game state
    private boolean gameStarted = false;
    private boolean playerTurn = true;
    private final Random random = new Random();

    // Bot smart targeting — adjacent cells to try after a hit
    private final List<int[]> botTargetQueue = new ArrayList<>();

    // Status labels
    private Label playerStatusLabel;  // below player board — shows bot's action
    private Label botStatusLabel;     // below bot board — shows player's turn status

    @Override
    public void start(Stage stage) {

        Label titleLabel = new Label("SHIP BATTLE");
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT * 3));
        titleLabel.setTextFill(Color.WHITE);

        VBox topBox = new VBox(8, titleLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(16, 0, 10, 0));

        // ── PLAYER side ────────────────────────────────────────────────────────

        Label playerLabel = new Label("PLAYER");
        playerLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT));
        playerLabel.setTextFill(Color.WHITE);

        GridPane playerGrid = buildGrid(playerButtons, true);

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

        Label trayLabel = new Label("unplaced ships");
        trayLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        trayLabel.setTextFill(Color.rgb(180, 180, 180));

        shipTray = new FlowPane(8, 8);
        shipTray.setPadding(new Insets(8, 0, 8, 0));
        shipTray.setPrefWrapLength(520);
        buildShipTrayItems();

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

        randomizeButton = new Button("RANDOMIZE");
        randomizeButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        randomizeButton.setStyle(
            "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
            "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        randomizeButton.setOnAction(e -> {
            playerBoard = RandomShip.makeShip();
            updateBoard(playerBoard, playerButtons);
            for (String ship : shipTrayItems.keySet()) hideFromTray(ship);
        });

        startButton = new Button("START GAME");
        startButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        startButton.setStyle(
            "-fx-background-color: #4A890C; -fx-text-fill: white;" +
            "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        startButton.setOnAction(e -> {
            if (!allShipsPlaced()) {
                playerStatusLabel.setText("Place all 5 ships first!");
                return;
            }
            startGame();
        });

        carrierButton = new Button("Use Carrier Ability");
        carrierButton.setDisable(true);
        carrierButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        carrierButton.setStyle(
            "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
            "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        carrierButton.setOnAction(e -> {
            abilities.toggleCarrier();
            if(abilities.getCarrierActive()) carrierButton.setText("Carrier Ability Active! [Click again to cancel]");
            else carrierButton.setText("Use Carrier Ability");
        
        });

        frigateButton = new Button("Use Frigate Ability");
        frigateButton.setDisable(true);
        frigateButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        frigateButton.setStyle(
            "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
            "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        frigateButton.setOnAction(e -> {
            abilities.useFrigate(playerBoard);
            updateBoard(playerBoard, playerButtons);
            playerStatusLabel.setText("Frigate has been revived!");
            botTargetQueue.clear();
            frigateButton.setText("Frigate Ability Used");
            frigateButton.setDisable(true);
        });

        submarineButton = new Button("Use Submarine Nuclear Ability");
        submarineButton.setDisable(true);
        submarineButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        submarineButton.setStyle(
            "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
            "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        submarineButton.setOnAction(e -> {
            abilities.toggleSubmarine();
            if (abilities.getSubmarineActive()) submarineButton.setText("Submarine Nuclear Active! [Click again to cancel]");
            else submarineButton.setText("Use Submarine Nuclear Ability");
        });

        radarButton = new Button("Use Radar (0x)");
        radarButton.setDisable(true);
        radarButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        radarButton.setStyle(
                "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        radarButton.setOnAction(e -> {
            String revealed = Powerup.doRadar(botBoard, markedBot);
            if(revealed != null) {
                radarCounter--;
                if(radarCounter == 0){
                    radarButton.setDisable(true);
                }
                radarButton.setText("Use Radar (" + radarCounter + "x)");
                updateBotBoardForPlayer();
                botStatusLabel.setText("Radar revealed the enemy " + revealed + "!");
            }
        });

        shieldButton = new Button("Use Shield (0x)");
        shieldButton.setDisable(true);
        shieldButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        shieldButton.setStyle(
                "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        shieldButton.setOnAction(e -> {
            Powerup.doShield(playerBoard, botStatusLabel);
            updateBoard(playerBoard, playerButtons);
            shieldCounter--;
            if(shieldCounter == 0){
                shieldButton.setDisable(true);
            }
            shieldButton.setText("Use Shield (" + shieldCounter + "x)");
        });

        reinforcementsButton = new Button("Use Reinforcements (0x)");
        reinforcementsButton.setDisable(true);
        reinforcementsButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        reinforcementsButton.setStyle(
                "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        reinforcementsButton.setOnAction(e -> {
            Powerup.doReinforcements();
            reinforcementsCounter--;
            if(reinforcementsCounter == 0){
                reinforcementsButton.setDisable(true);
            }
            reinforcementsButton.setText("Use Reinforcements (" + reinforcementsCounter + "x)");
        });

        communicationDisruptionButton = new Button("Use Communication Disruption (0x)");
        communicationDisruptionButton.setDisable(true);
        communicationDisruptionButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        communicationDisruptionButton.setStyle(
                "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        communicationDisruptionButton.setOnAction(e -> {
            Powerup.doCommunicationDisruption();
            communicationDisruptionCounter--;
            if(communicationDisruptionCounter == 0){
                communicationDisruptionButton.setDisable(true);
            }
            communicationDisruptionButton.setText("Use Communication Disruption (" + communicationDisruptionCounter + "x)");
        });

        blackoutButton = new Button("Use Blackout (0x)");
        blackoutButton.setDisable(true);
        blackoutButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        blackoutButton.setStyle(
                "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        blackoutButton.setOnAction(e -> {
            Powerup.doBlackout(playerBoard);
            blackoutCounter--;
            if(blackoutCounter == 0){
                blackoutButton.setDisable(true);
            }
            blackoutButton.setText("Use Blackout (" + blackoutCounter + "x)");
            updateBoard(playerBoard, playerButtons);
        });

        rebuildButton = new Button("Use Rebuild (0x)");
        rebuildButton.setDisable(true);
        rebuildButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        rebuildButton.setStyle(
                "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        rebuildButton.setOnAction(e -> {
            if(Powerup.doRebuild(initialPlayerBoard, playerBoard)) {
                rebuildCounter--;
                if(rebuildCounter == 0){
                    rebuildButton.setDisable(true);
                }
                rebuildButton.setText("Use Rebuild (" + rebuildCounter + "x)");
                updateBoard(playerBoard, playerButtons);
            }
        });

        repositionButton = new Button("Use Reposition (0x)");
        repositionButton.setDisable(true);
        repositionButton.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        repositionButton.setStyle(
                "-fx-background-color: #3a7bd5; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        repositionButton.setOnAction(e -> {
            Powerup.doReposition(initialPlayerBoard, playerBoard);
            repositionCounter--;
            if(repositionCounter == 0){
                repositionButton.setDisable(true);
            }
            repositionButton.setText("Use Reposition (" + repositionCounter + "x)");
            updateBoard(playerBoard, playerButtons);
        });

        // Lock action button widths so long text doesn't shift the boards.
        lockActionButtonWidth(carrierButton);
        lockActionButtonWidth(frigateButton);
        lockActionButtonWidth(submarineButton);
        lockActionButtonWidth(radarButton);
        lockActionButtonWidth(shieldButton);
        lockActionButtonWidth(reinforcementsButton);
        lockActionButtonWidth(communicationDisruptionButton);
        lockActionButtonWidth(blackoutButton);
        lockActionButtonWidth(rebuildButton);
        lockActionButtonWidth(repositionButton);

        playerStatusLabel = new Label("");
        playerStatusLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        playerStatusLabel.setTextFill(Color.rgb(220, 220, 220));
        playerStatusLabel.setPadding(new Insets(6, 0, 0, 0));

        HBox trayControls = new HBox(12, trayLabel, orientationToggle);
        trayControls.setAlignment(Pos.CENTER_LEFT);
        trayControls.setPadding(new Insets(8, 0, 0, 0));

        HBox bottomRow = new HBox(12, randomizeButton, startButton);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.setPadding(new Insets(8, 0, 0, 0));

        HBox abilityRow = new HBox(12, carrierButton, frigateButton, submarineButton);
        abilityRow.setAlignment(Pos.CENTER_LEFT);
        abilityRow.setPadding(new Insets(8, 0, 0, 0));

        HBox powerupFirstRow = new HBox(12, radarButton, shieldButton, reinforcementsButton);
        powerupFirstRow.setAlignment(Pos.CENTER_LEFT);
        powerupFirstRow.setPadding(new Insets(8, 0, 0, 0));

        HBox powerupSecondRow = new HBox(12, communicationDisruptionButton, blackoutButton);
        powerupSecondRow.setAlignment(Pos.CENTER_LEFT);
        powerupSecondRow.setPadding(new Insets(8, 0, 0, 0));

        HBox powerupThirdRow = new HBox(12, rebuildButton, repositionButton);
        powerupThirdRow.setAlignment(Pos.CENTER_LEFT);
        powerupThirdRow.setPadding(new Insets(8, 0, 0, 0));

        VBox playerSide = new VBox(6, playerLabel, playerGrid, playerStatusLabel,
                                   trayControls, shipTray, bottomRow, abilityRow, powerupFirstRow, powerupSecondRow, powerupThirdRow);
        playerSide.setAlignment(Pos.CENTER_LEFT);

        // ── BOT side ───────────────────────────────────────────────────────────

        Label botLabel = new Label("BOT");
        botLabel.setFont(Font.font("Georgia", FontWeight.BOLD, BASE_FONT));
        botLabel.setTextFill(Color.WHITE);

        GridPane botGrid = buildGrid(botButtons, false);

        botStatusLabel = new Label("");
        botStatusLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        botStatusLabel.setTextFill(Color.rgb(220, 220, 220));
        botStatusLabel.setPadding(new Insets(6, 0, 0, 0));

        VBox botSide = new VBox(6, botLabel, botGrid, botStatusLabel);
        botSide.setAlignment(Pos.TOP_LEFT);

        // ── Root layout ────────────────────────────────────────────────────────

        HBox boardsBox = new HBox(40, playerSide, botSide);
        boardsBox.setAlignment(Pos.TOP_CENTER);
        boardsBox.setPadding(new Insets(10, 24, 20, 24));

        VBox root = new VBox(topBox, boardsBox);
        root.setStyle("-fx-background-color: #232222f5;");

        updateBoard(playerBoard, playerButtons);
        updateBotBoardForPlayer();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Ship Battle");
        stage.show();
    }

    // ── Game Logic ─────────────────────────────────────────────────────────────

    private boolean allShipsPlaced() {
        for (VBox item : shipTrayItems.values())
            if (item.isVisible()) return false;
        return true;
    }

    private void startGame() {
        gameStarted = true;
        playerTurn = true;

        // Place bot ships randomly
        botBoard = RandomShip.makeShip();
        updateBotBoardForPlayer();

        // Lock placement UI
        orientationToggle.setDisable(true);
        randomizeButton.setDisable(true);
        startButton.setDisable(true);
        shipTray.setOnDragOver(null);
        shipTray.setOnDragDropped(null);

        // Enable bot board for clicking
        setBotBoardEnabled(true);

        playerStatusLabel.setText("");
        carrierButton.setDisable(false);
        frigateButton.setDisable(false);
        submarineButton.setDisable(false);
        botStatusLabel.setText("YOUR TURN — click a cell on the bot's board");

        // Snapshot board states to compare later
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                initialPlayerBoard.setCell(i, j, playerBoard.getCell(i, j));
            }
        }
    }

    private void onBotCellClicked(int row, int col) {
        if (!gameStarted || !playerTurn) return;
        Cell c = botBoard.getCell(row, col);
        if (abilities.getCarrierActive()) {
            abilities.useCarrier(row,col,markedBot);
            updateBotBoardForPlayer();
            botStatusLabel.setText("Carrier Ability has been used!");
            abilities.toggleCarrier();
            abilities.resetCarrierCooldown();
            carrierButton.setText("Carrier Ability Cooldown: 3 Turns");
            carrierButton.setDisable(true);

        }
        //else if frigate active {}
        else if (abilities.getSubmarineActive()) {
            abilities.useSubmarine(row, col, markedBot);
            updateBotBoardForPlayer();
            botStatusLabel.setText("Submarine Nuclear strike launched!");
            abilities.toggleSubmarine();
            abilities.resetSubmarineCooldown();
            submarineButton.setText("Submarine Nuclear Cooldown: 5 Turns");
            submarineButton.setDisable(true);
        }
        else {
            if (c == Cell.HIT || c == Cell.MISS) return; // already fired here

            boolean hit = botBoard.fireAt(row, col);
            updateBotBoardForPlayer();

            if (hit) {
                if (botBoard.allShipsSunk()) {
                    playerStatusLabel.setText("");
                    botStatusLabel.setText("YOU WIN! All bot ships sunk!");
                    gameStarted = false;
                    return;
                }
                String sunk = botBoard.getLastSunkShip();
                if (abilities.getBattleshipActive()) {
                    String msg = sunk != null
                        ? "YOU SUNK the bot's " + sunk + "! Your battleship can shoot again!"
                        : "YOU HIT a bot ship! Your battleship can shoot again!";
                    botStatusLabel.setText(msg);
                    playerTurn = true;
                    setBotBoardEnabled(true);
                    return;
                } else if (sunk != null) {
                    botStatusLabel.setText("YOU SUNK the bot's " + sunk + "!");
                } else {
                    botStatusLabel.setText("YOU HIT a bot ship!");
                }
            }
            else {
                botStatusLabel.setText("YOU MISSED.");
            }
        }

        playerTurn = false;
        setBotBoardEnabled(false);

        playerStatusLabel.setText("BOT'S TURN — waiting...");

        PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
        pause.setOnFinished(ev -> botFire());
        pause.play();
    }

    private void botFire() {
        int[] target = pickBotTarget();
        boolean hit = playerBoard.fireAt(target[0], target[1]);
        updateBoard(playerBoard, playerButtons);
        String sunk = playerBoard.getLastSunkShip();
        if (sunk != null) {
            sunkShips.add(sunk);
            botTargetQueue.clear();
            playerStatusLabel.setText("BOT SUNK your " + sunk + "!");
            if (sunk.equals("Frigate") && !abilities.isFrigateUsed()) {
                if (abilities.getFrigateActive()) abilities.toggleFrigate();
                frigateButton.setText("Frigate Sunk");
                frigateButton.setDisable(true);
            }
            if (sunk.equals("Carrier")) {
                carrierButton.setText("Carrier Sunk");
                carrierButton.setDisable(true);
            }
            if (sunk.equals("Submarine")) {
                submarineButton.setText("Submarine Sunk");
                submarineButton.setDisable(true);
            }
        } else if (hit) {
            playerStatusLabel.setText("BOT HIT YOUR SHIP!");
            addAdjacentTargets(target[0], target[1]);
        } else if (playerBoard.getBlocked()) {
            playerStatusLabel.setText("BOT HIT A SHIELDED SHIP! Shield has been consumed");
            playerBoard.setBlocked(false);
            playerBoard.removeShieldFrom(target[0], target[1]);
            updateBoard(playerBoard, playerButtons);
        } else {
            playerStatusLabel.setText("BOT MISSED.");
        }

        if (playerBoard.allShipsSunk()) {
            playerStatusLabel.setText("BOT WINS! All your ships are sunk.");
            gameStarted = false;
            return;
        }

        playerTurn = true;
        setBotBoardEnabled(true);
        botStatusLabel.setText("YOUR TURN — click a cell on the bot's board");

        //Decrement ship ability cooldowns
        if(abilities.getCarrierCooldown() > 0) {
           abilities.decrementCarrierCooldown();
            if(abilities.getCarrierCooldown() == 0) {
                carrierButton.setDisable(false);
                carrierButton.setText("Use Carrier Ability");
            }
            else {
                carrierButton.setText("Carrier Ability Cooldown: " + abilities.getCarrierCooldown() + " Turns");
            }
        }
        if(abilities.getSubmarineCooldown() > 0) {
            abilities.decrementSubmarineCooldown();
            if(abilities.getSubmarineCooldown() == 0) {
                submarineButton.setDisable(false);
                submarineButton.setText("Use Submarine Nuclear Ability");
            }
            else {
                submarineButton.setText("Submarine Nuclear Cooldown: " + abilities.getSubmarineCooldown() + " Turns");
            }
        }

        //Chance to give player powerup
        int dropChance = 3; //Chance of getting a powerup will be 1/dropChance
        int dropRoll = (int)(Math.random() * dropChance);
        if(dropRoll == 0){
            int whichPowerup = (int)(Math.random() * 7);
            switch(whichPowerup){
                case 0:
                    radarCounter++;
                    if(radarCounter == 1){
                        radarButton.setDisable(false);
                    }
                    radarButton.setText("Use Radar (" + radarCounter + "x)");
                    break;
                case 1:
                    shieldCounter++;
                    if(shieldCounter == 1){
                        shieldButton.setDisable(false);
                    }
                    shieldButton.setText("Use Shield (" + shieldCounter + "x)");
                    break;
                case 2:
                    reinforcementsCounter++;
                    if(reinforcementsCounter == 1){
                        reinforcementsButton.setDisable(false);
                    }
                    reinforcementsButton.setText("Use Reinforcements (" + reinforcementsCounter + "x)");
                    break;
                case 3:
                    communicationDisruptionCounter++;
                    if(communicationDisruptionCounter == 1){
                        communicationDisruptionButton.setDisable(false);
                    }
                    communicationDisruptionButton.setText("Use Communication Disruption (" + communicationDisruptionCounter + "x)");
                    break;
                case 4:
                    blackoutCounter++;
                    if(blackoutCounter == 1){
                        blackoutButton.setDisable(false);
                    }
                    blackoutButton.setText("Use Blackout (" + blackoutCounter + "x)");
                    break;
                case 5:
                    rebuildCounter++;
                    if(rebuildCounter == 1){
                        rebuildButton.setDisable(false);
                    }
                    rebuildButton.setText("Use Rebuild (" + rebuildCounter + "x)");
                    break;
                case 6:
                    repositionCounter++;
                    if(repositionCounter == 1){
                        repositionButton.setDisable(false);
                    }
                    repositionButton.setText("Use Reposition (" + repositionCounter + "x)");
                    break;
            }
        }
    }

    // Returns next target: queued adjacent cell if available, else random unfired cell
    private int[] pickBotTarget() {
        while (!botTargetQueue.isEmpty()) {
            int[] candidate = botTargetQueue.remove(0);
            int r = candidate[0], c = candidate[1];
            if (r >= 0 && r < 10 && c >= 0 && c < 10) {
                Cell cell = playerBoard.getCell(r, c);
                if (cell != Cell.HIT && cell != Cell.MISS) return candidate;
            }
        }
        List<int[]> unfired = playerBoard.getUnfiredCells();
        return unfired.get(random.nextInt(unfired.size()));
    }

    // Adds up/down/left/right neighbours to the bot's target queue
    private void addAdjacentTargets(int row, int col) {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            if (r >= 0 && r < 10 && c >= 0 && c < 10) {
                Cell cell = playerBoard.getCell(r, c);
                if (cell != Cell.HIT && cell != Cell.MISS)
                    botTargetQueue.add(new int[]{r, c});
            }
        }
    }

    private void setBotBoardEnabled(boolean enabled) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Cell cell = botBoard.getCell(r, c);
                boolean alreadyFired = (cell == Cell.HIT || cell == Cell.MISS);
                botButtons[r][c].setDisable(!enabled || alreadyFired);
                botButtons[r][c].setOpacity(1.0);
            }
        }
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
            if (gameStarted) { e.consume(); return; }
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
        if (item != null) { item.setVisible(false); item.setManaged(false); }
    }

    private void showInTray(String shipName) {
        VBox item = shipTrayItems.get(shipName);
        if (item != null) { item.setVisible(true); item.setManaged(true); }
    }

    // ── Board Grid ─────────────────────────────────────────────────────────────

    private GridPane buildGrid(Button[][] buttons, boolean interactive) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setPadding(new Insets(4));
        grid.setStyle("-fx-background-color: #000000ff; -fx-background-radius: 4;");
        // Prevent layout from stretching the board area horizontally/vertically.
        grid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

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
                    // Bot board: click handler set now, disabled until game starts
                    int r = row, c = col;
                    btn.setOnAction(e -> onBotCellClicked(r, c));
                    btn.setDisable(true);
                    btn.setOpacity(1.0);
                }

                buttons[row][col] = btn;
                grid.add(btn, col + 1, row + 1);
            }
        }

        return grid;
    }

    private void lockActionButtonWidth(Button btn) {
        btn.setMinWidth(ACTION_BTN_WIDTH);
        btn.setPrefWidth(ACTION_BTN_WIDTH);
        btn.setMaxWidth(ACTION_BTN_WIDTH);
        btn.setWrapText(true);
    }

    // ── Drag Source: picking up a placed ship from the board ───────────────────

    private void attachBoardDragSource(Button btn, int row, int col) {
        btn.setOnDragDetected(e -> {
            if (gameStarted) { e.consume(); return; }
            Cell cell = playerBoard.getCell(row, col);
            if (cell == Cell.WATER || cell == Cell.HIT || cell == Cell.MISS) {
                e.consume(); return;
            }
            String shipName = playerBoard.getShipName(row, col);
            if (shipName == null) { e.consume(); return; }

            boolean vertical =
                (row + 1 < 10 && playerBoard.getCell(row + 1, col) == cell) ||
                (row - 1 >= 0 && playerBoard.getCell(row - 1, col) == cell);

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

        btn.setOnDragExited(e -> { clearPreview(); e.consume(); });

        btn.setOnDragDropped(e -> {
            boolean success = false;
            if (e.getDragboard().hasString() && draggingShipName != null) {
                if (dragFromBoard) playerBoard.removeShipAt(dragFromRow, dragFromCol);

                boolean placed = playerBoard.addShipByNameOriented(
                    playerBoard.toCoord(row, col), draggingShipName, dragIsVertical
                );

                if (placed) {
                    updateBoard(playerBoard, playerButtons);
                    if (!dragFromBoard) hideFromTray(draggingShipName);
                    success = true;
                } else {
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
                if(playerBoard.isShielded(row, col)) btn.setText("Sh");
                else btn.setText(cell.getSymbol());
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

    // Bot board: hide ship positions — only show HIT/MISS, everything else as water
    private void updateBotBoardForPlayer() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Cell actual = botBoard.getCell(row, col);
                Cell display = (actual == Cell.HIT || actual == Cell.MISS || markedBot[row][col]) ? actual : Cell.WATER;
                Button btn = botButtons[row][col];
                if(markedBot[row][col])  btn.setText(display.getTrueSymbol()); //only show marked tiles if they are marked
                else btn.setText(display.getSymbol());
                btn.setStyle(
                    "-fx-background-color: " + display.getColor() + ";" +
                    "-fx-text-fill: " + display.getTextColor() + ";" +
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
