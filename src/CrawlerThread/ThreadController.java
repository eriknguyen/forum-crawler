package CrawlerThread;

/**
 *
 * This code uses reference by Andreas Hess <andreas.hess@ucd.ie> in the public domain.
 * Link: http://andreas-hess.info/programming/webcrawler/
 *
 * 
 */

public class ThreadController {

	int currentLevel;
	TaskQueue tasks;
	MainCrawler receiver;
	int threadId;
	ConnectionManager connectionManager;

	/**
	 * maximum depth level allowed
	 * -1 if be unlimited
	 */
	int maxLevel;

	/**
	 * maximum number of parallel threads
	 * -1 if unlimited
	 */
	int maxThreads;

	/**
	 * The class of the threads created by this ThreadController
	 */
	Class threadClass;

	/**
	 * A unique synchronized counter
	 */
	int syncCounter;

	/**
	 * Constructor that intializes the instance variables
	 * The queue may already contain some tasks.
	 * If _maxThreads > 0, _maxThreads threads are started immediately.
	 * If _tasks.size(_level) > _maxThreads == -1, then only
	 * _tasks.size(_level) threads are started. Note that this includes
	 * the case where _maxThreads == -1, therefore even if the number of
	 * allowed threads is unlimited, only a finite number of threads are
	 * started.
	 */
	public ThreadController(Class _threadClass,
							ConnectionManager connManager,
							int _maxThreads,
							int _maxLevel,
							TaskQueue _tasks,
							int _level,
							MainCrawler _receiver)
		throws InstantiationException, IllegalAccessException {
		threadClass = _threadClass;
		connectionManager = connManager;
		maxThreads = _maxThreads;
		maxLevel = _maxLevel;
		tasks = _tasks;
		currentLevel = _level;
		receiver = _receiver;
		syncCounter = 0;
		threadId = 0;
		startThreads();
	}

	/**
	 * Get a unique number from a counter
	 */
	public synchronized int getUniqueNumber() {
		return syncCounter++;
	}

	/**
	 * Adjust number of allowed threads and start new threads if possible
	 */
	public synchronized void setMaxThreads(int _maxThreads)
		throws InstantiationException, IllegalAccessException {
		maxThreads = _maxThreads;
		startThreads();
	}

	/**
	 * Get number of maximum allowed threads
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * Get number of maximum level
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * Get number of currently running threads
	 */
	public int getRunningThreads() {
		return threadId;
	}

	/**
	 * Called by a thread to tell the controller that it is about to stop.
	 * The threadId is handed over to the MessageReceiver.
	 * If this was the last running thread it means that one level of the
	 * queue has been completed. In this case, increment the level (if
	 * allowed) and start new threads.
	 */
	public synchronized void finished(int threadId) {
		this.threadId--;
		receiver.finished(threadId);
		if (this.threadId == 0) {
			currentLevel++;
			if (currentLevel > maxLevel) {
				receiver.finishedAll();
				return;
			}
			// debug
			// System.err.println("new level " + level);
			// if no tasks in queue we're don
			if (tasks.getQueueSize(currentLevel) == 0) {
				receiver.finishedAll();
				return;
			}
			try {
				System.out.println("GOING TO NEXT LEVEL: " + currentLevel);
				startThreads();
			} catch (InstantiationException e) {
				// Something has gone wrong on the way, because if it hadn't
				// worked at all we wouldn't be here. Anyway, we can do
				// nothing about it, so we just quit instead of moving to
				// a new level.
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// Something has gone wrong on the way, because if it hadn't
				// worked at all we wouldn't be here. Anyway, we can do
				// nothing about it, so we just quit instead of moving to
				// a new level.
				e.printStackTrace();
			}
		}
	}

	/**
	 * Start the maximum number of allowed threads
	 */
	public synchronized void startThreads() throws InstantiationException, IllegalAccessException {
		// Start m threads
		// For more information on where m comes from see comment on
		// the constructor.
		int m = maxThreads - threadId;
		int ts = tasks.getQueueSize(currentLevel);
		if (ts < m || maxThreads == -1) {
			m = ts;
		}
		// debug
		// System.err.println(m + " " + maxThreads + " " + threadId + " " + ts);
		// Create some threads
		for (int n = 0; n < m; n++) {
			CrawlerThread thread =
				(CrawlerThread) threadClass.newInstance();
			thread.setThreadController(this);
			thread.setConnectionManager(this.connectionManager);
			thread.setMessageReceiver(receiver);
			thread.setLevel(currentLevel);
			thread.setQueue(tasks);
			thread.setId(threadId++);
			thread.start();
		}
	}
}
