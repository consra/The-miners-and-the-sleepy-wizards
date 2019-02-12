import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for a miner.
 */
public class Miner extends Thread {
	/**
	 * Creates a {@code Miner} object.
	 * 
	 * @param hashCount
	 *            number of times that a miner repeats the hash operation when
	 *            solving a puzzle.
	 * @param solved
	 *            set containing the IDs of the solved rooms
	 * @param channel
	 *            communication channel between the miners and the wizards
	 */
    private CommunicationChannel channel;
    private static Set<Integer> solved;
    Integer hashCount;
	public Miner(Integer hashCount, Set<Integer> solved, CommunicationChannel channel) {
	    this.channel = channel;
	    this.solved = solved;
	    this.hashCount = hashCount;
	}

	@Override
	public void run() {
		while(true) {
			Message parentMessage = this.channel.getMessageWizardChannel();
            if (parentMessage.getData().equals("EXIT")) {
                return;
            }

            if(parentMessage.getData().equals("END")) {
            	continue;
			}

			Message currentRoomMessage = this.channel.getMessageWizardChannel();

			if(solved.contains(currentRoomMessage.getCurrentRoom()))
				continue;
			else {
				solved.add(currentRoomMessage.getCurrentRoom());
				Message toSend = new Message(parentMessage.getCurrentRoom(), currentRoomMessage.getCurrentRoom(),
						encryptMultipleTimes(currentRoomMessage.getData(), this.hashCount));

				this.channel.putMessageMinerChannel(toSend);
			}
		}
	}

	private static String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
