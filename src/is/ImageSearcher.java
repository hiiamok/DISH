package is;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import GUI.DISH;
import common.ImageDistanceMap;
import common.ImageFeatures;
import common.Utilities;

/**
 * ImageSearcher Class
 * 
 *  @author ok
 */
public class ImageSearcher extends Configured implements Tool {

    private static String localQueryImage = "";
    private static String imageFeatures = "/imageFeatures";
    private static String images = "/images";
    private static String output = "/DISH";
    private static int numResults = 1;
    private static boolean measureTime = false;
    private static boolean useCLI = false;
    private static Configuration config;
    private static int mapperLines = 10000;
   
    /**
     * ImageSearcher Mapper Class
     */
	public static class ImageSearchMapper extends Mapper<LongWritable, Text, NullWritable, ImageDistanceMap> {

		private List<ImageDistanceMap> topList = new ArrayList<ImageDistanceMap>();
		private int maxNumberOfResults = 1;
		
    	/**
    	 * Setup Mapper
    	 */
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			maxNumberOfResults = conf.getInt("NumberOfResults", 1);
		}
		
		/**
		 * Map method
		 * Generates Euclidian Distance between query image and all images listed in the ImageFeatures file
		 */
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		    String line = value.toString();

		    Configuration conf = context.getConfiguration();
		    ImageFeatures queryImage = new ImageFeatures(conf.get(("QueryImage")));
		    
	    	ImageFeatures currentImage = new ImageFeatures(line);
	    	ImageDistanceMap idm = new ImageDistanceMap(queryImage.calculateEuclidianDistance(currentImage), queryImage.title, currentImage.title);
	    	topList.add(idm);
		 }
		
		/**
		 * Cleanup Mapper
		 * Get Top N results of this mapper, and pass them to reducer
		 */
		protected void cleanup(Context context) throws IOException, InterruptedException {
	
        	Collections.sort(topList, new Comparator<ImageDistanceMap>(){
	    	    public int compare(ImageDistanceMap idm1, ImageDistanceMap idm2) {
	    	    	if (idm1.getDistance() < idm2.getDistance()) return -1;
	    	        if (idm1.getDistance() > idm2.getDistance()) return 1;
	    	        return 0;
	    	    }
        	});
        	
        	if(maxNumberOfResults < topList.size())
        		topList = topList.subList(0, maxNumberOfResults);

        	for(ImageDistanceMap idm : topList) 
        		context.write(NullWritable.get(), idm);

		}
	}
	
	/**
	 * ImageSearcher Reducer Class
	 */
    public static class ImageSearchReducer extends Reducer<NullWritable, ImageDistanceMap, NullWritable, Text> {
    	 
    	private List<ImageDistanceMap> topList = new ArrayList<ImageDistanceMap>();
    	private int maxNumberOfResults = 1;

    	/**
    	 * Setup Reducer
    	 */
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			maxNumberOfResults = conf.getInt("NumberOfResults", 1);
		}
		
		/**
		 * Reduce method
		 * Sorts result from mapping and returns top n results
		 */
    	protected void reduce(NullWritable key, Iterable<ImageDistanceMap> values, Context context) throws IOException, InterruptedException {
    		
    		for(ImageDistanceMap value : values) {
    			topList.add(new ImageDistanceMap(value.getDistance(), value.getQueryImageTitle(), value.getComparedImageTitle()));
    		}
        }
    	
		/**
		 * Cleanup Reducer
		 * Get Top N results of all top n mapper outputs and save result
		 */
		protected void cleanup(Context context) throws IOException, InterruptedException {
	
        	Collections.sort(topList, new Comparator<ImageDistanceMap>(){
	    	    public int compare(ImageDistanceMap idm1, ImageDistanceMap idm2) {
	    	    	if (idm1.getDistance() < idm2.getDistance()) return -1;
	    	        if (idm1.getDistance() > idm2.getDistance()) return 1;
	    	        return 0;
	    	    }
        	});
        	
        	if(maxNumberOfResults < topList.size())
        		topList = topList.subList(0, maxNumberOfResults);

        	String resultString = Utilities.generateResultString(topList);

        	context.write(NullWritable.get(), new Text(resultString));
		}
         
    }

    /**
     * Run method called for starting a MapReduce Job
     */
    public int run(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InterruptedException  {
    	checkRequiredPaths();
    	
    	long startTime = 0;
    	if(measureTime)
    		startTime = System.nanoTime(); 

    	Configuration conf = getConf();
        Job job = Job.getInstance(conf, "ImageSearcher");
        job.setJarByClass(ImageSearcher.class);
        
        job.setMapperClass(ImageSearchMapper.class);
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(ImageDistanceMap.class);

        job.setReducerClass(ImageSearchReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(NLineInputFormat.class);
        
        job.setNumReduceTasks(1);
        
        FileInputFormat.addInputPath(job, new Path(conf.get("ImageFeatures")));
        FileOutputFormat.setOutputPath(job, new Path(conf.get("Output")));  
        
        boolean res = job.waitForCompletion(true);
       
        if(measureTime) {
        	long elapsedTime = System.nanoTime() - startTime;
        	System.out.println("== MapReduce Execution Time: " + (double)elapsedTime / 1000000000.0 + "s ==");
        }
        
        return res ? 0 : 1;
    }
    
    /**
     * Parse command line arguments and set config parameters
     */
    public static void initialize(String[] args) {
        String option = null;
        
        option = Utilities.checkArguments(args, "-search", true);
        if(option != null)
        	localQueryImage = option;
        
        option = Utilities.checkArguments(args, "s", true);
        if(option != null)
        	localQueryImage = option;
        
        option = Utilities.checkArguments(args, "-output", true);
        if(option != null)
        	output = option;
        
        option = Utilities.checkArguments(args, "o", true);
        if(option != null)
        	output = option;
                
        option = Utilities.checkArguments(args, "-cli", false);
        if(option != null)
        	useCLI = true;
        
        option = Utilities.checkArguments(args, "-help", false);
        if(option != null)
        	Utilities.printUsage(false);
        
        option = Utilities.checkArguments(args, "-images", true);
        if(option != null)
        	images = option;
        
        option = Utilities.checkArguments(args, "i", true);
        if(option != null)
        	images = option;
        
        option = Utilities.checkArguments(args, "-imagefeatures", true);
        if(option != null)
        	imageFeatures = option;
        
        option = Utilities.checkArguments(args, "if", true);
        if(option != null)
        	imageFeatures = option;
        
        option = Utilities.checkArguments(args, "-results", true);
        if(option != null)
        	numResults = Integer.parseInt(option);
        
        option = Utilities.checkArguments(args, "r", true);
        if(option != null)
        	numResults = Integer.parseInt(option);
        
        option = Utilities.checkArguments(args, "-time", false);
        if(option != null)
        	measureTime = true;
        
        option = Utilities.checkArguments(args, "t", false);
        if(option != null)
        	measureTime = true;
        
        option = Utilities.checkArguments(args, "-lines", true);
        if(option != null)
        	mapperLines = Integer.parseInt(option);
        
        option = Utilities.checkArguments(args, "l", true);
        if(option != null)
        	mapperLines = Integer.parseInt(option);
        
        config.set("ImageFeatures", imageFeatures);
        config.set("Output", output);
        config.set("Images", images);
        config.setBoolean("CLI", useCLI);
        
        config.setInt("mapreduce.input.lineinputformat.linespermap", mapperLines);
    }
    
    /**
     * Method used for checking if required paths exist
     * Required Paths: ImageFeatures file; Output
     */
    public static void checkRequiredPaths() throws IOException {
    	FileSystem dfs = FileSystem.get(config);
    	
        Path imageFeaturesPath = new Path(imageFeatures);
        if(!dfs.exists(imageFeaturesPath))
        	Utilities.printError("ImageFeatures File <"+imageFeatures+"> does not exist.");
        
        Path outputPath = new Path(output);
        if(dfs.exists(outputPath)) {
        	dfs.delete(outputPath, true);
        }
    }
     
    /**
     * Main Method for starting the ImageSearcher
     */
	public static void main(String[] args) throws Exception {
		
		config = new Configuration();
        String[] otherArgs = new GenericOptionsParser(config, args).getRemainingArgs();

        initialize(otherArgs);

        if(useCLI) {
        	// use cli
        	
            if(localQueryImage == "")
            	Utilities.printUsage(false);
            
            File localQueryImageFile = new File(localQueryImage);
            
            if(!localQueryImageFile.exists())
            	Utilities.printError("Local Query File <"+localQueryImage+"> does not exist.");
            
            ImageFeatures queryImage = Utilities.getImageFeatures(localQueryImageFile);
            config.set("QueryImage", queryImage.toString());
            config.setInt("NumberOfResults", numResults);
            
            ToolRunner.run(config, new ImageSearcher(), args);
            
            Utilities.printResults(config);
            
        } else {
        	// use gui
        	
        	config.set("QueryImage", localQueryImage);
			new DISH(config, args);
        }
	}
}