import java.util.*;

/*
CS 321: Ship Battles
Random Ship Configuration
By: Connor Harrison
*/

//creates random ship configurations (to be run when random button is clicked)
public class RandomShip {
    public static void main(String[] args) {
        int[][] grid = new int[10][10];     //grid to hold ships
        printGrid(grid);                    //print for a before
        System.out.println();
        boolean legal = false;              //determines if the placed ship is legal or not
        boolean done = false;               //determines if we are done placing ships
        boolean giveup = false;             //determines if we should give up (if an impossible configuration is reached)
        int initx = 0;                      //the starting x position of the ship (leftmost value)
        int inity = 0;                      //the starting y position of the ship (uppermost value)
        int tries = 0;                      //how many tries were taken to place a ship legally
        while(!done) { //loop until we are done
            for (int i = 1; i <= 5; i++) { //loop over all 5 ships
                legal = false; //assume that the next ship is illegal and reset tries to 0
                tries = 0;
                if (Math.random() < 0.5) { //flip a coin, if heads, place a horizontal ship. Otherwise, place a vertical ship
                    while (!legal) { //keep trying until a legal configuration is found
                        initx = (int) (Math.random() * (10 - getSize(i))); //randomly generate inital x
                        inity = (int) (Math.random() * 10);                //randomly generate inital y
                        legal = isLegal(initx, inity, getSize(i), true, grid); //check if the placement is legal from the helper method
                        tries++; //mark attempt as a try
                        if(tries == 100) { //if we have attempted 100 times with no legal results, give up
                            giveup = true;
                            break;
                        }
                    }
                    if(!giveup) { //if we haven't given up, add a horizontal ship in the specified coordinates
                        grid = addHorizontal(grid, i, initx, inity);
                    }
                    else { //otherwise, reset the grid, reset the giveup variable, and restart the ship placment loop from the beginning
                        grid = new int[10][10];
                        giveup = false;
                        break;
                    }
                } else {
                    while (!legal) { //keep trying until a legal configuration is found
                        initx = (int) (Math.random() * 10);                 //randomly generate inital x
                        inity = (int) (Math.random() * (10 - getSize(i)));  //randomly generate inital y
                        legal = isLegal(initx, inity, getSize(i), false, grid); //check if the placement is legal from the helper method
                        tries++;    //mark attempt as a try
                        if(tries == 100) { //if we have attempted 100 times with no legal results, give up
                            giveup = true;
                            break;
                        }
                    }
                    if(!giveup) { //if we haven't given up, add a horizontal ship in the specified coordinates
                        grid = addVertical(grid, i, initx, inity);
                    }
                    else { //otherwise, reset the grid, reset the giveup variable, and restart the ship placment loop from the beginning
                        grid = new int[10][10];
                        giveup = false;
                        break;
                    }
                }
                if(i == 5) { //if we have placed the final ship without giving up, we are done
                    done = true;
                }
            }
        }
        grid = cleanLegalCheck(grid); //remove extra markings made by isLegal() to reveal the final board
        printGrid(grid);
    }

    public static boolean isLegal(int x, int y, int size, boolean isHori, int[][] grid) {
        //make the starting point the max of -1 and the coordinate - 1 (prevent out-of-bounds)
        int startX = Math.max(-1, x - 1);
        int startY = Math.max(-1, y - 1);
        if(isHori) { //if this is a horizontal ship, check horizontally
            for(int i = startX; i < startX + size + 2; i++) { //loop the ship's size + 2 (an extra space infront and behind)
                for(int j = startY; j < startY + 3; j++) {
                    if((i >= 0 && i < 10 && j >= 0 && j < 10) && grid[i][j] != 0) { //if there is a non-empty coordinate, this is not legal
                        return false;
                    }
                }
            }
        }
        else {
            for(int i = startX; i < startX + 3; i++) { //loop 3 times (1 for to the left of the ship, 1 for at the ship, and 1 for to the right of the ship)
                for(int j = startY; j < startY + size + 2; j++) { //loop the ship's size + 2 (an extra space infront and behind)
                    if((i >= 0 && i < 10 && j >= 0 && j < 10) && grid[i][j] != 0) { //if there is a non-empty coordinate, this is not legal
                        return false;
                    }
                }
            }
        }
        return true; //if we didn't find any illegal spaces, return true, this is legal.
    }

    public static int[][] cleanLegalCheck(int[][] grid) {
        for(int i = 0; i < grid.length; i++) { //loop through the entire grid, removing all 9s that were added to act as ship spacing
            for(int j = 0; j < grid[i].length; j++) {
                if(grid[i][j] == 9) {
                    grid[i][j] = 0;
                }
            }
        }
        return grid;
    }
    public static int[][] addHorizontal(int[][] grid, int id, int initx, int inity) {
        int size = getSize(id); //get the size of the ship
        for(int i = 0; i < size + 2; i++) {  //if placement won't go out-of-bounds, and is past the size of the ship, place a 9, otherwise, place the ship's id if it is in bounds
            if((i == 0 && initx != 0) || (i == size + 1 && initx < 10 - size)) {
                grid[initx + i - 1][inity] = 9;
            }
            else if (i != 0 && i < 11 - size) {
                grid[initx + i - 1][inity] = id;
            }
        }
        for(int i = 0; i < size + 2; i++) { //loop above the ship to add spacing if it won't go out-of-bounds
            if(initx + i - 1 >= 0 && initx + i - 1 < 10 && inity > 0){
                grid[initx + i - 1][inity - 1] = 9;
            }
        }
        for(int i = 0; i < size + 2; i++) { //loop below the ship to add spacing if it won't go out-of-bounds
            if(initx + i - 1 >= 0 && initx + i - 1 < 10 && inity < 9){
                grid[initx + i - 1][inity + 1] = 9;
            }
        }
        return grid; //return grid with added ship
    }

    public static int[][] addVertical(int[][] grid, int id, int initx, int inity) {
        int size = getSize(id); //get the size of the ship
        for(int i = 0; i < size + 2; i++) {
            if((i == 0 && inity != 0) || (i == size + 1 && inity < 10 - size)) { //if placement won't go out-of-bounds, and is past the size of the ship, place a 9, otherwise, place the ship's id if it is in bounds
                grid[initx][inity + i - 1] = 9;
            }
            else if (i != 0 && i < 11 - size) {
                grid[initx][inity + i - 1] = id;
            }
        }
        for(int i = 0; i < size + 2; i++) { //loop to the left of the ship to add spacing if it won't go out-of-bounds
            if(inity + i - 1 >= 0 && inity + i - 1 < 10 && initx > 0){
                grid[initx - 1][inity + i - 1] = 9;
            }
        }
        for(int i = 0; i < size + 2; i++) { //loop to the right of the ship to add spacing if it won't go out-of-bounds
            if(inity + i - 1 >= 0 && inity + i - 1 < 10 && initx < 9){
                grid[initx + 1][inity + i - 1] = 9;
            }
        }
        return grid; //return grid with added ship
    }

    public static int getSize(int id) {
        //returns the size of the ship based on an id.
        int size = 0;
        if(id == 1) { //Destroyer
            size = 3;
        }
        if(id == 2) { //Carrier
            size = 5;
        }
        if(id == 3) { //Battleship
            size = 4;
        }
        if(id == 4) { //Submarine
            size = 3;
        }
        if(id == 5) { //Frigate
            size = 2;
        }
        return size;
    }

    public static void printGrid(int[][] grid) {
        //debug method that prints the enitre grid
        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[1].length; j++)
                System.out.print(grid[j][i] + " ");
            System.out.println();
        }
    }
}