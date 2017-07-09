using System;
using UnityEngine;

public static class MaloWDebug
{
	private static object _lock = new object();
	public static void Log(string msg)
	{
		try
		{
			lock (_lock)
			{
				using (System.IO.StreamWriter file = new System.IO.StreamWriter(@"C:\MaloWLog.txt", true))
				{
					file.WriteLine(msg);
				}
			}
		}
		catch (System.Exception ex)
		{
			Debug.Log(ex.Message);
		}
	}
}


