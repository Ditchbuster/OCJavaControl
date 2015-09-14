package org.ditchbuster.ocserver;


/**
 * Created by CPearson on 9/13/2015.
 */
public class Robot {
    private World world;
    private Server.ClientThread ct;
    private String name;

    public int x;
    public int y;

    private RobotAi ai;
    public void setRobotAi(RobotAi ai) {this.ai =ai;}



    public Robot(World world){
        this.world = world;

    }
    public Robot(String name, World world, Server.ClientThread ct){
        this.name = name;
        this.world = world;
        this.ct = ct;
    }

    public void move(int x, int y){
        //TODO: should send a new destination to the server, let the server figure out how to get there
    }
    public void moveBy(int mx, int my){
        ai.onEnter(x+mx, y+my, world.tile(x+mx,y+my));
    }
}
