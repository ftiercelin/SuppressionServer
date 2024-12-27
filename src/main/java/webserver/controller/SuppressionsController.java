package webserver.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SuppressionsController {
	protected static final Logger LOGGER = LoggerFactory.getLogger(SuppressionsController.class);
	private static final String SUPP_XML_FILENAME = "publishedSuppressions.xml";
	private static final String ENDPOINT = "/suppressions.xml";

	@GetMapping(
			value={"/unauthenticated" + ENDPOINT}, 
			produces=MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getSuppressions(
			@RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> auth,
			@RequestHeader Map<String, String> headers,
			@RequestParam  Optional<String> full
			) {
		String id = "unauthenticated";
		if(full.isPresent())
			displayHeaders(id, headers);
		else 
			displayAuthHeader(id, auth);
        return flushSuppressions();
    }
	

	@GetMapping(
			value={"/bearer" + ENDPOINT}, 
			produces=MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getSuppressionsBearer(
			@RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> auth,
			@RequestHeader Map<String, String> headers,
			@RequestParam  Optional<String> full
			) {
		String id = "Bearer";
		if(full.isPresent())
			displayHeaders(id, headers);
		else 
			displayAuthHeader(id, auth);
		return checkCredentialsAndflush(HttpStatus.UNAUTHORIZED, "Bearer", auth);
    }
	
	@GetMapping(
			value={"/basic" + ENDPOINT}, 
			produces=MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getSuppressionsBasic(
			@RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> auth,
			@RequestHeader Map<String, String> headers,
			@RequestParam  Optional<String> full
			) {
		String id = "Basic";
		if(full.isPresent())
			displayHeaders(id, headers);
		else 
			displayAuthHeader(id, auth);
		return checkCredentialsAndflush(HttpStatus.UNAUTHORIZED, "Basic", auth);
    }
	
	@GetMapping(
			value={"/basic302" + ENDPOINT}, 
			produces=MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getSuppressionsBasic302(
			@RequestHeader(HttpHeaders.AUTHORIZATION) Optional<String> auth,
			@RequestHeader Map<String, String> headers,
			@RequestParam  Optional<String> full
			) {
		String id = "Basic302";
		if(full.isPresent())
			displayHeaders(id, headers);
		else 
			displayAuthHeader(id, auth);
		return checkCredentialsAndflush(HttpStatus.FOUND, "Basic", auth);
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private ResponseEntity<String> checkCredentialsAndflush(HttpStatus status, String authType, Optional<String> auth) {
		if (!isAuth(authType, auth)) {
			return response(toXmlError(new IOException("Authorization header not provided")),
					getHttpHeaders("WWW-Authenticate", authType + " realm=\"hosted suppressions\""), status);
		}
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
	
	private void displayHeaders(String id, Map<String, String> headers) {
		LOGGER.info("Headers for {}", id);
		headers.forEach((k, v) -> LOGGER.info("   <-- {}: {}", k, v));
	}
	private ResponseEntity<String> response(String data, HttpHeaders httpHeaders, HttpStatus status) {
		LOGGER.info("   --> HTTP-{}", status);
		for( String key : httpHeaders.keySet()) {
			httpHeaders.get(key).forEach(value -> LOGGER.info("   --> {}: {}", key, value));
		}
        return new ResponseEntity<>(data, httpHeaders, status);
	}

	private boolean displayAuthHeader(String label, Optional<String> auth) {
		if (!auth.isPresent()) {
			LOGGER.info("   <-- No {} header provided for {}", HttpHeaders.AUTHORIZATION, label);
			return false;
		}
		String value = auth.get();
		if (value.isEmpty()) {
			LOGGER.info("   <-- Empty {} header provided for {}", HttpHeaders.AUTHORIZATION, label);
			return false;
		}
		LOGGER.info("   <-- {}: {}", HttpHeaders.AUTHORIZATION, value);
		return true;		
	}
	private boolean isAuth(String authType, Optional<String> auth) {
		if (!auth.isPresent()) {
			return false;
		}
		String value = auth.get();
		if (value.isEmpty()) {
			return false;
		}
		return value.toLowerCase().startsWith(authType.toLowerCase());		
	}

}
