import java.util.Set;

public class ShipAbilities {
    private boolean carrierActive = false;
    private int carrierResetCooldown = 3;
    private int carrierCooldown = 0; 

    private boolean frigateActive = false;
    private boolean frigateUsed = false;

    private Set<String> sunkShips;

    public ShipAbilities(Set<String> sunkShips) {
        this.sunkShips = sunkShips;
    }

    // Carrier
    public boolean getCarrierActive() {
        return carrierActive;
    }

    public void toggleCarrier() {
        carrierActive = !carrierActive;
    }

    public int getCarrierCooldown() {
        return carrierCooldown;
    }
    public void decrementCarrierCooldown() {
        carrierCooldown--;
    }

    public void resetCarrierCooldown() {
        carrierCooldown = carrierResetCooldown;
    }

    public boolean[][] useCarrier(int row, int col, boolean[][] markedBot) {
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++) {
                if(i + row >= 0 && j + col >= 0 && i + row < 10 && j + col < 10) markedBot[row + i][col + j] = true;
            }
        }
        return markedBot;
    }

    public boolean getBattleshipActive() {
        return !sunkShips.contains("Battleship");
    }

    // Frigate

    public boolean getFrigateActive() {
        return frigateActive;
    }

    public void toggleFrigate() {
        frigateActive = !frigateActive;
    }

    public boolean isFrigateUsed() {
        return frigateUsed;
    }

    public void useFrigate(Board playerBoard) {
        frigateUsed = true;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (playerBoard.getCell(i, j) == Cell.FRIGATE) {
                    int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    for (int[] d : dirs) {
                        int ni = i + d[0], nj = j + d[1];
                        if (ni >= 0 && ni < 10 && nj >= 0 && nj < 10
                            && playerBoard.getCell(ni, nj) == Cell.HIT) {
                            playerBoard.setCell(ni, nj, Cell.FRIGATE);
                        }
                    }
                }
            }
        }
    }
}