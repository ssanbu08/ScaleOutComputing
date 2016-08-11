import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class PatentClaims {

	public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String fields[] = value.toString().split(",", -20);
			String country = fields[4];
			String numClaims = fields[8];
			if (numClaims.length() > 0 && !numClaims.startsWith("\"")) {
				context.write(new Text(country), new Text(numClaims + ",1"));
			}
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, DoubleWritable> {
		
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			double sum = 0;
			int count = 0;
			while (values.iterator().hasNext()) {
				String fields[] = values.iterator().next().toString().split(",");
				sum += Double.parseDouble(fields[0]);
				count += Integer.parseInt(fields[1]);
			}
			context.write(key, new DoubleWritable(sum / count));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "PatentClaims");
		
		job.setJarByClass(PatentClaims.class);
		
		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		job.setMapperClass(MapClass.class);
		job.setReducerClass(Reduce.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}