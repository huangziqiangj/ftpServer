package org.hzq.ftpService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class CheckOut extends Thread{
	public static volatile boolean isRestart=false;
	@Override
	public void run() {
		while(true){
			BufferedReader read=null;
			FileWriter writer=null;
			try {
				Thread.sleep(5000);
				File file=new File(Main.runTimePath + "/bin/checkOut.conf");
				read=new BufferedReader(new FileReader(file));
				String readLine = read.readLine();
				if(readLine.trim().equals("out=1")){
					writer=new FileWriter(file);
					writer.write("out=0\r\nlogcat=false");
					writer.flush();
					writer.close();
					read.close();
					System.out.println("系统即将退出!!!");
					System.exit(0);
					return;
				}
				String logcat = read.readLine().trim();
				Main.logPrint=logcat.equals("logcat=true");
				read.close();
				read=null;
			} catch (Exception e) {
				try {
					if(writer!=null){
						writer.close();
					}
					if(read!=null){
						read.close();
					}
					
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
	
		
}
