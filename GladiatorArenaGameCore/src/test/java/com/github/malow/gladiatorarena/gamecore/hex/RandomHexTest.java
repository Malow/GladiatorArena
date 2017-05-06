package com.github.malow.gladiatorarena.gamecore.hex;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class RandomHexTest
{
  @Test
  public void test()
  {
    int sizeX = 50;
    int sizeY = 50;
    HexagonMap m = new HexagonMap(sizeX, sizeY);
    long startTime;
    long endTime;
    DecimalFormat df = new DecimalFormat("#.##");
    Random rand = new Random();
    int distance;
    //
    startTime = System.nanoTime();
    Hex from = new Hex(rand.nextInt(sizeX), rand.nextInt(sizeY));
    Hex to = new Hex(rand.nextInt(sizeX), rand.nextInt(sizeY));
    from = new Hex(0, 5);
    to = new Hex(25, 5);
    System.out.println("From: " + from.toString() + " to: " + to.toString());
    endTime = System.nanoTime() - startTime;
    System.out.println("Time for coords: " + df.format(endTime / 1000000.0) + " ms");
    //
    startTime = System.nanoTime();
    distance = m.getDistanceInHexes(from, to);
    endTime = System.nanoTime() - startTime;
    System.out.println("Time for getDistance: " + df.format(endTime / 1000000.0) + " ms");
    System.out.println("Distance Exact: " + distance);
    //
    startTime = System.nanoTime();
    String sMap = m.getAsGraphicalStringWithDistances(from);
    endTime = System.nanoTime() - startTime;
    System.out.println("Time for getAsGraphicalStringWithDistances: " + df.format(endTime / 1000000.0) + " ms");
    System.out.println(sMap);
    //
    startTime = System.nanoTime();
    List<Hex> path = m.aStar(from, to);
    endTime = System.nanoTime() - startTime;
    System.out.println("Time for astar: " + df.format(endTime / 1000000.0) + " ms");
    String pathS = "";
    if (path != null)
    {
      for (Hex node : path)
      {
        pathS += node.toString() + " - ";
      }
      System.out.println("Path: Length: " + path.size());
      if (pathS.length() > 2)
      {
        pathS = pathS.substring(0, pathS.length() - 2);
      }
      System.out.println(pathS);
    }
    else
    {
      System.out.println("ERROR: couldnt find an astar path");
    }
    System.out.println("NBs_");
    List<Hex> ns = m.getNeighborsForHex(from);
    ns.stream().forEach(n -> System.out.println(n.toString()));
  }
}
