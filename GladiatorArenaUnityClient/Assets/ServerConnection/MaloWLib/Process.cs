using UnityEngine;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using System.Runtime.CompilerServices;

public abstract class Process
{
	public const int NOT_STARTED = 0, WAITING = 1, RUNNING = 2, FINISHED = 3;
	private const int DEFAULT_WARNING_THRESHOLD_EVENTQUEUE_FULL = 250;
	
	private static long nextPID = 0;
	
	private Thread proc = null;
	private Queue<ProcessEvent> eventQueue;
	private int state;
	private int warningThresholdEventQueue = DEFAULT_WARNING_THRESHOLD_EVENTQUEUE_FULL;
	private long ID;
	protected bool stayAlive = true;
	
	public Process()
	{
		this.ID = Process.nextPID;
		Process.nextPID++;
		this.state = NOT_STARTED;
		this.eventQueue = new Queue<ProcessEvent>();
		this.proc = new Thread(new ThreadStart(Run));
	}
	
	private void Run()
	{
		state = RUNNING;
		this.Life();
        state = FINISHED;
	}
	
	public abstract void Life();
	
	public void Start()
	{
		if (this.state == NOT_STARTED)
		{
			this.proc.Start();
		}
	}
	
	private void Suspend()
	{
		try
		{
			System.Threading.Thread.Sleep(Timeout.Infinite);
		}
		catch (System.Exception ex)
		{
			Debug.Log(ex.Message);
		}
	}
	
	public void Resume()
	{
		// Needed because WaitEvent is not completely synchronized, so if a thread wants to resume it while it's 
		// going to sleep we need to continuously call on it to restart until it responds. Either solve this a better way or start using 
		// PeekEvent() instead of WaitEvent() more and add a sleep between Peeks.
		while (this.state == WAITING)
			this.proc.Interrupt();
		
	}
	
	public void Close()
	{
		this.stayAlive = false;
		ProcessEvent ev = new ProcessEvent();
		this.PutEvent(ev);
		this.CloseSpecific();
	}
	
	public virtual void CloseSpecific()
	{
		
	}
	
	public void WaitUntillDone()
	{
		while (this.state != FINISHED)
			System.Threading.Thread.Sleep(1);
	}
	
	public ProcessEvent WaitEvent()
	{
		bool sleep = this.WaitEventCheckForSleep();
		
		if (sleep)
		{
			this.Suspend();
			this.state = RUNNING;
		}
		
		return this.WaitEventDequeEvent();
	}
	
	
	private bool WaitEventCheckForSleep()
	{
		lock (this)
		{
			bool sleep = this.eventQueue.Count == 0;
			
			if (sleep)
			{
				this.state = WAITING;
			}
			
			return sleep;
		}
	}
	
	private ProcessEvent WaitEventDequeEvent()
	{
		lock (this)
		{
			ProcessEvent ev = this.eventQueue.Dequeue();
			return ev;
		}
	}
	
	public ProcessEvent PeekEvent()
	{
		lock (this)
		{
			ProcessEvent ev = null;
			if (this.eventQueue.Count != 0)
			{
				ev = this.eventQueue.Dequeue();
			}
			
			return ev;
		}
	}
	
	public void PutEvent(ProcessEvent ev)
	{
		this.PutEvent(ev, true);
	}
	
	public void PutEvent(ProcessEvent ev, bool important)
	{
		lock (this)
		{
			bool go = true;
			if (!important)
			{
				if (this.eventQueue.Count > 20)
				{
					go = false;
				}
			}
			
			if (go)
			{
				
				int queueSize = this.eventQueue.Count;
				
				this.eventQueue.Enqueue(ev);
				
				if (queueSize > this.warningThresholdEventQueue)
				{
					Debug.Log("Warning, EventQueue of process " + this.ID + " has " + this.eventQueue.Count + " unread events.");
					this.warningThresholdEventQueue *= 2;
				}
				
				if (this.state == WAITING)
				{
					this.Resume();
				}
			}
		}
	}
	
	public int GetState() { return this.state; }
	
	public long GetID() { return this.ID; }
	public long GetNrOfProcs() { return Process.nextPID; }
	public int GetEventQueueSize() { return this.eventQueue.Count; }
}


