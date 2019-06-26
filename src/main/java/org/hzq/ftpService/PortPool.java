package org.hzq.ftpService;

import java.util.HashMap;

public class PortPool {
	static int min_port = 29301;
	static int max_port = 39301;
	public static volatile int now_port = 29301;
	private int count = 85;
	private volatile HashMap<String, Boolean> ports;

	public PortPool() {
		ports = new HashMap<String, Boolean>();
		for (int i = 35; i < count; i++) {
			for (int j = 1; j < 255; j++) {
				int p = i * 256 + j;
				ports.put(p + "," + i + "," + j, false);
			}
		}
	};

	private static PortPool portPool;

	public static PortPool getPortPool() {
		if (portPool == null) {
			synchronized (PortPool.class) {
				if (portPool == null) {
					portPool = new PortPool();
				}
			}
		}
		return portPool;
	}

	public String getPort() {
		while (true) {
			for (int i = 35; i < count; i++) {
				for (int j = 1; j < 255; j++) {
					int p = i * 256 + j;
					String key = p + "," + i + "," + j;
					if (!ports.get(key)) {
						synchronized (PortPool.class) {
							if (!ports.get(key)) {
								ports.put(key, true);
								return key;
							}
						}
					}
				}
			}
		}
	}

	public void release(String key) {
		synchronized (PortPool.class) {
			ports.put(key, false);
		}
	}
}
