package br.com.falbuquerque.logging.request;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.gson.annotations.Expose;

/**
 * Request to be logged.
 * 
 * @author Felipe Albuquerque
 */
public class Request {

    @Expose
    private final String token;

    @Expose
    private final Collection<Parameter> parameters;

    /**
     * Builds a request.
     * 
     * @param token
     *            the token of the request
     * @param parameters
     *            the parameters of the request
     */
    public Request(final String token, final Parameter... parameters) {
        this.token = token;
        this.parameters = Arrays.asList(parameters);
    }

    /**
     * Gets the token of the request.
     * 
     * @return the token of the request
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the parameters of the request.
     * 
     * @return the parameters of the request
     */
    public Collection<Parameter> getParameters() {
        return Collections.unmodifiableCollection(parameters);
    }

}
