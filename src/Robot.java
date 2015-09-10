/**
 * Created by CPearson on 9/5/2015.
 */
public class Robot {
    String name;
    State state;
    Robot(){
        this("tmp");
    }
    Robot(String in){
        name=in;
        state = State.IDLE;
    }

    public void cmd(String line){

    }





    public State getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum State{
        IDLE, MOVING, DIGGING
    }
}
