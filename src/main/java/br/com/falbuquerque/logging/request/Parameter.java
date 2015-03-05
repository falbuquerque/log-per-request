package br.com.falbuquerque.logging.request;

import com.google.gson.annotations.Expose;

/**
 * Represents a parameter of a request.
 * 
 * @author Felipe Albuquerque
 */
public class Parameter {

    @Expose
    private final String name;

    @Expose
    private final Object value;

    /**
     * Builds a parameter.
     * 
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public Parameter(final String name, final Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the name of the parameter.
     * 
     * @return the name of the parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the parameter.
     * 
     * @return the value of the parameter
     */
    public Object getValue() {
        return value;
    }

}
