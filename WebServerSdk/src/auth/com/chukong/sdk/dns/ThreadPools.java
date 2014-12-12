package com.chukong.sdk.dns;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yihua.huang@dianping.com
 * @date Apr 11, 2013
 */

public class ThreadPools {

	private ThreadPoolExecutor mainProcessExecutor;
	private ThreadPoolExecutor udpReceiverExecutor;
	private int threadNum = 0;
	
	private Configure configure = new Configure();

	public void resize() {
		if (threadNum != configure.getThreadNum()) {
			threadNum = configure.getThreadNum();
			System.out.println("Thread num changed, resize to " + threadNum);
			if (threadNum < configure.getThreadNum()) {
				mainProcessExecutor.setMaximumPoolSize(threadNum);
				mainProcessExecutor.setCorePoolSize(threadNum);
				udpReceiverExecutor.setMaximumPoolSize(threadNum);
				udpReceiverExecutor.setCorePoolSize(threadNum);
			} else {
				mainProcessExecutor.setCorePoolSize(threadNum);
				mainProcessExecutor.setMaximumPoolSize(threadNum);
				udpReceiverExecutor.setCorePoolSize(threadNum);
				udpReceiverExecutor.setMaximumPoolSize(threadNum);
			}
		}
	}

	public ExecutorService getMainProcessExecutor() {
		return mainProcessExecutor;
	}

	public ExecutorService getUdpReceiverExecutor() {
		return udpReceiverExecutor;
	}

	public ThreadPools() {
		threadNum = configure.getThreadNum();
		mainProcessExecutor = new ThreadPoolExecutor(threadNum, threadNum, 0L,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		udpReceiverExecutor = new ThreadPoolExecutor(threadNum, threadNum, 0L,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
}
