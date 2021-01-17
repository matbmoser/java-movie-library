package org.dis.practica2.grupo6.backend;

public class VDException extends Exception{
        public VDException() { super(); }
        public VDException(String message) { super(message); }
        public VDException(String message, Throwable cause) { super(message, cause); }
        public VDException(Throwable cause) { super(cause); }
}
