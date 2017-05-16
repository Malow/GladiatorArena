using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MapScript : MonoBehaviour {

    public List<List<Tile>> tiles = new List<List<Tile>>();

	// Use this for initialization
	void Start () {
        for (int i = 0; i < 100; i++)
        {
            List<Tile> row = new List<Tile>();
            float posX = i * 0.866f;
            float posYOffset = i % 2 == 0 ? 0 : 0.5f;
            for (int u = 0; u < 50; u++)
            {
                GameObject templateTile = GameObject.FindGameObjectWithTag("hexagon");
                GameObject o = (GameObject)GameObject.Instantiate(templateTile, new Vector3(posX, 0, u + posYOffset), new Quaternion(0, 0, 0, 0));
                Tile wTile = new Tile(0, o);
                row.Add(wTile);
            }
            this.tiles.Add(row);
        }
    }
	
	// Update is called once per frame
	void Update () {
		
	}
}
