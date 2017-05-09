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
    Hexagon from = new Hexagon(rand.nextInt(sizeX), rand.nextInt(sizeY));
    Hexagon to = new Hexagon(rand.nextInt(sizeX), rand.nextInt(sizeY));
    from = new Hexagon(0, 5);
    to = new Hexagon(25, 5);
    System.out.println("From: " + from.toString() + " to: " + to.toString());
    endTime = System.nanoTime() - startTime;
    System.out.println("Time for coords: " + df.format(endTime / 1000000.0) + " ms");
    //
    startTime = System.nanoTime();
    distance = HexagonHelper.getDistanceInHexes(from, to);
    endTime = System.nanoTime() - startTime;
    System.out.println("Time for getDistance: " + df.format(endTime / 1000000.0) + " ms");
    System.out.println("Distance Exact: " + distance);
    //
    startTime = System.nanoTime();
    String sMap = HexagonHelper.visualizeHexagonMapWithDistances(sizeX, sizeY, from);
    endTime = System.nanoTime() - startTime;
    System.out.println("Time for getAsGraphicalStringWithDistances: " + df.format(endTime / 1000000.0) + " ms");
    System.out.println(sMap);
    //
    startTime = System.nanoTime();
    List<Hexagon> path = m.getAStarPath(from, to);
    endTime = System.nanoTime() - startTime;
    System.out.println("Time for astar: " + df.format(endTime / 1000000.0) + " ms");
    String pathS = "";
    if (path != null)
    {
      for (Hexagon node : path)
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
    List<Hexagon> ns = m.getNeighborsForHex(from);
    ns.stream().forEach(n -> System.out.println(n.toString()));
  }

  @Test
  public void printCoords()
  {
    System.out.println(HexagonHelper.visualizeHexagonMapWithPositions(10, 10));
  }

  @Test
  public void getPathTest()
  {
    int sizeX = 50;
    int sizeY = 50;
    HexagonMap m = new HexagonMap(sizeX, sizeY);
    Hexagon from = new Hexagon(0, 0);
    Hexagon to = new Hexagon(5, 5);
    List<Hexagon> path = m.getAStarPath(from, to);
    for (Hexagon hex : path)
    {
      System.out.println("path.add(new Position(" + hex.x + ", " + hex.y + "));");
    }
  }
}
