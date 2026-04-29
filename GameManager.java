import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

public class GameManager {
    public static int playerHP = 17;
    public static int botHP = 17;
    public static Scanner scnr = new Scanner(System.in);

    // GUI: fresh game state factory (keeps restart logic out of the UI)
    public static class GuiState {
        public final Board playerBoard;
        public final Board botBoard;
        public final Board initialPlayerBoard;
        public final boolean[][] markedBot;
        public final Set<String> sunkShips;
        public final ShipAbilities abilities;

        public final int radarCounter;
        public final int shieldCounter;
        public final int reinforcementsCounter;
        public final int communicationDisruptionCounter;
        public final int blackoutCounter;
        public final int rebuildCounter;
        public final int repositionCounter;

        public GuiState(
            Board playerBoard,
            Board botBoard,
            Board initialPlayerBoard,
            boolean[][] markedBot,
            Set<String> sunkShips,
            ShipAbilities abilities,
            int radarCounter,
            int shieldCounter,
            int reinforcementsCounter,
            int communicationDisruptionCounter,
            int blackoutCounter,
            int rebuildCounter,
            int repositionCounter
        ) {
            this.playerBoard = playerBoard;
            this.botBoard = botBoard;
            this.initialPlayerBoard = initialPlayerBoard;
            this.markedBot = markedBot;
            this.sunkShips = sunkShips;
            this.abilities = abilities;

            this.radarCounter = radarCounter;
            this.shieldCounter = shieldCounter;
            this.reinforcementsCounter = reinforcementsCounter;
            this.communicationDisruptionCounter = communicationDisruptionCounter;
            this.blackoutCounter = blackoutCounter;
            this.rebuildCounter = rebuildCounter;
            this.repositionCounter = repositionCounter;
        }
    }

    public static GuiState newGuiState() {
        Set<String> sunkShips = new HashSet<>();
        return new GuiState(
            new Board(),
            new Board(),
            new Board(),
            new boolean[10][10],
            sunkShips,
            new ShipAbilities(sunkShips),
            0, 0, 0, 0, 0, 0, 0
        );
    }

    public static void playGame(Board playerBoard, Board botBoard) {
        while(playerHP > 0 && botHP > 0) {
            botBoard = playerStrike(botBoard);
            playerBoard = botStrike(playerBoard);
        }
        if(playerHP == 0) {
            System.out.println("Bot has won! " + botHP + " ship coordinates remain unharmed");
        }
        if(botHP == 0) {
            System.out.println("Player has won! " + playerHP + " ship coordinates remain unharmed");
        }
        scnr.close();
    }

    public static Board playerStrike(Board botBoard) {
        int[] coords;
        boolean valid = false;
        while(!valid) {
            coords = getPlayerCoords();
            if(botBoard.getCell(coords[0],coords[1]) == Cell.HIT || botBoard.getCell(coords[0],coords[1]) == Cell.MISS) {
                System.out.println("ERROR: Area already targeted. Please select unique coordinates.");
            }
            else if(botBoard.getCell(coords[0],coords[1]) != Cell.WATER) {
                Cell hit = botBoard.getCell(coords[0],coords[1]);
                botBoard.setCell(coords[0],coords[1], Cell.HIT);
                announceSink(hit, botBoard);
                System.out.println("It's a HIT! " + hit);
                botHP--;
                valid = true;
            }
            else {
                botBoard.setCell(coords[0],coords[1], Cell.MISS);
                System.out.println("It's a miss...");
                valid = true;
            }
            botBoard.displayBoardShips();
        }
        return botBoard;
    }

    public static Board botStrike(Board playerBoard) {
        int[] coords = new int[2];
        boolean valid = false;
        while(!valid) {
            coords[0] = (int) (Math.random() * 10);
            coords[1] = (int) (Math.random() * 10);
            if(playerBoard.getCell(coords[0],coords[1]) == Cell.HIT || playerBoard.getCell(coords[0],coords[1]) == Cell.MISS) {}
            else if(playerBoard.getCell(coords[0],coords[1]) != Cell.WATER) {
                System.out.println("Striking at coordinates " + coords[0] + coords[1] + "...");
                Cell hit = playerBoard.getCell(coords[0],coords[1]);
                playerBoard.setCell(coords[0],coords[1], Cell.HIT);
                announceSink(hit, playerBoard);
                System.out.println("It's a HIT! " + hit);
                playerHP--;
                valid = true;
            }
            else {
                System.out.println("Striking at coordinates " + (char) (coords[0] + 65) + (coords[1] + 1) + "...");
                playerBoard.setCell(coords[0],coords[1], Cell.MISS);
                System.out.println("It's a miss...");
                valid = true;
            }
        }
        playerBoard.displayBoardShips();
        return playerBoard;
    }

    public static int[] getPlayerCoords() {
        int[] coords = new int[2];
        boolean valid = false;
        char letter = ' ';
        int num = 0;
        while(!valid) {
            System.out.println("Enter coordinates to strike at. Format input like this: C3 <A-J><1-10>");
            String input = scnr.nextLine();
            letter = input.toUpperCase().charAt(0);
            num = Integer.parseInt(input.substring(1));
            System.out.println("Row: " + letter + (int) letter);
            System.out.println("Column: " + num);
            if(((int) letter) - 65 < 0 || ((int) letter) - 65 > 9) {
                System.out.println("ERROR: Invalid Row Coordinate. Make sure the character you enter is between A and J.");
            }
            else if(num < 1 || num > 10) {
                System.out.println("ERROR: Invalid Column Coordinate. Make sure the number you enter is between 1 and 10.");
            }
            else {
                valid = true;
                System.out.println("Striking at coordinates " + letter + num + "...");
            }
        }
        coords[0] = ((int) letter) - 65;
        coords[1] = num - 1;
        return coords;
    }

    public static void announceSink(Cell c, Board b) {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                if(b.getCell(i, j) == c) {
                    return;
                }
            }
        }
        System.out.println(c + " has been sunk!");
    }


}
