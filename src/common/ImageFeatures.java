package common;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import net.semanticmetadata.lire.imageanalysis.CEDD;

/**
 * ImageFeatures class
 * Contains Title and Histogram of an image
 * 
 *  @author ok
 */
public class ImageFeatures {

	public String title = "";
	public double[] histogram;
	
	/**
	 * Constructor used for generating object from Name and BufferedImage.
	 */
	public ImageFeatures(String imageName, BufferedImage bufferedImage) {
		this.title = imageName;
		generateImageHistogram(bufferedImage);
	}
	
	/**
	 * Constructor used for generating object from ImageFeatures String
	 */
	public ImageFeatures(String imageFeatures) {
		String[] features = imageFeatures.split(";");
		ArrayList<String> featureList = new ArrayList<String>(Arrays.asList(features));
		
		this.title = featureList.get(0);
		
		double[] histogram = new double[featureList.size()-1];
		for(int i=1; i<histogram.length; i++) {
			histogram[i] = Double.parseDouble(featureList.get(i));
		}
		
		this.histogram = histogram;
	}
	
	/**
	 * Generate Image Histogram with help of 3rd party framwork LIRE
	 */
	private void generateImageHistogram(BufferedImage bufferedImage) {
		CEDD bimgCedd = new CEDD();
		bimgCedd.extract(bufferedImage);
		this.histogram = bimgCedd.getDoubleHistogram();
	}
	
	/**
	 * Generate a String representation of object
	 */
	public String toString() {
		String imageFeatureString = this.title + ";";
		if(this.histogram != null && this.histogram.length > 0) {
			for(int i=0; i<this.histogram.length; i++) {
				imageFeatureString += this.histogram[i] + ";";
			}			
		}
		return imageFeatureString;
	}
	
	/**
	 * Calculate Euclidian Distance between this and another ImageFeature object
	 */
	public double calculateEuclidianDistance(ImageFeatures compareImageFeature) {
		double Sum = 0.0;
		if(compareImageFeature == null) {
			if(this.histogram == null) {
				return 0.0; 
			}
			return Double.MAX_VALUE;
		}

		if(this.histogram.length != compareImageFeature.histogram.length)
			return Double.MAX_VALUE;
        
        for(int i=0;i<this.histogram.length;i++) {
           Sum = Sum + Math.pow((this.histogram[i]-compareImageFeature.histogram[i]),2.0);
        }
        
        return Math.sqrt(Sum);		
	}
	
}