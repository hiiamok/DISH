package ifg;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

/**
 * BinaryFileRecordReader Class
 * 
 *  @author ok
 */
public class BinaryFileRecordReader extends RecordReader<Text, BytesWritable>{

	private Path fileToRead;
	private long fileLength;
	private Configuration config;
	private boolean processed;
	
	private Text key;
	private BytesWritable value;
	
	/**
	 * Constructor
	 */
    public BinaryFileRecordReader(CombineFileSplit fileSplit, TaskAttemptContext context, Integer pathToProcess) {
    	
    	processed = false;
    	fileToRead = fileSplit.getPath(pathToProcess);
    	fileLength = fileSplit.getLength(pathToProcess);
    	config = context.getConfiguration();

    	assert 0 == fileSplit.getOffset(pathToProcess);
    	
    	try {
    		FileSystem dfs = FileSystem.get(config);
    		assert dfs.getFileStatus(fileToRead).getLen() == fileLength;
    		
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	key = new Text(Path.getPathWithoutSchemeAndAuthority(fileToRead).toString());
    	value = new BytesWritable();
    }
    
    /**
     * Reset
     */
	@Override
	public void close() throws IOException {
		key.clear();
		value.set(new byte[0], 0, 0);
	}
	
	/**
	 * Get the current key
	 */
	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}
	
	/**
	 * Get the current value
	 */
	@Override
	public BytesWritable getCurrentValue() throws IOException, InterruptedException {
		return value;
	}
	
	/**
	 * Get progress
	 */
	@Override
	public float getProgress() throws IOException, InterruptedException {
		 return (processed) ? (float) 1.0 : (float) 0.0;
	}
	
	/**
	 * Initialize, nothing to do here since we are using a constructor
	 */
	@Override
	public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
    	// nothing to do here
	}
	
	/**
	 * Read next binary file and return true
	 * or return false if no more file to read
	 */
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		
		if(processed)
			return false;
		
		if(fileLength > (long)Integer.MAX_VALUE)
			 throw new IOException("File is longer than Integer.MAX_VALUE.");		
		
		byte[] contents = new byte[(int)fileLength];
		
		FileSystem dfs = fileToRead.getFileSystem(config);
		FSDataInputStream in = null;
		
		try {
			in = dfs.open(fileToRead);
			IOUtils.readFully(in, contents, 0, contents.length);
			value.set(contents, 0, contents.length);
			
		} finally {
			IOUtils.closeStream(in);
		}
		
		processed = true;
		
		return true;
	}
	
}