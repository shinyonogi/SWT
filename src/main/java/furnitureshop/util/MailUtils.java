package furnitureshop.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

public class MailUtils {

	// Static fields to access mail server
	private static final String
			TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token",
			EMAIL_ADDRESS = "praxis.ecg2020@gmail.com",

	OAUTHCLIENT_ID = "909218273569-2s7og2iu3acc9tfjic6ai4ab7t5ulngb.apps.googleusercontent.com",
			OAUTH_SECRET = "VM4NeqolYWvboiC5aOHTaaVg",
			REFRESH_TOKEN = "1//09tMqvPNkXADbCgYIARAAGAkSNwF-L9IrPpjLKmwJtDRtHzctPr5qUoUH" +
					"-H5HqXjEu8OgQi0hvBEScUHYyBgA1m_tvW3H4jjeQcU";

	// Dynamic fields which can change if token get invalid over time
	private static String ACCESS_TOKEN = "ya29.a0AfH6SMCGdM0Z6O6Srghiec1sEDVfvhRt7zwaTNzqhRxytoo6EQbMkVZTRX8ayhPZqqY" +
			"MsQFsrcqxE0nRMiNB2T869uJaVYEm7cGr5dChKp7GY-NACOY7spuayrhTc9USAf0yBCJjdB0_uoyAsrcG7egBowDrVAwqNtA5DrQAl9U";
	private static long TOKEN_EXPIRES = 0L;

	/**
	 * Sends an E-Mail to the {@code target} with a {@code subject} and a specific {@code message}
	 *
	 * @param subject The subject of the E-Mail
	 * @param target  The target E-Mail
	 * @param message The content of the E-Mail
	 *
	 * @return {@code true} if the E-Mail was successfully send
	 */
	public static boolean sendMail(String subject, String target, String message) {
		renewToken();

		final JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setUsername(EMAIL_ADDRESS);
		sender.setPassword(ACCESS_TOKEN);
		sender.setHost("smtp.gmail.com");
		sender.setPort(587);

		final Properties properties = System.getProperties();

		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.auth.mechanisms", "XOAUTH2");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.debug", "false");

		sender.setJavaMailProperties(properties);

		try {
			final MimeMessage msg = sender.createMimeMessage();

			msg.setFrom(EMAIL_ADDRESS);
			msg.setRecipients(RecipientType.TO, target);
			msg.setSubject(subject);
			msg.setText(message);

			sender.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * This helper Method checks and renews the access token for sending E-Mails
	 */
	private static void renewToken() {
		if (System.currentTimeMillis() <= TOKEN_EXPIRES) {
			return;
		}

		final String request = "client_id=" + URLEncoder.encode(OAUTHCLIENT_ID, StandardCharsets.UTF_8)
				+ "&client_secret=" + URLEncoder.encode(OAUTH_SECRET, StandardCharsets.UTF_8)
				+ "&refresh_token=" + URLEncoder.encode(REFRESH_TOKEN, StandardCharsets.UTF_8)
				+ "&grant_type=refresh_token";

		try {
			final HttpURLConnection con = (HttpURLConnection) new URL(TOKEN_URL).openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");

			try (PrintWriter out = new PrintWriter(con.getOutputStream())) {
				out.print(request);
			}

			con.connect();

			final HashMap<String, Object> result = new ObjectMapper().readValue(
					con.getInputStream(), new TypeReference<>() {}
			);

			ACCESS_TOKEN = (String) result.get("access_token");
			TOKEN_EXPIRES = System.currentTimeMillis() + (((Number) result.get("expires_in")).intValue() * 1000L);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
