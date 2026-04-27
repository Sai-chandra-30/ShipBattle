public class Powerup {

    //Int representing which powerup this is
    //  0 = Radar
    //  1 = Shield
    //  2 = Reeinforcements
    //  3 = Communication Disruption
    //  4 = Blackout
    //  5 = Rebuild
    //  6 = Reposition
    int type;

    //Constructor called when a powerup is generated
    public Powerup(){
        type = (int)(Math.random() * 7);
    }

    //Public method called when the powerup is used - switches into the correct one for the type
    public void usePowerup(){
        switch(type){
            case 0:
                doRadar();
                break;
            case 1:
                doShield();
                break;
            case 2:
                doReeinforcements();
                break;
            case 3:
                doCommunicationDisruption();
                break;
            case 4:
                doBlackout();
                break;
            case 5:
                doRebuild();
                break;
            case 6:
                doReposition();
                break;
        }
    }

    private void doRadar(){
        //TO DO
    }

    private void doShield(){
        //TO DO
    }

    private void doReeinforcements(){
        //TO DO
    }

    private void doCommunicationDisruption(){
        //TO DO
    }

    private void doBlackout(){
        
    }

    private void doRebuild(){

    }

    private void doReposition(){

    }



}