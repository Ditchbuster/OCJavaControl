package org.ditchbuster.ocserver;


/**
 * Created by CPearson on 9/13/2015.
 */
public class ScannerAi extends RobotAi {
    public ScannerAi(Robot robot){
        super(robot);
    }

    @Override
    public void onEnter(int x, int y, Tile tile) {
        robot.x = x;
        robot.y = y;
    }
}
