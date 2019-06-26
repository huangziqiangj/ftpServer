package org.hzq.ftpService;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Util {
	public static String path;

	/**
	 * dom4j解析xml初始化数据
	 * 
	 * @param fileName
	 * @return
	 */
	public static Object initDataFromXML(String fileName) {
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(Main.runTimePath + "/bin/" + fileName));
			Element rootElement = document.getRootElement();
			// 获取整个xml的所有节点
			Iterator<Element> iterator = rootElement.elementIterator();
			while (iterator.hasNext()) {
				Element next = (Element) iterator.next();
				String name = next.getName();
				// 根据节点的名称决定是直接获取数据还是继续创建子节点的迭代器,获取每个节点的id,name等等属性可以使用attributes()方法
				if(name.equals("serverAddr")){
					String addr = next.getText();
					Main.initPASV(addr);
				}else if (name.equals("rootDir")) {
					Main.rootDir = next.getText();
				} else if (name.equals("users")) {
					Iterator<Element> users = next.elementIterator();
					while (users.hasNext()) {
						User u = new User();
						Element user = users.next();
						Iterator<Element> userDatas = user.elementIterator();
						while (userDatas.hasNext()) {
							Element userData = userDatas.next();
							String userDataName = userData.getName();
							if (userDataName.equals("username")) {
								u.setUsername(userData.getText());
							} else if (userDataName.equals("password")) {
								u.setPassword(userData.getText());
							} else if (userDataName.equals("workDirs")) {
								Iterator<Element> workDirs = userData.elementIterator();
								List<String> list = new ArrayList<String>();
								while (workDirs.hasNext()) {
									Element workDir = workDirs.next();
									list.add(workDir.getText());
								}
								u.setWorkDir(list);
							} else if (userDataName.equals("permission")) {
								String permissions = userData.getText();
								u.setPermission(permissions.split("-"));
							} else if (userDataName.equals("loginState")) {
								u.setState(Integer.valueOf(userData.getText()));
							}
						}
						Main.users.add(u);
					}
				} else if (name.equals("initDir")) {
					Iterator<Element> initDir = next.elementIterator();
					boolean auto=false;
					String dirs="";
					while(initDir.hasNext()){
						Element next2 = initDir.next();
						String name2 = next2.getName();
						if(name2.equals("autoCreate")){
							auto=next2.getText().equals("true");
						}else if(name2.equals("dirs")){
							dirs=next2.getText();
						}
					}
					if(auto){
						createDir(dirs);
					}
				} else if (name.equals("logDir")) {
					Main.logDir = next.getText();
				}else if(name.equals("logPrint")){
					Main.logPrint="true".equals(next.getText().trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	/**
	 * 检查ftp的所有目录是否存在,不存在的话直接创建
	 * 
	 * @param text
	 */
	private static void createDir(String text) {
		String[] split = text.split("-");
		for (String string : split) {
			File file = new File(Main.rootDir + "/" + string);
			if (!file.exists() || !file.isDirectory()) {
				file.mkdir();
			}
		}

	}

	/**
	 * 获取完整异常信息
	 *
	 * @dagewang 2017年10月30日
	 */
	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		try {
			t.printStackTrace(pw);
			return sw.toString();
		} finally {
			pw.close();
		}
	}

	public static User findUser(String username) {
		for (User user : Main.users) {
			if (user.getUsername().equals(username)) {
				return user;
			}
		}
		return null;
	}

	public static String getNowDate() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return simpleDateFormat.format(new Date());
	}
}
