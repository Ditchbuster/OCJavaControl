package org.ditchbuster.ocserver;

/**
 * Created by CPearson on 9/12/2015.
 */
public enum Tile {
    FLOOR,
    WALL,
    BOUNDS;


    Tile(){
    }
    public boolean isDiggable(){
        return this==Tile.WALL;
        //TODO: check if any blocks/tiles are not diggable
    }
}
