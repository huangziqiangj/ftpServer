package org.hzq.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hzq.ftpService.Main;
import org.hzq.ftpService.Messige;
import org.hzq.ftpService.Util;

public class BlastInspect {
	private Map<String,Integer> record=new HashMap<String,Integer>();
	private List<BlastUser>  blasUsers=new ArrayList<BlastUser>();
	public BlastInspect(){
		main();
	}
	private void main() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Set<String> keySet = record.keySet();
					Iterator<String> iterator = keySet.iterator();
					while(iterator.hasNext()){
						String next = iterator.next();
						Integer integer = record.get(next);
						integer-=60;
						if(integer<=0){
							record.remove(next);
						}else{
							record.put(next, integer);
						}
					}
					
				}
			}
		}).start();
	}
	public void addBlackUsers(String address){
		boolean flag=true;
		for (int i = 0; i < blasUsers.size(); i++) {
			BlastUser blastUser = blasUsers.get(i);
			if(blastUser.getAddress().equals(address)){
				blastUser.setCount();
				flag=false;
			}
		}
		if(flag){
			blasUsers.add(new BlastUser(address));
		}
	}
	public boolean addRecord(String address){
		Main.myLog.addMessige(new Messige("Attacker", Util.getNowDate(),"9", "Brute force password", "9", address));
		record.put(address, 86400);
		return false;
	}
	public boolean isBlackList(String address){
		Integer integer = record.get(address);
		return integer!=null;
	}
	public void removeBlastUser(String address){
		Iterator<BlastUser> iterator = blasUsers.iterator();
		while(iterator.hasNext()){
			BlastUser next = iterator.next();
			if(next.getAddress().equals(address)){
				iterator.remove();
				break;
			}
		}
	}
	
}
