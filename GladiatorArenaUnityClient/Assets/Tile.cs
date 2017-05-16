using System;
using UnityEngine;

public class Tile
{
    public static int TILE_CHECKER = 36;
    public static int TILE_GRASS = 37;
    public static int TILE_BRICK = 38;

    public int type;
    public GameObject obj;

    public Tile(int type, GameObject obj)
    {
        this.type = type;
        this.obj = obj;
    }
}



