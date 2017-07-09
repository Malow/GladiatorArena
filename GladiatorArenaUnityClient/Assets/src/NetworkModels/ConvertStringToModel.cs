using UnityEngine;
using System;


public class ConvertStringToModel
{
	public static ModelInterface ToModel(String networkString)
	{
		ModelInterface mi = null;

		mi = PositionUpdate.ToModel (networkString);
		if (mi != null)
			return mi;		
    
		if (mi == null)
			Debug.Log ("FAILED! FAILED! FAILED! to add new network model: " + networkString);

		return mi;
	}
}


