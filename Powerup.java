import java.util.*;
import  javafx.scene.control.Label;

public class Powerup {
    
    //Reveal a random unsunk enemy ship. Returns the ship name, or null if all ships are sunk.
    public static String doRadar(Board botBoard, boolean[][] markedBot){
        Cell[] allShipTypes = {Cell.CARRIER, Cell.BATTLESHIP, Cell.DESTROYER, Cell.SUBMARINE, Cell.FRIGATE};
        String[] shipNames = {"Carrier", "Battleship", "Destroyer", "Submarine", "Frigate"};

        //Find ship types still on the board
        int[] unsunkIndices = new int[allShipTypes.length];
        int unsunkCount = 0;
        for (int s = 0; s < allShipTypes.length; s++) {
            boolean found = false;
            for (int i = 0; i < 10 && !found; i++) {
                for (int j = 0; j < 10 && !found; j++) {
                    if (botBoard.getCell(i, j) == allShipTypes[s]) {
                        unsunkIndices[unsunkCount++] = s;
                        found = true;
                    }
                }
            }
        }

        if (unsunkCount == 0) return null;

        //Pick one at random and mark its cells
        int pick = unsunkIndices[(int)(Math.random() * unsunkCount)];
        Cell chosen = allShipTypes[pick];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (botBoard.getCell(i, j) == chosen) {
                    markedBot[i][j] = true;
                }
            }
        }
        return shipNames[pick];
    }

    public static void doShield(Board playerBoard, Label botStatusLabel){
        //look for unsunken ships
        ArrayList<Cell> types = new ArrayList<Cell>();
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                if(playerBoard.getCell(i, j) != Cell.HIT && playerBoard.getCell(i, j) != Cell.MISS && playerBoard.getCell(i, j) != Cell.WATER && !types.contains(playerBoard.getCell(i,j))) {
                    types.add(playerBoard.getCell(i, j));
                }
            }
        }
        if(types.size() == 0) return;
        int index = (int) Math.random() * types.size();
        Cell chosen = types.get(index);
        boolean[][] toShield = playerBoard.getShieldedPlayer();
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                if(playerBoard.getCell(i,j) == chosen){
                    toShield[i][j] = true;
                }
            }
        }
        playerBoard.setShieldedPlayer(toShield);
        String shipName = chosen.toString().substring(0,1) + chosen.toString().substring(1).toLowerCase();
        botStatusLabel.setText("Shield has been applied to your " + shipName + "!");



    }

    public static void doReinforcements(Set<String> sunkShips, ShipAbilities abilities){
        //resets the cooldown of all ship abilities as long as the ship is still alive.
        if (!sunkShips.contains("Carrier")) {
            abilities.resetCarrierCooldown();
        }
        if (!sunkShips.contains("Submarine")) {
            abilities.resetSubmarineCooldown();
        }
        if (!sunkShips.contains("Frigate")) {
            abilities.toggleFrigate();
        }
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

    public static void doReposition(Board initBoard, Board playerBoard){
        //Count how many hits each type of ship has taken
        int carrierHealth = 5;
        int battleshipHealth = 4;
        int destroyerHealth = 3;
        int submarineHealth = 3;
        int frigateHealth = 2;

        int carrierPrio = 1;
        int battleshipPrio = 1;
        int destroyerPrio = 1;
        int submarinePrio = 1;
        int frigatePrio = 1;

        Cell curr = Cell.WATER;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (playerBoard.getCell(i, j) == Cell.HIT) {
                    curr = initBoard.getCell(i, j);
                    switch (curr) {
                        case CARRIER:
                            carrierHealth--;
                            if (carrierHealth == 0) {
                                carrierPrio = 0;
                            } else {
                                carrierPrio = 2;
                            }
                            break;
                        case BATTLESHIP:
                            battleshipHealth--;
                            if (battleshipHealth == 0) {
                                battleshipPrio = 0;
                            } else {
                                battleshipPrio = 2;
                            }
                            break;
                        case DESTROYER:
                            destroyerHealth--;
                            if (destroyerHealth == 0) {
                                destroyerPrio = 0;
                            } else {
                                destroyerPrio = 2;
                            }
                            break;
                        case SUBMARINE:
                            submarineHealth--;
                            if (submarineHealth == 0) {
                                submarinePrio = 0;
                            } else {
                                submarinePrio = 2;
                            }
                            break;
                        case FRIGATE:
                            frigateHealth--;
                            if (frigateHealth == 0) {
                                frigatePrio = 0;
                            } else {
                                frigatePrio = 2;
                            }
                            break;
                    }
                }
            }
        }

        //Decide which to move off of priority and randomization
        int usedPrio = 1;
        if (carrierPrio == 2 || battleshipPrio == 2 || destroyerPrio == 2 || submarinePrio == 2 || frigatePrio == 2) {
            usedPrio = 2;
        }

        int currLength = -1;
        boolean keepGoing = true;
        while (keepGoing) {
            int i = (int)(Math.random() * 5);
            switch (i) {
                case 0:
                    if (carrierPrio == usedPrio) {
                        curr = Cell.CARRIER;
                        currLength = 5;
                        keepGoing = false;
                    }
                    break;
                case 1:
                    if (battleshipPrio == usedPrio) {
                        curr = Cell.BATTLESHIP;
                        currLength = 4;
                        keepGoing = false;
                    }
                    break;
                case 2:
                    if (destroyerPrio == usedPrio) {
                        curr = Cell.DESTROYER;
                        currLength = 3;
                        keepGoing = false;
                    }
                    break;
                case 3:
                    if (submarinePrio == usedPrio) {
                        curr = Cell.SUBMARINE;
                        currLength = 3;
                        keepGoing = false;
                    }
                    break;
                case 4:
                    if (frigatePrio == usedPrio) {
                        curr = Cell.FRIGATE;
                        currLength = 2;
                        keepGoing = false;
                    }
                    break;
            }
        }

        //Find a valid location to move the ship to
        keepGoing = true;
        int row = -1;
        int col = -1;
        boolean isVertical = false;
        while (keepGoing) {
            row = (int)(Math.random() * 10);
            col = (int)(Math.random() * 10);
            if ((int)(Math.random() * 2) == 0) {
                isVertical = true;
            } else {
                isVertical = false;
            }

            if (playerBoard.canPlaceShipPublic(row, col, currLength, isVertical)) {
                keepGoing = false;
            }
        }
        
        //Remove existing ship from both the real board and the snapshot of the board from the start
        int hitsTaken = 0;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (initBoard.getCell(i, j) == curr) {
                    if (playerBoard.getCell(i, j) == Cell.HIT) {
                        hitsTaken++;
                    }
                    playerBoard.setCell(i, j, Cell.WATER);
                    initBoard.setCell(i, j, Cell.WATER);
                }
            }
        }
        
        //Place the ship in new location
        if (isVertical) {
            for (int i = 0; i < currLength; i++) {
                initBoard.setCell(row + i, col, curr);
                if (hitsTaken > 0) {
                    playerBoard.setCell(row + i, col, Cell.HIT);
                    hitsTaken--;
                } else {
                    playerBoard.setCell(row + i, col, curr);
                }
            }
        } else {
            for (int i = 0; i < currLength; i++) {
                initBoard.setCell(row, col + i, curr);
                if (hitsTaken > 0) {
                    playerBoard.setCell(row, col + i, Cell.HIT);
                    hitsTaken--;
                } else {
                    playerBoard.setCell(row, col + i, curr);
                }
            }
        }

        return;
    }
}
