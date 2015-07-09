package ifg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import common.ImageFeatures;
import common.Utilities;

/**
 * ImageFeaturesGnerator Class
 * 
 *  @author ok
 */
public class ImageFeaturesGenerator {

	private static boolean measureTime = false;
	private static String splitSize = "1GB";
	
    /**
     * ImageFeaturesGenerator Mapper Class
     */
	public static class ImageFeaturesGeneratorMapper extends Mapper<Text, BytesWritable, NullWritable, Text> {
		
		/**
		 * Map method
		 * Generates ImageFeatures String and writes it to output file
		 */
		 public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
			 
			 String fileName = key.toString();
			 byte[] imageData = value.getBytes();

			 ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
			 BufferedImage bimg = ImageIO.read(bais);			 
			 ImageFeatures imgf = new ImageFeatures(fileName, bimg);

			 context.write(NullWritable.get(), new Text(imgf.toString()));
		 }
	}
	
    /**
     * Main Method for starting the ImageFeaturesGenerator
     */
	public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        String imagesDirectory = "/images";
        String outputFile = "/imageFeatures";
        
        String option = null;
        
        option = Utilities.checkArguments(otherArgs, "-help", false);
        if(option != null)
        	Utilities.printUsage(true);
        
        option = Utilities.checkArguments(otherArgs, "-images", true);
        if(option != null)
        	imagesDirectory = option;
        
        option = Utilities.checkArguments(otherArgs, "i", true);
        if(option != null)
        	imagesDirectory = option;
        
        option = Utilities.checkArguments(otherArgs, "-imagefeatures", true);
        if(option != null)
        	outputFile = option;
        
        option = Utilities.checkArguments(otherArgs, "if", true);
        if(option != null)
        	outputFile = option;
        
        option = Utilities.checkArguments(args, "-time", false);
        if(option != null)
        	measureTime = true;
        
        option = Utilities.checkArguments(args, "t", false);
        if(option != null)
        	measureTime = true;
        
        option = Utilities.checkArguments(args, "-maxbytes", true);
        if(option != null)
        	splitSize = option;
        
        option = Utilities.checkArguments(args, "mb", true);
        if(option != null)
        	splitSize = option;
        
        Path imagesPath = new Path(imagesDirectory);
        
        FileSystem dfs = FileSystem.get(conf);
        if(!dfs.exists(imagesPath))
        	Utilities.printError("Images Directory <"+imagesPath+"> does not exist.");    
        
        Path outputPath = new Path(outputFile);
        if(dfs.exists(outputPath)) {
        	dfs.delete(outputPath, true);
        }
		
    	long startTime = 0;
    	if(measureTime)
    		startTime = System.nanoTime(); 

    	Job job = Job.getInstance(conf, "ImageSearcher");
	
	    job.setJarByClass(ImageFeaturesGenerator.class);
	    job.setJobName("ImageFeaturesGenerator");
	    job.setOutputKeyClass(NullWritable.class);
	    job.setOutputValueClass(Text.class);
	    job.setInputFormatClass(BinaryFileInputFormat.class);
	    job.setMapperClass(ImageFeaturesGeneratorMapper.class);
	    job.setNumReduceTasks(0);
	    
	    BinaryFileInputFormat.setMaxInputSplitSize(job, Utilities.parseSize(splitSize));
	   
	    FileInputFormat.addInputPath(job, imagesPath);
	    FileInputFormat.setInputDirRecursive(job, true);
	    
	    FileOutputFormat.setOutputPath(job, outputPath);
	    
	    boolean res = job.waitForCompletion(true);

        if(measureTime) {
        	long elapsedTime = System.nanoTime() - startTime;
        	System.out.println("== MapReduce Execution Time: " + (double)elapsedTime / 1000000000.0 + "s ==");
        }
        
        System.exit(res ? 0 : 1);
	}
	
}