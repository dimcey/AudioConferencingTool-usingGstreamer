package Middleware;

public interface EventListener {

    /**
     * This method fire an event locally.
     * 
     * @param message is the code of the message.
     * @param o is the object to fire.
     */
    void fireEvent(int message, Object o);
}
