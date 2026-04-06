import java.util.*;

public class Board {

    private Cell[][] board;
    private final int SIZE = 10;

    public Board() {
        board = new Cell[SIZE][SIZE];
        setBoard();
    }

    public void setBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = Cell.WATER;
            }
        }
    }

    private int[] parseCoord(String coord) {
        coord = coord.toUpperCase();
        int row = coord.charAt(0) - 'A';
        int col = Integer.parseInt(coord.substring(1)) - 1;
        return new int[]{row, col};
    }

    public void displayBoardCoords() {
        for (int i = 0; i < 10; i++) {
            char rowLabel = (char) ('A' + i);

            for (int j = 0; j < 10; j++) {
                String coord = rowLabel + Integer.toString(j + 1);
                System.out.print("[ " + coord + " ]");
            }
            System.out.println();
        }
    }

    public void displayBoardShips() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                System.out.print("[ " + getSymbol(board[i][j]) + " ]");
            }
            System.out.println();
        }
    }

    private String getSymbol(Cell cell) {
        switch (cell) {
            case WATER: return "~";
            case CARRIER: return "C";
            case BATTLESHIP: return "B";
            case DESTROYER: return "D";
            case SUBMARINE: return "S";
            case FRIGATE: return "F";
            case HIT: return "X";
            case MISS: return "O";
            default: return "?";
        }
    }

    public boolean verifyCoord(String coord) {
        if (coord == null || coord.length() < 2 || coord.length() > 3) {
            return false;
        }

        coord = coord.toUpperCase();

        char rowChar = coord.charAt(0);
        if (rowChar < 'A' || rowChar > 'J') {
            return false;
        }

        try {
            int col = Integer.parseInt(coord.substring(1));
            return col >= 1 && col <= 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void addShip(String coord, char ship) {
        if (!verifyCoord(coord)) {
            System.out.println("Invalid coordinate.");
            return;
        }

        int[] pos = parseCoord(coord);
        int x = pos[0];
        int y = pos[1];

        switch (ship) {
            case 'c':
                placeShip(x, y, 5, Cell.CARRIER);
                break;
            case 'b':
                placeShip(x, y, 4, Cell.BATTLESHIP);
                break;
            case 'd':
                placeShip(x, y, 3, Cell.DESTROYER);
                break;
            case 's':
                placeShip(x, y, 3, Cell.SUBMARINE);
                break;
            case 'f':
                placeShip(x, y, 2, Cell.FRIGATE);
                break;
            default:
                System.out.println("Unknown ship type.");
        }
    }

    private boolean canPlaceShip(int x, int y, int length, boolean vertical) {
        if (vertical) {
            if (x + length > SIZE) return false;
            for (int i = 0; i < length; i++) {
                if (board[x + i][y] != Cell.WATER) return false;
            }
        } else {
            if (y + length > SIZE) return false;
            for (int i = 0; i < length; i++) {
                if (board[x][y + i] != Cell.WATER) return false;
            }
        }
        return true;
    }

    private void placeShip(int x, int y, int length, Cell type) {
        if (canPlaceShip(x, y, length, true)) {
            for (int i = 0; i < length; i++) {
                board[x + i][y] = type;
            }
        }
        else if (canPlaceShip(x, y, length, false)) {
            for (int i = 0; i < length; i++) {
                board[x][y + i] = type;
            }
        }
        else {
            System.out.println("Cannot place ship here.");
        }
    }
}