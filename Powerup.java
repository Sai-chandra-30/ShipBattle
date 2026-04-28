public class Powerup {
    
    public static void doRadar(){
        //TO DO
    }

    public static void doShield(){
        //TO DO
    }

    public static void doReinforcements(){
        //TO DO
    }

    public static void doCommunicationDisruption(){
        //TO DO
    }

    public static void doBlackout(Board playerBoard){
        //For each cell...
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                //If it is marked as a miss, remove the mark
                if (playerBoard.getCell(i, j) == Cell.MISS) {
                    playerBoard.setCell(i, j, Cell.WATER);
                }
            }
        }
    }

    public static boolean doRebuild(Board initBoard, Board playerBoard){
        //Count how many hits each type of ship has taken
        int carrierHealth = 5;
        int battleshipHealth = 4;
        int destroyerHealth = 3;
        int submarineHealth = 3;
        int frigateHealth = 2;
        Cell curr;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (playerBoard.getCell(i, j) == Cell.HIT) {
                    curr = initBoard.getCell(i, j);
                    switch (curr) {
                        case CARRIER:
                            carrierHealth--;
                            break;
                        case BATTLESHIP:
                            battleshipHealth--;
                            break;
                        case DESTROYER:
                            destroyerHealth--;
                            break;
                        case SUBMARINE:
                            submarineHealth--;
                            break;
                        case FRIGATE:
                            frigateHealth--;
                            break;
                    }
                }
            }
        }

        //Decide which to rebuild off of priority
        if (carrierHealth == 0) {
            curr = Cell.CARRIER;
        } else if (battleshipHealth == 0) {
            curr = Cell.BATTLESHIP;
        } else if (destroyerHealth == 0) {
            curr = Cell.DESTROYER;
        } else if (submarineHealth == 0) {
            curr = Cell.SUBMARINE;
        } else if (frigateHealth == 0) {
            curr = Cell.FRIGATE;
        } else {
            return false; //If there are no sunk ships, Rebuild fails and does not consume the powerup
        }
        
        //Rebuild the ship
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if ((playerBoard.getCell(i, j) == Cell.HIT) && (initBoard.getCell(i, j) == curr)) {
                    playerBoard.setCell(i, j, curr);
                }
            }
        }
        return true;
    }

    public static void doReposition(){
        //TO DO (Liam)
    }
}
