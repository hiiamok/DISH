package common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

/**
 * ImageDistanceMap
 * Writable used for passing results of Mapper to Reducer
 * 
 *  @author ok
 */
public class ImageDistanceMap implements WritableComparable<ImageDistanceMap> {

	private DoubleWritable distance;
	private Text queryImageTitle;
	private Text comparedImageTitle;
	
	/**
	 * Constructor for empty object
	 */
	public ImageDistanceMap() {
		set(new DoubleWritable(), new Text(), new Text());
	}
	
	/**
	 * Constructor for new object based on given object
	 */
	public ImageDistanceMap(ImageDistanceMap idm) {
		set(idm.distance, idm.queryImageTitle, idm.comparedImageTitle);
	}
	
	/**
	 * Constructor for given parameters
	 */
	public ImageDistanceMap(double distance, String qImageTitle, String compImageTitle) {
		set(new DoubleWritable(distance), new Text(qImageTitle), new Text(compImageTitle));
	}
	
	/**
	 * Helper method for setting properties
	 */
	public void set(DoubleWritable distance, Text qImageTitle, Text compImageTitle) {
		this.distance = distance;
		this.queryImageTitle = qImageTitle;
		this.comparedImageTitle = compImageTitle;
	}
	
	/**
	 * Return Title of Query Image
	 */
	public String getQueryImageTitle() {
		return queryImageTitle.toString();
	}
	
	/**
	 * Return Title of ComparedImage
	 */
	public String getComparedImageTitle() {
		return comparedImageTitle.toString();
	}
	
	/**
	 * Get Distance of Compared Images
	 */
	public double getDistance() {
		return distance.get();
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		distance.readFields(in);
		queryImageTitle.readFields(in);
		comparedImageTitle.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		distance.write(out);
		queryImageTitle.write(out);
		comparedImageTitle.write(out);
	}

	@Override
	public int compareTo(ImageDistanceMap o) {
		int cmp = distance.compareTo(o.distance);
		if(cmp == 0) {
			cmp = queryImageTitle.compareTo(o.queryImageTitle);
			if(cmp == 0)
				cmp = comparedImageTitle.compareTo(o.comparedImageTitle);
		}
		return cmp;
	}
	
}