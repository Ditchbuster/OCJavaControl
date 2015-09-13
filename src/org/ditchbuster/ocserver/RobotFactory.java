package org.ditchbuster.ocserver;

import asciiPanel.AsciiPanel;
import org.ditchbuster.ocasciiconsole.Robot;
import org.ditchbuster.ocasciiconsole.World;

/**
 * Created by CPearson on 9/13/2015.
 */
public class RobotFactory {
    private World world;

    public RobotFactory(World world){
        this.world = world;
    }

    public Robot newRobot(){
        Robot robot = new Robot(world, '@', AsciiPanel.brightWhite);
        world.addAt(robot,1,1);
        new RobotAi(robot);
        return robot;
    }
}
