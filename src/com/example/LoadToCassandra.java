package com.example;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;


/*
Create the following 2 tables in Cassandra

create table movies (movieId int primary key, title text, genres text);
create table ratings(
    userId int, 
    movieId int, 
    rating float, 
    timestamp bigint,
    primary key ((userId), timestamp, movieId)
) with clustering order by (timestamp desc);
 
*/
public class LoadToCassandra {
	private static SparkSession spark = null;
	
	public static void loadCsvToCassandraTable(String path, String keyspace, String table) {
		Dataset<Row> ds = spark
				.read()
				.format("csv")
				.option("header", true)
				.option("inferSchema", true)
				.load(path);
		ds = cleanColumns(ds);
		
		Map<String, String> options = new HashMap<>();
		options.put("table", table);
		options.put("keyspace", keyspace);
		
		ds
			.write()
			.format("org.apache.spark.sql.cassandra")
			.options(options)
			.mode(SaveMode.Append)
			.save();			
		
	}
	
	public static Dataset<Row> cleanColumns(Dataset<Row> df){
		Dataset<Row> dataset = df;
		for(String col:df.columns()) {
			dataset = dataset.withColumnRenamed(col, col.toLowerCase());
		}
		return dataset;
	}
	
	public static void main(String[] args) {

		
		SparkConf conf = new SparkConf()
				.setAppName(LoadToCassandra.class.getName())
				.setIfMissing("spark.master", "local[*]");
		
		spark = SparkSession.builder().config(conf).getOrCreate();
		
		loadCsvToCassandraTable("/home/training/Downloads/datasets/ml-latest-small/movies.csv", "demo", "movies");
		loadCsvToCassandraTable("/home/training/Downloads/datasets/ml-latest-small/ratings.csv", "demo", "ratings");


		
		spark.close();
		
	}

}
