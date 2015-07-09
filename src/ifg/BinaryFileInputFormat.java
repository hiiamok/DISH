package ifg;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

/**
 * BinaryFileInputFormat Class
 * Used for reading several BinaryFiles for one MapperTask
 * 
 *  @author ok
 */
public class BinaryFileInputFormat extends CombineFileInputFormat<Text, BytesWritable> {

	/**
	 * Do not allow to split one file into several parts
	 */
    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return false;
    }
    
    /**
     * Return a CombineFileRecordReader
     */
    @Override
    public CombineFileRecordReader<Text, BytesWritable> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {

        if (!(split instanceof CombineFileSplit))
            throw new IllegalArgumentException("Input must be a Directory!");

        return new CombineFileRecordReader<Text, BytesWritable>((CombineFileSplit) split, context, BinaryFileRecordReader.class);
    }
    
}