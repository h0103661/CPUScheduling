package com.lu.cpu;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.JFrame;

public class MainCPUScheduling extends JFrame implements WindowListener{

	private static MainCPUScheduling instance;
	
	private static final long serialVersionUID = 1L;
	private static String frameName = "109700015 CPU排程工具";
	
	private static boolean DEBUG = false;
	
	/*
	 * create instance
	 */

	public static void main(String[] args) {
		instance = new MainCPUScheduling();
        instance.init();
	}
	
	public MainCPUScheduling() {
		
	}
	
	public static MainCPUScheduling getInstance() {
		return instance;
	}
	
	/*
	 * logDEBUG
	 */
	
	public void log(String msg) {
    	System.out.println(msg);
    	//pushlogDEBUG(msg);if(DEBUG)
	}
	
	public void logDEBUG(String msg) {
		if(DEBUG)System.out.println(msg);
    	//pushlogDEBUG(msg);
	}
	
	/*
	 * init
	 */
	
	private String fileInputName = "input.txt";
	private List<String> listInput;
	private List<Process> listPro;
	private Map<Integer, Process> mapSchFCFS;
	private Map<Process, Integer> mapTimeRunFCFS;
	private Map<Process, Integer> mapTimeWaitFCFS;
	
	private void init() {
		//排程
		listInput = this.readFileFromString(fileInputName);
		listPro = this.createListPro(listInput);
		
		//sort by arrival
		Collections.sort(listPro);
		for(Process p : listPro) {
			log("[after sort] process " + p.getName() + " : " + p.getPriority() + ", " + p.getBurst() + ", " + p.getArrival());
		}
		
		//FCFS
		
		log("[FCFS] ========================");
		
		mapSchFCFS = this.createSchedulingFCFS(listPro);
		for(Integer i : mapSchFCFS.keySet()) {
			Process p = mapSchFCFS.get(i);
			log("[FCFS] at " + i + " : " + p.getName());
		}
		
		log("[FCFS] ========================");
		
		mapTimeRunFCFS = this.getTurnaroundTime(listPro, mapSchFCFS);
		for(Process p : mapTimeRunFCFS.keySet()) {
			Integer i = mapTimeRunFCFS.get(p);
			log("[FCFS] " + p.getName() + " Turnaround Time: " + i);
		}
		
		log("[FCFS] ========================");
		
		mapTimeWaitFCFS = this.getWaitingTime(listPro, mapSchFCFS);
		for(Process p : mapTimeWaitFCFS.keySet()) {
			Integer i = mapTimeWaitFCFS.get(p);
			log("[FCFS] " + p.getName() + " Waiting Time: " + i);
		}
		
		log("[FCFS] ========================");
		
		//gui
		/*
		initGui();
		addWindowListener(this);*/
	}
	
	/*
	 * read file from string location to list string
	 */
	
	private List<String> readFileFromString(String loc) {
		logDEBUG("[read file] ========================");
		
		List<String> listInputs = new ArrayList<String>();
		/*
		 * 讀取檔案
		 */
		File file = new File(loc);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader buffer = new BufferedReader(reader);
		Scanner scan = new Scanner(buffer);
		
		/*
		 * 讀取字元
		 */
		while(scan.hasNext()){
			String next = scan.next();
			listInputs.add(next);
			logDEBUG("[read file] read: " + next);
		}
		
		/*
		 * 結束讀取
		 */
		scan.close();
		try {
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logDEBUG("[read file] ========================");
		
		return listInputs;
	}
	
	private int changeTime = -1;
	
	public int getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(int changeTime) {
		this.changeTime = changeTime;
	}

	private List<Process> createListPro(List<String> listInput){
		logDEBUG("[create list pro] ========================");
		
		List<Process> list = new ArrayList<Process>();
		
		//get change time
		int count = 0;
		while(this.getChangeTime() == -1) {
			String input = listInput.get(count);
			int in = 0;
			try {
				in = Integer.parseInt(input);
			} catch(NumberFormatException e) {
				count++;
				continue;
			}
			this.setChangeTime(in);
			logDEBUG("[create list pro] set change time: " + this.getChangeTime());
		}
		
		//remove change time from list
		listInput.remove(count);
		
		//format process
		String name = "";
		int priority = -1;
		int burst = -1;
		int arrival = -1;
		for(String s : listInput) {
			if(name.isEmpty() || name.isBlank()) {
				name = s;
				
				continue;
			} else if(priority == -1) {
				int in = 0;
				try {
					in = Integer.parseInt(s);
				} catch(NumberFormatException e) {
					continue;
				}
				priority = in;
				continue;
			} else if(burst == -1) {
				int in = 0;
				try {
					in = Integer.parseInt(s);
				} catch(NumberFormatException e) {
					continue;
				}
				burst = in;
				continue;
			} else if(arrival == -1) {
				int in = 0;
				try {
					in = Integer.parseInt(s);
				} catch(NumberFormatException e) {
					continue;
				}
				arrival = in;
				
				//create new process
				list.add(new Process(name, priority, burst, arrival));
				logDEBUG("[create list pro] new process " + name + " : " + priority + ", " + burst + ", " + arrival);
				
				//init
				name = "";
				priority = -1;
				burst = -1;
				arrival = -1;
				
				continue;
			}
		}
		
		logDEBUG("[create list pro] ========================");
		
		return list;
	}
	
	/*
	 * scheduling
	 */
	
	private Process pIdle = new Process("Idle", 0, 0, 0);
	private Process pEND = new Process("END", 0, 0, 0);
	
	private Map<Integer, Process> createSchedulingFCFS(List<Process> listPro){
		logDEBUG("[sch FCFS] ========================");
		
		Map<Integer, Process> mapSch = new TreeMap<Integer, Process>();
		
		//如果第一個p的到達時間不是0, 前面補idle
		int next = 0;
		
		for(Process p : listPro) {
			//如果下一個時間比到達時間早, 補idle
			if(next < p.getArrival()) {
				mapSch.put(next, pIdle);
				logDEBUG("[sch FCFS] at " + next + " : " + pIdle.getName());
				next = p.getArrival();
			}
			mapSch.put(next, p);
			logDEBUG("[sch FCFS] at " + next + " : " + p.getName());
			next += p.getBurst();
		}
		
		mapSch.put(next, pEND);
		logDEBUG("[sch FCFS] at " + next + " END ");
		
		logDEBUG("[sch FCFS] ========================");
		return mapSch;
	}
	
	private Map<Process, Integer> getTurnaroundTime(List<Process> listPro, Map<Integer, Process> mapSch){
		logDEBUG("[get turn time] ========================");
		
		Map<Process, Integer> mapTimeRun = new TreeMap<Process, Integer>();
		
		boolean next = false;
		for(Process pro : listPro) {
			int lastRunTime = 0;
			for(Integer i : mapSch.keySet()) {
				if(next) {
					next = false;
					lastRunTime = Integer.max(lastRunTime, i);
				} else {
					if(mapSch.get(i) == pro) {
						next = true;
					}
				}
			}
			mapTimeRun.put(pro, lastRunTime - pro.getArrival());
		}
		
		
		logDEBUG("[get turn time] ========================");
		
		return mapTimeRun;
	}
	
	private Map<Process, Integer> getWaitingTime(List<Process> listPro, Map<Integer, Process> mapSch){
		logDEBUG("[get wait time] ========================");
		
		Map<Process, Integer> mapTimeWait = new TreeMap<Process, Integer>();
		
		for(Process pro : listPro) {
			for(Integer i : mapSch.keySet()) {
				if(mapSch.get(i) == pro) {
					mapTimeWait.put(pro, i - pro.getArrival());
				}
			}
		}
		
		
		logDEBUG("[get wait time] ========================");
		
		return mapTimeWait;
	}
	
	/*
	 * create gui
	 */
	
	private int layoutX, layoutY;
    private BorderLayout layoutMain;
    private Container containerMain;

	private void initGui() {
		/*
		 * main frame
		 */
		layoutX = 1200;
        layoutY = 1000;
        setSize(layoutX, layoutY);
        setTitle(frameName);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        layoutMain = new BorderLayout();
        setLayout(layoutMain);
        
        containerMain = getContentPane();
        containerMain.setLayout(null);
	}
	
	/*
	 * WindowListener
	 */

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
