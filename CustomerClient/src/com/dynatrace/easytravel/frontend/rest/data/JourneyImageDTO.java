package com.dynatrace.easytravel.frontend.rest.data;

import java.util.Random;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class JourneyImageDTO {
	
	private static final String IMAGE_PATH = "images/";
	private static final int MIN_PICTURE_NUMBER = 1;
	private static final int MAX_PICTURE_NUMBER = 22;
	
	private int previousId;
	private int currentId;
	private int nextId;
	
	public JourneyImageDTO() {
		this.currentId = randomImage();
		this.nextId = randomImage(this.currentId);
		this.previousId = randomImage(this.currentId);
	}

	public String getCurrentPath() {
		return idToImagePath(currentId);
	}

	public void setCurrentPath(String imagePath) {
		this.currentId = imagePathToId(imagePath);
	}
	
	public String getPreviousPath() {
		return idToImagePath(previousId);
	}

	public void setPreviousPath(String imagePath) {
		this.previousId = imagePathToId(imagePath);
	}
	
	public String getNextPath() {
		return idToImagePath(nextId);
	}

	public void setNextPath(String imagePath) {
		this.nextId = imagePathToId(imagePath);
	}
	
	private static String idToImagePath(int id) {
		return  IMAGE_PATH + Integer.toString(id) + ".jpg";
	}
	
	private static int imagePathToId(String imagePath) {
		return Integer.parseInt(imagePath.replaceAll("\\D", ""));
	}
	
	private static int randomImage() {
	    return randomImage(-1);
	}
	
	private static int randomImage(int id) {
		int randomImageNumber;
        do {
        	randomImageNumber = new Random().nextInt((MAX_PICTURE_NUMBER - MIN_PICTURE_NUMBER) + 1) + MIN_PICTURE_NUMBER;
        } while (id == randomImageNumber);
			
	    return  randomImageNumber;
	}
	
}
