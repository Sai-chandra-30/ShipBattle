import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scnr = new Scanner(System.in);
        boolean gameStart = false;
        boolean random = false;

        while (!gameStart) {
            System.out.print("Start Game? (y/n): ");
            char userInput1 = Character.toLowerCase(scnr.next().charAt(0));

            if (userInput1 == 'y') {
                gameStart = true;
            }
            else if (userInput1 == 'n') {
                System.exit(0);
            }
            else {
                System.out.println("Please enter y or n");
            }
        }

        gameStart = false;

        while (!gameStart) {
            System.out.print("Pick a place for a ship, or do it randomly? (p/r): ");
            char userInput1 = Character.toLowerCase(scnr.next().charAt(0));

            if (userInput1 == 'p') {
                gameStart = true;
            }
            else if (userInput1 == 'r') {
                gameStart = true;
                random = true;
            }
            else {
                System.out.println("Please enter p or r");
            }
        }

        Board b1 = new Board();
        if(!random) {
            b1.displayBoardCoords();

            boolean validShip = false;
            char userInput2 = '0';
            System.out.println("\nCarrier: (c)\nBattleship: (b)\nDestroyer: (d)\nSubmarine: (s)\nFrigate: (f)");

            while (!validShip) {
                System.out.print("Choose Ship to place: ");
                userInput2 = Character.toLowerCase(scnr.next().charAt(0));

                switch (userInput2) {
                    case 'c':
                    case 'b':
                    case 'd':
                    case 's':
                    case 'f':
                        validShip = true;
                        break;
                    default:
                        System.out.println("Please enter a valid letter");
                }
            }

            boolean validCoordinate = false;
            String userInput3 = "";

            while (!validCoordinate) {
                System.out.print("Select coordinate: ");
                userInput3 = scnr.next().toUpperCase();

                if (b1.verifyCoord(userInput3)) {
                    validCoordinate = true;
                } else {
                    System.out.println("Enter valid coordinate.");
                }
            }

            b1.addShip(userInput3, userInput2);

            System.out.println("\nUpdated Board:");
        }
        else {
            b1 = RandomShip.makeShip();
        }
        //b1.displayBoardShips();
        GameManager.playGame(b1, RandomShip.makeShip());
        scnr.close();
    }
}