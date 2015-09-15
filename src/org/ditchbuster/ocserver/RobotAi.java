package org.ditchbuster.ocserver;



/**
 * Created by CPearson on 9/13/2015.
 */
public class RobotAi {
    protected Robot robot;

    public RobotAi(Robot robot){
        this.robot = robot;
        this.robot.setRobotAi(this);
    }

    public void onEnter(int x, int y, Tile tile){}
}
