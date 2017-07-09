
using UnityEngine;
using System.Collections.Generic;
using Newtonsoft.Json;

public class Entity
{
	public long GUID;
	public float posx;
	public float posy;
	public float posz;
	public float speed;
	public float forwardx;
	public float forwardy;
	public float forwardz;
	public int lastProcessedInput;
	public float blockLevel;

	public Entity (long GUID, float posx, float posy, float posz, float speed, float forwardx, float forwardy, float forwardz, int lastProcessedInput, float blockLevel)
	{
		this.GUID = GUID;
		this.posx = posx;
		this.posy = posy;
		this.posz = posz;
		this.speed = speed;
		this.forwardx = forwardx;
		this.forwardy = forwardy;
		this.forwardz = forwardz;
		this.lastProcessedInput = lastProcessedInput;
		this.blockLevel = blockLevel;
	}
}

public class PositionUpdate : ModelInterface
{
	public string type = "PositionUpdate";
	public List<Entity> entities;

	public PositionUpdate (List<Entity> entities)
	{
		this.entities = entities;
	}
	
	public override string ToNetworkString()
	{
		return JsonConvert.SerializeObject(this);
	}
	
	public static PositionUpdate ToModel(string networkString)
	{
		PositionUpdate req = JsonConvert.DeserializeObject<PositionUpdate>(networkString);
		if (req != null && req.type == "PositionUpdate")
			return req;
		return null;
	}
}