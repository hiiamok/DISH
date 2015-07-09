# DISH
Distributed Image Search with Hadoop

# Tests

	All the tests were made on the 36GB ALOI image set (http://aloi.science.uva.nl)
	
	The tests included 2 tasks:
		* The first task was to generate an ImageFeatures file.
		* The second task was to find 10 similar images to a given image
	
	For a better performance comparison, all tasks were run on nodes with the same system settings.
	They were set up as virtual machines with 3GB RAM and 2 CPU cores (2.8GHz i7)
	running on Ubuntu Server 15.04

	Cluster setup:
		In a real environment the namenode may be on a different server than the datanodes.
		In this tests, the namenode was running on the same node with 1 datanode.
		Since the virtual machine cluster is not going to be bigger than 4 virtual machines
		this setup allows us to use one more datanode.
		
	Also there were no special cluster tuning made. So only default settings were used.

# Results

## 1 node

		* ImageFeaturesGenerator		83	minutes
		* ImageSearcher					96	seconds
		
## 2 nodes

		* ImageFeaturesGenerator		53	minutes
		* ImageSearcher					78	seconds

## 3 nodes
	
		* ImageFeaturesGenerator		31	minutes
		* ImageSearcher					61	seconds

## 4 nodes
	
		* ImageFeaturesGenerator		17	minutes
		* ImageSearcher					41	seconds
		
# One more test

	The last test was run on a MacBook Pro Quad Core (2.8GHz i7) with 16GB RAM.
	(host system of the earlier run virtual machines cluster)
	
	This test was just run to compare the results to a native system with more resources.
	Only a single node setup with default settings was used.
	
		* ImageFeaturesGenerator		21	minutes
		* ImageSearcher					46	seconds

# Conclusion

	The results are as expected. The more datanodes are used,
	the faster the tasks could be executed. Several mapper tasks
	can work parallel. And as not all files are stored on one datanode, 
	each datanodes has their own data to process.