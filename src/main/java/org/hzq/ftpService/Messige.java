package org.hzq.ftpService;

public class Messige {
	private String userName;
	private String date;
	private String state;
	private String messige;
	private String level;
	private String addres;
	public Messige(String userName,String date,String state,String messige,String level,String addres){
		this.date=date;
		this.level=level;
		this.state=state;
		this.userName=userName;
		this.date=date;
		this.messige=messige;
		this.addres=addres;
	}
	
	public String getAddres() {
		return addres;
	}

	public void setAddres(String addres) {
		this.addres = addres;
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getMessige() {
		return messige;
	}
	public void setMessige(String messige) {
		this.messige = messige;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return date.toString()+"    --user:"+userName+"  --addres:"+addres+"    --state:"+state+"    --msg:"+messige+"    --level:"+level+"\r\n";
	}
	
}
