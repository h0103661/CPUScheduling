package com.lu.cpu;

public class Process implements Comparable<Process>{

	private String name;
	private int priority;
	private int burst;
	private int arrival;
	
	//FCFS
	private int FCFS_turn;
	private int FCFS_wait;
	
	public Process(String name, int priority, int burst, int arrival) {
		super();
		this.name = name;
		this.priority = priority;
		this.burst = burst;
		this.arrival = arrival;
	}
	
	/*
	 * 
	 */
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getBurst() {
		return burst;
	}

	public void setBurst(int burst) {
		this.burst = burst;
	}

	public int getArrival() {
		return arrival;
	}

	public void setArrival(int arrival) {
		this.arrival = arrival;
	}
	
	/*
	 * 
	 */

	public int getFCFS_turn() {
		return FCFS_turn;
	}

	public void setFCFS_turn(int fCFS_turn) {
		FCFS_turn = fCFS_turn;
	}

	public int getFCFS_wait() {
		return FCFS_wait;
	}

	public void setFCFS_wait(int fCFS_wait) {
		FCFS_wait = fCFS_wait;
	}

	@Override
	public int compareTo(Process o) {
		return this.getArrival() - o.getArrival();
	}

	
}
