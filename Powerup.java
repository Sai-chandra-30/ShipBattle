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

    public static void doRebuild(){
        //TO DO (Liam)
    }

    public static void doReposition(){
        //TO DO (Liam)
    }
}
