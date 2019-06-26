package org.hzq.ftpService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyLog extends Thread {
	private int next;
	private List<Messige> works1 = new ArrayList<Messige>();
	private List<Messige> works2 = new ArrayList<Messige>();
	private File file;
	private volatile boolean worksFlag=true;
	@Override
	public void run() {
		try {
			nextFileName();
			file=new File(Main.logDir+next+"-operation.log");
			while (true) {
				Thread.sleep(30000);
				if(worksFlag){
					if(works1.size()>0){
						writerData();
					}
				}else{
					if(works2.size()>0){
						writerData();
					}
				}
			}
		} catch (Exception e) {
			String stackTrace = Util.getStackTrace(e);
			Main.log.error(stackTrace);
		}
	}
	private void writerData() throws Exception{
		BufferedWriter bw=new BufferedWriter(new FileWriter(file,true));
		worksFlag=!worksFlag;
		if(!worksFlag){
			Iterator<Messige> iterator = works1.iterator();
			while(iterator.hasNext()){
				Messige msg = iterator.next();
				bw.append(msg.toString());
				bw.flush();
			}
			works1.clear();
			works1=null;
			iterator=null;
			works1=new ArrayList<Messige>();
		}else{
			Iterator<Messige> iterator = works2.iterator();
			while(iterator.hasNext()){
				Messige msg = iterator.next();
				bw.append(msg.toString());
				bw.flush();
			}
			works2.clear();
			works2=null;
			works2=new ArrayList<Messige>();
		}
		bw.close();
		if(file.length()>1024000){
			next++;
			file=new File(Main.logDir+next+"-operation.log");
		}
	}
	private void nextFileName() {
		File file = new File(Main.logDir);
		if(!file.exists()){
			file.mkdirs();
		}
		Integer count = 0;
		String[] list = file.list();
		if(list.length>0){
			for (String string : list) {
				if(string.indexOf("-")!=-1){
					String[] split = string.split("-");
					Integer valueOf = Integer.valueOf(split[0]);
					if (valueOf > count) {
						count = valueOf;
					}
				}
			}
		}
		next = ++count;
	}
	public  void addMessige(Messige msg){
		if(worksFlag){
			works1.add(msg);
		}else{
			works2.add(msg);
		}
		
	}
}
