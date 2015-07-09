package common;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

/**
 * Utilities class
 * Contains Helper methods
 * 
 *  @author ok
 */
public class Utilities {

	public static String[] fileExtensions = { "jpg", "JPG", "gif", "GIF", "bmp", "BMP", "png", "PNG", "jpeg", "JPEG" };
	private final static String outputFormat = "%-40s%s%n";
	
	/**
	 * Generate result string for output
	 */
	public static String generateResultString(List<ImageDistanceMap> imageDistanceMap) {
    	String resultString = "";
    	for(int i=0; i<imageDistanceMap.size(); i++) {
    		resultString += imageDistanceMap.get(i).getDistance() + ";" + imageDistanceMap.get(i).getComparedImageTitle();
    		if(i < imageDistanceMap.size() - 1) resultString += "\n\r";
    	}
        return resultString;
    }

	/**
	 * Read MapReduce results from hdfs and return HashMap
	 */
	public static LinkedHashMap<String, Double> getResults(Configuration config) throws IOException {

		FileSystem dfs = FileSystem.get(config);
	    FileStatus[] fss = dfs.listStatus(new Path(config.get("Output")), new PathFilter() {
            public boolean accept(Path path) {
               return path.getName().startsWith("part");
            }
         }); 

	    LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>();
	    
	    for (FileStatus status : fss) {

	    	BufferedReader br = new BufferedReader(new InputStreamReader(dfs.open(status.getPath())));
	    	String line;
	    	while ((line = br.readLine()) != null) {
	    		final String[] lineArr = line.split(";");
	    		if(lineArr.length == 2) {
	    			Double distance = Double.parseDouble(lineArr[0]);
	    			map.put(lineArr[1], distance);
	    		}
	    	}
	    	br.close();
	    }
	    return map;
	}
    
    /**
     * 
     * Generate resultstring and show on CLI
     */
    public static void printResults(Configuration config) throws IOException {
    	String resultString = "\n\r";
    	resultString += "ImageSearcher Result" + "\n\r";
    	resultString += "=======" + "\r\n";
    	LinkedHashMap<String, Double> map = getResults(config);
    	for (Entry<String, Double> result : map.entrySet()) {
			String[] pathArr = result.getKey().split("/");
			String fileName = pathArr[pathArr.length-1];
			resultString += RoundTo2Decimals(result.getValue()) + "\t" + fileName + "\n\r";
    	}
    	resultString += "=======" + "\r\n";
    	System.out.println(resultString);
    }
    
    /**
     * Print usage to cli
     */
    public static void printUsage(boolean imageFeatures) {
    	if(!imageFeatures) {
    		System.err.println("");
            System.err.println("Usage: ImageSearcher [options]");
            System.err.println("");
            System.err.println("options:");
            System.err.printf(outputFormat, "-s, --search <path>", "path to local query image");
            System.err.printf(outputFormat, "-i, --images <path>", "path hdfs image directory (default: /images)");
            System.err.printf(outputFormat, "-if, --imagefeatures <path>", "path to hdfs imagefeatures (default: /imageFeatures)");
            System.err.printf(outputFormat, "-o, --output <path>", "path to output (default: /DISH)");
            System.err.printf(outputFormat, "-r, --results n", "number of results (default: 1)");
            System.err.printf(outputFormat, "-l, --lines n", "max number of lines each mapper reads (default: 10000)");
            System.err.printf(outputFormat, "-t, --time", "measure execution time");
            System.err.printf(outputFormat, "--help", "print usage information");
            System.err.printf(outputFormat, "--cli", "use commandline output instead of GUI");
            System.err.println("");
    	} else {
    		System.err.println("");
            System.err.println("Usage: ImageFeaturesGenerator [options]");
            System.err.println("");
            System.err.println("options:");
            System.err.printf(outputFormat, "-i, --images <path>", "path hdfs image directory (default: /images)");
            System.err.printf(outputFormat, "-if, --imagefeatures <path>", "path to hdfs imagefeatures result (default: /imageFeatures)");
            System.err.printf(outputFormat, "-mb, --maxbytes", "max memory size of one split (default: 1GB)");
            System.err.printf(outputFormat, "-t, --time", "measure execution time");
            System.err.printf(outputFormat, "--help", "print usage information");
            System.err.println("");
    	}
        System.exit(2);
    }
    
    /**
     * Print error to cli
     */
    public static void printError(String text) {
    	System.err.println("Error: " + text);
    	System.exit(2);
    }
    
    /**
     * Check cli argument
     */
    public static String checkArguments(String[] args, String arg, boolean hasValue) {
    	int index = Arrays.asList(args).indexOf("-" + arg);
    	if(index == -1)
    		return null;
    	if(hasValue)
    		return args[index+1];
    	return args[index];
    }
    
    /**
     * Generate ImageFeature object from File
     */
    public static ImageFeatures getImageFeatures(File f) throws IOException {
        BufferedImage bimg = ImageIO.read(f);        
        return new ImageFeatures(f.getName(), bimg); 
    }
    
    /**
     * Used to convert a very long double to print only 2 decimals
     */
    public static double RoundTo2Decimals(double val) {
        DecimalFormat df2 = new DecimalFormat("###.##");
        return Double.valueOf(df2.format(val));
    }
    
    /**
     * Checks if a given filename has an accepted imagefile extension
     */
    public static boolean hasImageFileExtension(String fileName) {
    	for(String extension : fileExtensions) {
    		if(fileName.toLowerCase().endsWith(extension)) {
                return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Convert a given human readable filesize to plain bytes
     */
    public static long parseSize(String text) {
        double d = Double.parseDouble(text.replaceAll("[GMK]B$", ""));
        long l = Math.round(d * 1024 * 1024 * 1024L);
        switch (text.charAt(Math.max(0, text.length() - 2))) {
            default:  l /= 1024;
            case 'K': l /= 1024;
            case 'M': l /= 1024;
            case 'G': return l;
        }
    }

}