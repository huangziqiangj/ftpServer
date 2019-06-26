package org.hzq.ftpService;

import java.util.List;

public class User {
	private String username;
	private String password;
	private String []  permission;
	private List<String> workDir;
	private int state;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String[] getPermission() {
		return permission;
	}
	public void setPermission(String[] permission) {
		this.permission = permission;
	}
	public List<String> getWorkDir() {
		return workDir;
	}
	public void setWorkDir(List<String> workDir) {
		this.workDir = workDir;
	}
	
}
