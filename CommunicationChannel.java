/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CommunicationChannel {
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
    LinkedBlockingQueue<Message> queue_miners = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<Message> queue_wizzards = new LinkedBlockingQueue<>();
	private final ReentrantLock lock_wizz = new ReentrantLock();
    private final ReentrantLock lock_miner = new ReentrantLock();
	Message firstMessage = null;
	public CommunicationChannel() {
	}

	/**
	 * Puts a message on the miner channel (i.e., where miners write to and wizards
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {

		try {
			queue_miners.put(message);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
		Message head = null;
		try {
			head = queue_miners.take();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return head;
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write to and miners
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageWizardChannel(Message message) {
        lock_wizz.lock();
	    if(message.getData().equals("END") || message.getData().equals("EXIT"))
        {
            try {
                queue_wizzards.put(message);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock_wizz.unlock();
            }
        }
        else {
            try {
                queue_wizzards.put(message);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (lock_wizz.getHoldCount() == 2) {
                    lock_wizz.unlock();
                    lock_wizz.unlock();
                }
            }
        }
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {
		Message head = null;
		lock_miner.lock();
		try {
			head = queue_wizzards.take();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if(head.getData().equals("EXIT") || head.getData().equals("END"))
        {
            lock_miner.unlock();
        }
        else {
            if(lock_miner.getHoldCount() == 2)
            {
                lock_miner.unlock();
                lock_miner.unlock();
            }
        }
		return head;
	}
}
