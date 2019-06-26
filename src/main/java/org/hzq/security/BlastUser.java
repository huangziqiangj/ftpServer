package org.hzq.security;

import org.hzq.ftpService.Main;

public class BlastUser {
	private String address;
	private Integer count;
	private int time;
	
	public BlastUser(String address){
		this.address=address;
		this.time=30;
		this.count=0;
		main();
	}
	private void main() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean flag=true;
				while(flag){
					time-=1;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(count>=5){
						Main.blastInspect.addRecord(address);
						Main.blastInspect.removeBlastUser(address);
						flag=false;
					}
					if(time<=0){
						Main.blastInspect.removeBlastUser(address);
						flag=false;
					}
				}
				
			}
		}).start();
	}
	public boolean addRecord(String address){
		
		return false;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount() {
		count++;
		this.time=30;
	}
	
}
