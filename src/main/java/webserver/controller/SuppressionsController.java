package webserver.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SuppressionsController {
	protected static final Logger LOGGER = LoggerFactory.getLogger(SuppressionsController.class);
	private static final String SUPP_XML_FILENAME = "publishedSuppressions.xml";
	private static final String ENDPOINT = "/suppressions.xml";

	@GetMapping(
			value={"/unauthenticated" + ENDPOINT}, 
			produces=MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getSuppressions(@RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> auth) {
		LOGGER.info("Reading suppression (unauthenticated)");
		displayAuthHeader("unauthenticated", auth);
        return flushSuppressions();
    }
	
	@GetMapping(
			value={"/bearer" + ENDPOINT}, 
			produces=MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getSuppressionsBearer(@RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> auth) {
		return checkCredentialsAndflush("Bearer", auth);
    }
	
	@GetMapping(
			value={"/basic" + ENDPOINT}, 
			produces=MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getSuppressionsBasic(@RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> auth) {
		return checkCredentialsAndflush("Basic", auth);
    }
	
	@GetMapping(
			value={"/basic302" + ENDPOINT}, 
			produces=MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getSuppressionsBasic302(@RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> auth) {
		LOGGER.info("Reading suppression Basic 302 - Non Preemptive");
		boolean authProvided = displayAuthHeader("Basic 302", auth);
		if (!authProvided)
			return response(toXmlError(new IOException("Authorization header not provided")),
					getHttpHeaders("WWW-Authenticate", "Basic realm=\"hosted suppressions\""), HttpStatus.FOUND);
		return checkCredentialsAndflush("Basic", auth);
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private ResponseEntity<String> checkCredentialsAndflush(String auth, Optional<String> auth2) {
		LOGGER.info("Reading suppression " + auth +" - Non Preemptive");
		boolean authProvided = displayAuthHeader(auth, auth2);
		if (!authProvided)
			return response(toXmlError(new IOException("Authorization header not provided")),
					getHttpHeaders("WWW-Authenticate", auth + " realm=\"hosted suppressions\""), HttpStatus.UNAUTHORIZED);
		
		return flushSuppressions();
	}

	private ResponseEntity<String> flushSuppressions() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(SUPP_XML_FILENAME);
		if(is == null)
            return response(toXmlError(new IOException(SUPP_XML_FILENAME+" no found")), getHttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
			
		StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
        		new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            LOGGER.error("Error reading suppression: {}", e.getMessage());
            return new ResponseEntity<>(toXmlError(e), getHttpHeaders(), HttpStatus.BAD_REQUEST);
        }
        return response(sb.toString(), getHttpHeaders(), HttpStatus.OK);
	}

	private ResponseEntity<String> response(String data, HttpHeaders httpHeaders, HttpStatus status) {
		LOGGER.info("   > HTTP-{}", status);
		httpHeaders.forEach((k,v) -> LOGGER.info("   > {}: {}", k, v));
        return new ResponseEntity<>(data, httpHeaders, status);
	}

	private HttpHeaders getHttpHeaders(String key, String value) {
		final HttpHeaders httpHeaders= getHttpHeaders();
	    httpHeaders.add(key, value);
	    return httpHeaders;
	}

	private String toXmlError(Exception e) {
		return String.format("<error class='%s'>%s</error>", e.getClass().getSimpleName(), e.getMessage());
	}

	public static HttpHeaders getHttpHeaders() {
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_XML);
	    return httpHeaders;
	}
	
	private boolean displayAuthHeader(String label, Optional<String> auth) {
		if (!auth.isPresent()) {
			LOGGER.info("< No {} header provided for {}", HttpHeaders.AUTHORIZATION, label);
			return false;
		}
		String value = auth.get();
		if (value.isEmpty()) {
			LOGGER.info("< Empty {} header provided for {}", HttpHeaders.AUTHORIZATION, label);
			return false;
		}
		LOGGER.info("< {}: {}", HttpHeaders.AUTHORIZATION, value);
		return true;		
	}

}
