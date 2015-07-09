# DISH
Distributed Image Search with Hadoop


# Small File Problem of Hadoop

This is a well known problem of hadoop.
Hadoop was designed for using it with large datafiles.
Every file has a block size of default 64MB. 
This means a file is splitted over datanodes if its > 64MB, otherwise its fully saved on 1 datanode.

# What does this mean for MapReduce?

If a binary file is read, there is one mapper started per file.
If there is a huge amout of files - like about 70.000 - there are 70.000 mapper tasks started.
This is a huge overhead and can result in running in a Java out of memory error.

# How can one avoid this problem?

If possible files could be combined to bigger files.
In case of binary files this is mostly not possible.
In this case, all the files could be written into sequence files.

Or we could just generate a list of all files, and instead of reading all the files all the time, 
we are reading this list.

# Generating a file-index list

In my opinion there are 2 ways of accomplishing this task:
	
## Using a simple file-by-file processing
		
	This task would run on as 1 process only.
	It would be possible to start some parallel threads to read and process all files.
	Still this approve would take some serious amount of time for a huge set of files.

## Using MapReduce
		
	The file could be process via a mapper.
	If all files would be stored on only one datanode, this wouldnt be any better than a)
	But hopefully in a setup cluster not all files are getting stored on one node.		
	It is also possible to combine inputs. This means, one mapper reads several input files at once.

	Because small files wont be splitted over several blocks, we have to set
	some limit for each mapper task. It is possible to set the max memory size of 1 split.
	So there would be several splits generated.