package org.fogbowcloud.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fogbowcloud.blowout.core.model.Task;

public abstract class Job implements Serializable {

	private static final long serialVersionUID = -6111900503095749695L;

	protected Map<String, Task> taskList = new HashMap<>();
	
	public enum TaskState{
		
		READY("READY"),RUNNING("RUNNING"),COMPLETED("COMPLETED"),FAILED("FAILED"),NOT_CREATED("NOT CREATED");
		
		private String value;
		
		TaskState(String value){
			this.value = value;
		}
		
		public String getValue(){
			return this.value;
		}
	}
	
	public static final Logger LOGGER = LoggerFactory.getLogger(Job.class);
	
	private ReentrantReadWriteLock taskReadyLock = new ReentrantReadWriteLock();

	private boolean isCreated = false;

	public Job(List<Task> tasks) {
		for(Task task : tasks){
			addTask(task);
		}
	}

	public Job() {

	}

	//TODO: not sure that we need to guarantee thread safety at the job level
	public void addTask(Task task) {
		LOGGER.debug("Adding task " + task.getId());
		taskReadyLock.writeLock().lock();
		try {
			getTaskList().put(task.getId(), task);
		} finally {
			taskReadyLock.writeLock().unlock();
		}
	}
	
	public List<Task> getTasks(){
		return new ArrayList<>(taskList.values());
	}
	
	public abstract void finish(Task task);

	public abstract void fail(Task task);

	public abstract String getId();

	//TODO: it seems this *created* and restart methods help the Scheduler class to its job. I'm not sure
	//if we should keep them.
	public boolean isCreated() {
		return this.isCreated;
	}
	
	public void setCreated() {
		this.isCreated = true;
	}

	public void restart() {
		this.isCreated = false;
	}

	public Map<String, Task> getTaskList() {
		return taskList;
	}

	//FIXME: why do we need this method? (serialization?)
	public void setTaskList(Map<String, Task> taskList) {
		this.taskList = taskList;
	}
}
