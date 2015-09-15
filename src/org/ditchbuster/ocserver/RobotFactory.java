package org.ditchbuster.ocserver;

/**
 * Created by CPearson on 9/13/2015.
 */
public class RobotFactory {
    private World world;

    public RobotFactory(World world){
        this.world = world;
    }

    public Robot newRobot(String name,Server.ClientThread ct){//TODO add the robot in properly
        Robot robot = new Robot(name,world,ct);
        world.addAt(robot,1,1);
        new RobotAi(robot);
        return robot;
    }
}
