package com.dynatrace.easytravel.launcher.engine;



/**
 * Represents a three-state processing feedback.
 * 
 * @author martin.wurzinger
 */
public enum Feedback {

    /** if we are sure that everything went well */
    Success(true),

    /** if we are not sure that everything went well but on the other side no problem was detected */
    Neutral(true),

    /** if an error was detected */
    Failure(false);

    private final boolean isOk;

    private Feedback(boolean isOk) {
        this.isOk = isOk;
    }

    /**
     * Check if problems were detected while processing.
     * 
     * @return <code>true</code> if no problems occurred or <code>false</code> otherwise
     * @author martin.wurzinger
     */
    public boolean isOk() {
        return isOk;
    }

    
    /**
     * Return the highest severity of the provided feedback-items.
     * 
     * @param feedback1
     * @param feedback2
     * 
     * @return The Feedback with the highest severity. 
     */
    public static Feedback getMostSevere(Feedback feedback1, Feedback feedback2) {
    	if(Feedback.Failure.equals(feedback1) || Feedback.Failure.equals(feedback2)) {
    		return Feedback.Failure;
    	}

    	if(Feedback.Neutral.equals(feedback1) || Feedback.Neutral.equals(feedback2)) {
    		return Feedback.Neutral;
    	}
    	
    	return Feedback.Success;
    }
}
