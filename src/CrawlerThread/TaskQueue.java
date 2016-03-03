package CrawlerThread;

import Entities.CrawlTask;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by Khanh Nguyen on 3/3/2016.
 */
public class TaskQueue {

    LinkedList evenQueue;
    LinkedList oddQueue;
    Set gatheredLinks;
    Set processedLinks;
    int maxElements;
    String filenamePrefix;

    public TaskQueue() {
        evenQueue = new LinkedList();
        oddQueue = new LinkedList();
        gatheredLinks = new HashSet();
        processedLinks = new HashSet();
        maxElements = -1;
        filenamePrefix = "";
    }

    public TaskQueue(int _maxElements, String _filenamePrefix) {
        evenQueue = new LinkedList();
        oddQueue = new LinkedList();
        gatheredLinks = new HashSet();
        processedLinks = new HashSet();
        maxElements = _maxElements;
        filenamePrefix = _filenamePrefix;
    }

    public void setFilenamePrefix(String _filenamePrefix) {
        filenamePrefix = _filenamePrefix;
    }

    public String getFilenamePrefix() {
        return filenamePrefix;
    }

    public void setMaxElements(int _maxElements) {
        maxElements = _maxElements;
    }

    public Set getGatheredElements() {
        return gatheredLinks;
    }

    public Set getProcessedElements() {
        return processedLinks;
    }

    public int getQueueSize(int level) {
        if (level % 2 == 0) {
            return evenQueue.size();
        } else {
            return oddQueue.size();
        }
    }

    public int getProcessedSize() {
        return processedLinks.size();
    }

    public int getGatheredSize() {
        return gatheredLinks.size();
    }

    /**
     * Return and remove the first element from the appropriate queue
     * Note that the return type of this method is Object for compliance
     * with interface Queue.
     */
    public synchronized CrawlTask pop(int level) {
        CrawlTask task;
        // try to get element from the appropriate queue
        // is the queue is empty, return null
        if (level % 2 == 0) {
            if (evenQueue.size() == 0) {
                return null;
            } else {
                task = (CrawlTask) evenQueue.removeFirst();
            }
        } else {
            if (oddQueue.size() == 0) {
                return null;
            } else {
                task = (CrawlTask) oddQueue.removeFirst();
            }
        }
        // convert the string to a url and add to the set of processed links
        try {
            processedLinks.add(task);
            return task;
        } catch (Exception e) {
            // shouldn't happen, as only URLs can be pushed
            System.out.println("EXCEPTION: taskQueue.pop");
            return null;
        }
    }

    /**
     * Add an element at the end of the appropriate queue
     * Note that the type of argument url is Object for compliance with
     * interface Queue.
     */
    public synchronized boolean push(CrawlTask task, int level) {
        // don't allow more than maxElements links to be gathered
        if (maxElements != -1 && maxElements <= gatheredLinks.size())
            return false;

        if (gatheredLinks.add(task)) {
            // has not been in set yet, so add to the appropriate queue
            if (level % 2 == 0) {
                evenQueue.addLast(task);
            } else {
                oddQueue.addLast(task);
            }
            return true;
        } else {
            // this link has already been gathered
            return false;
        }
    }

    /**
     * Clear both queues
     * The sets of gathered and processed Elements are not affected.
     */
    public synchronized void clear() {
        evenQueue.clear();
        oddQueue.clear();
    }
}
