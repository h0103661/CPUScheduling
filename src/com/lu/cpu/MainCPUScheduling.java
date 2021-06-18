package com.lu.cpu;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class MainCPUScheduling extends JFrame{

	private static MainCPUScheduling instance;
	
	private static final long serialVersionUID = 1L;
	private static String frameName = "109700015 呂宥融 CPU排程工具";
	
	private static boolean DEBUG = true;
	
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
	
	private List<String> listInput;
	private List<Process> listPro;
	private Map<Integer, Process> mapSchFCFS;
	private Map<Process, Integer> mapTimeRunFCFS;
	private Map<Process, Integer> mapTimeWaitFCFS;
	private Map<Integer, Process> mapSchSJF;
	private Map<Process, Integer> mapTimeRunSJF;
	private Map<Process, Integer> mapTimeWaitSJF;
	
	private void init() {
		
		initGui();
	}
	
	private void scheduling(File file) {
		//排程
		listInput = this.readFileFromFile(file);
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
			p.setFCFS_turn(i);
		}
		
		log("[FCFS] ========================");
		
		mapTimeWaitFCFS = this.getWaitingTime(listPro, mapSchFCFS);
		for(Process p : mapTimeWaitFCFS.keySet()) {
			Integer i = mapTimeWaitFCFS.get(p);
			log("[FCFS] " + p.getName() + " Waiting Time: " + i);
			p.setFCFS_wait(i);
		}
		
		log("[FCFS] ========================");
		
		//SJF
		
		log("[SJF] ========================");
		
		mapSchSJF = this.createSchedulingSJF(listPro);
		for(Integer i : mapSchSJF.keySet()) {
			Process p = mapSchSJF.get(i);
			log("[SJF] at " + i + " : " + p.getName());
		}
		
		log("[SJF] ========================");
		
		mapTimeRunSJF = this.getTurnaroundTime(listPro, mapSchSJF);
		for(Process p : mapTimeRunSJF.keySet()) {
			Integer i = mapTimeRunSJF.get(p);
			log("[SJF] " + p.getName() + " Turnaround Time: " + i);
			p.setSJF_turn(i);
		}
		
		log("[SJF] ========================");
		
		mapTimeWaitSJF = this.getWaitingTime(listPro, mapSchSJF);
		for(Process p : mapTimeWaitSJF.keySet()) {
			Integer i = mapTimeWaitSJF.get(p);
			log("[SJF] " + p.getName() + " Waiting Time: " + i);
			p.setSJF_wait(i);
		}
		
		log("[SJF] ========================");
		
		//gui
		
		clearGui();
		locY = 50;
		createGuiPic(locY, "FCFS", mapSchFCFS);
		locY += 100;
		createGuiPic(locY, "SJF", mapSchSJF);
		locY += 100;
        createGuiTable(locY);
	}
	
	/*
	 * read file from string location to list string
	 */
	
	private List<String> readFileFromFile(File file) {
		logDEBUG("[read file] ========================");
		
		List<String> listInputs = new ArrayList<String>();
		/*
		 * 讀取檔案
		 */
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
			log("[create list pro] set change time: " + this.getChangeTime());
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
	
	private Process pIdle = new Process("/", 0, 0, 0);
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
	
	private Map<Integer, Process> createSchedulingSJF(List<Process> listPro){
		logDEBUG("[sch SJF] ========================");
		
		Map<Integer, Process> mapSch = new TreeMap<Integer, Process>();
		Map<Process, Integer> mapPro = new TreeMap<Process, Integer>();
		for(Process p : listPro) {
			mapPro.put(p, p.getBurst());
		}
		
		//如果第一個p的到達時間不是0, 前面補idle
		Process nowP = null;
		int next = 0;
		
		for(int count = 0; count <= 999; count++) {
			next -= 1;
			if(next <= 0) {
				next = 0;
				if(nowP != null && nowP != pIdle && nowP != pEND) {
					mapPro.remove(nowP);
				}
				if(mapPro.isEmpty()) {
					mapSch.put(count, pEND);
					logDEBUG("[sch SJF] " + count + " END ");
					break;
				}
				for(Process p : mapPro.keySet()) {
					if(count < p.getArrival()) {
						mapSch.put(count, pIdle);
						logDEBUG("[sch SJF] " + count + " : " + pIdle.getName());
						nowP = pIdle;
						next = p.getArrival() - count;
					} else {
						mapSch.put(count, p);
						logDEBUG("[sch SJF] " + count + " : " + p.getName());
						nowP = p;
						next += p.getBurst();
					}
					break;
				}
			} else {
				boolean first = true;
				for(Process p : mapPro.keySet()) {
					if(first) {
						first = false;
						continue;
					}
					logDEBUG("[sch SJF] to cut " + next + "/" + count + " : " + p.getName());
					if(count > p.getArrival()) {
						if(next > p.getBurst()) {
							count -= 1;
							mapPro.put(nowP, next);
							mapSch.put(count, p);
							logDEBUG("[sch SJF] cut " + count + " : " + p.getName());
							nowP = p;
							next = p.getBurst();
						}
					}
					break;
				}
			}
		}
		
		logDEBUG("[sch SJF] ========================");
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
    
    private int locY = 0;
    
    private int picmult = 10;
    
    private void initGui() {
    	/*
		 * main frame
		 */
		layoutX = 1920;
        layoutY = 800;
        setSize(layoutX, layoutY);
        setTitle(frameName);
        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        layoutMain = new BorderLayout();
        setLayout(layoutMain);
        
        containerMain = getContentPane();
        containerMain.setLayout(null);
        
        /*
         * Basic
         */
        addChooseFileBtn();
        
        /*
         * finish
         */
        
        setVisible(true);
    }
    
    private void clearGui() {
    	containerMain.removeAll();
    	addChooseFileBtn();
    	containerMain.revalidate();
    	containerMain.repaint();
    }
    
    private void addChooseFileBtn() {
    	JButton btn_cf = new JButton("選擇檔案");
        btn_cf.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent a){
		        JFileChooser fileChooser = new JFileChooser();
		        int returnValue = fileChooser.showOpenDialog(null);
		        if (returnValue == JFileChooser.APPROVE_OPTION){
			        File selectedFile = fileChooser.getSelectedFile();
			        log("[select file] " + selectedFile.getName());
			        scheduling(selectedFile);
		        }
	        }
        });
        btn_cf.setBounds(0, 0, 75, 25);
        containerMain.add(btn_cf);
    }

	private void createGuiPic(int locY, String name, Map<Integer, Process> map) {
		int locX = 10;
		containerMain.add(addJLabel(name, locX - 5, locY + 25));
		locX += 30;
        String lastn = "";
        int lasti = -1;
        for(Integer i : map.keySet()) {
			Process p = map.get(i);
			if(lasti != -1) {
				containerMain.add(addJLabel(String.valueOf(lasti), locX, locY));
				int length = (i - lasti);
				containerMain.add(addJButton(lastn, locX, locY + 25, length));
				locX += (length * picmult);
			}
			lastn = p.getName();
			lasti = i;
			if(p == this.pEND) {
				containerMain.add(addJLabel(String.valueOf(i), locX - 5, locY));
			}
		}
	}
	
	private void createGuiTable(int locY) {
        String[] columns = {"Process", "priority", "burst", "arrival", "FCFS_Turn", "FCFS_Wait", "SJF_Turn", "SJF_Wait"};
        Object[][] list = new Object[listPro.size()][8];
        int count = 0;
        for(Process p : listPro) {
        	list[count][0] = p.getName();
        	list[count][1] = p.getPriority();
        	list[count][2] = p.getBurst();
        	list[count][3] = p.getArrival();
        	list[count][4] = p.getFCFS_turn();
        	list[count][5] = p.getFCFS_wait();
        	list[count][6] = p.getSJF_turn();
        	list[count][7] = p.getSJF_wait();
        	count++;
        }
        JTable jt = new JTable(list, columns);
        
        JScrollPane scrollPane = new JScrollPane(jt);  
        jt.setFillsViewportHeight(true);
        scrollPane.setBounds(0, locY, layoutX, layoutY);
        containerMain.add(scrollPane);
	}
	
	private JLabel addJLabel(String title, int x, int y) {
		int length = title.length() * 8;
		
		JLabel j = new JLabel(title);
		j.setBounds(x, y, length, 25);
		
		return j;
	}
	
	private JButton addJButton(String title, int x, int y, int length) {
		JButton j = new JButton(title);
		j.setBounds(x, y, (length * picmult), 25);
		
		return j;
	}
}
