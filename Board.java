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

    public Cell[][] getBoard() {
        return board;
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
                System.out.print("[ " + board[i][j].getSymbol() + " ]");
            }
            System.out.println();
        }
    }

    public boolean verifyCoord(String coord) {
        if (coord == null || coord.length() < 2 || coord.length() > 3) return false;
        coord = coord.toUpperCase();
        char rowChar = coord.charAt(0);
        if (rowChar < 'A' || rowChar > 'J') return false;
        try {
            int col = Integer.parseInt(coord.substring(1));
            return col >= 1 && col <= 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Converts a grid position to a board coordinate string like "A1"
    public String toCoord(int row, int col) {
        return (char) ('A' + row) + String.valueOf(col + 1);
    }

    // Accepts the ship's display name (e.g. "Battleship") instead of a char code
    public void addShipByName(String coord, String shipName) {
        char ship;
        switch (shipName) {
            case "Carrier":    ship = 'c'; break;
            case "Battleship": ship = 'b'; break;
            case "Destroyer":  ship = 'd'; break;
            case "Submarine":  ship = 's'; break;
            case "Frigate":    ship = 'f'; break;
            default: System.out.println("Unknown ship: " + shipName); return;
        }
        addShip(coord, ship);
    }

    public void addShip(String coord, char ship) {
        if (!verifyCoord(coord)) {
            System.out.println("Invalid coordinate.");
            return;
        }
        int[] pos = parseCoord(coord);
        int x = pos[0], y = pos[1];
        switch (ship) {
            case 'c': placeShip(x, y, 5, Cell.CARRIER);    break;
            case 'b': placeShip(x, y, 4, Cell.BATTLESHIP); break;
            case 'd': placeShip(x, y, 3, Cell.DESTROYER);  break;
            case 's': placeShip(x, y, 3, Cell.SUBMARINE);  break;
            case 'f': placeShip(x, y, 2, Cell.FRIGATE);    break;
            default:  System.out.println("Unknown ship type.");
        }
    }

    private boolean canPlaceShip(int x, int y, int length, boolean vertical) {
        if (vertical) {
            if (x + length > SIZE) return false;
            for (int i = 0; i < length; i++) if (board[x + i][y] != Cell.WATER) return false;
        } else {
            if (y + length > SIZE) return false;
            for (int i = 0; i < length; i++) if (board[x][y + i] != Cell.WATER) return false;
        }
        return true;
    }

    private void placeShip(int x, int y, int length, Cell type) {
        if (canPlaceShip(x, y, length, true)) {
            for (int i = 0; i < length; i++) board[x + i][y] = type;
        } else if (canPlaceShip(x, y, length, false)) {
            for (int i = 0; i < length; i++) board[x][y + i] = type;
        } else {
            System.out.println("Cannot place ship here.");
        }
    }

    // Returns the cell at the given grid position
    public Cell getCell(int row, int col) {
        return board[row][col];
    }
    // Sets the cell to the given state
    public void setCell(int row, int col, Cell c) {
        board[row][col] = c;
    }

    // Returns the ship length for a given ship name
    public static int getShipLength(String shipName) {
        switch (shipName) {
            case "Carrier":    return 5;
            case "Battleship": return 4;
            case "Destroyer":  return 3;
            case "Submarine":  return 3;
            case "Frigate":    return 2;
            default:           return 0;
        }
    }

    // Exposes private canPlaceShip for drag-and-drop preview validation
    public boolean canPlaceShipPublic(int row, int col, int length, boolean vertical) {
        return canPlaceShip(row, col, length, vertical);
    }

    // Places a ship at the given coord with explicit orientation; returns true if successful
    public boolean addShipByNameOriented(String coord, String shipName, boolean vertical) {
        int length = getShipLength(shipName);
        Cell type = nameToCell(shipName);
        if (length == 0 || type == null) return false;
        if (!verifyCoord(coord)) return false;
        int[] pos = parseCoord(coord);
        int x = pos[0], y = pos[1];
        if (!canPlaceShip(x, y, length, vertical)) return false;
        if (vertical) {
            for (int i = 0; i < length; i++) board[x + i][y] = type;
        } else {
            for (int i = 0; i < length; i++) board[x][y + i] = type;
        }
        return true;
    }

    // Returns the ship display name at (row, col), or null if not a ship
    public String getShipName(int row, int col) {
        return cellToName(board[row][col]);
    }

    // Removes all cells of the ship at (row, col); returns ship name or null
    public String removeShipAt(int row, int col) {
        Cell c = board[row][col];
        if (c == Cell.WATER || c == Cell.HIT || c == Cell.MISS) return null;
        String name = cellToName(c);
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] == c) board[i][j] = Cell.WATER;
        return name;
    }

    private Cell nameToCell(String shipName) {
        switch (shipName) {
            case "Carrier":    return Cell.CARRIER;
            case "Battleship": return Cell.BATTLESHIP;
            case "Destroyer":  return Cell.DESTROYER;
            case "Submarine":  return Cell.SUBMARINE;
            case "Frigate":    return Cell.FRIGATE;
            default:           return null;
        }
    }

    private String cellToName(Cell c) {
        switch (c) {
            case CARRIER:    return "Carrier";
            case BATTLESHIP: return "Battleship";
            case DESTROYER:  return "Destroyer";
            case SUBMARINE:  return "Submarine";
            case FRIGATE:    return "Frigate";
            default:         return null;
        }
    }

    // Fires at (row, col); returns true if it hit a ship
    public boolean fireAt(int row, int col) {
        Cell c = board[row][col];
        if (c == Cell.HIT || c == Cell.MISS) return false;
        if (c != Cell.WATER) {
            board[row][col] = Cell.HIT;
            return true;
        }
        board[row][col] = Cell.MISS;
        return false;
    }

    // Returns true if every ship cell has been hit
    public boolean allShipsSunk() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                Cell c = board[i][j];
                if (c != Cell.WATER && c != Cell.HIT && c != Cell.MISS)
                    return false;
            }
        return true;
    }

    // Returns all cells that have not been fired at yet
    public List<int[]> getUnfiredCells() {
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] != Cell.HIT && board[i][j] != Cell.MISS)
                    cells.add(new int[]{i, j});
        return cells;
    }
}
