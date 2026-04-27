public class ShipAbilities {
    private boolean carrierActive = false;
    private int carrierResetCooldown = 3;
    private int carrierCooldown = 0; 

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
}