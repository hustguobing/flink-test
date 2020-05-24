package com.test

import org.apache.flink.streaming.api.scala._
import java.util.Properties
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink
import org.apache.flink.streaming.connectors.fs.StringWriter
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.types.Row
import java.sql.Types

/**
 * /flink/flink-1.7.2/bin/flink run 	-c com.test.StreamWithKafka2Mysql -p 4 /root/flink_test-0.0.1-SNAPSHOT.jar
 *
 * /opt/cloudera/parcels/KAFKA-3.0.0-1.3.0.0.p0.40/lib/kafka/bin/kafka-console-producer.sh --broker-list 172.16.9.109:9092,172.16.9.110:9092 --topic test
 */
case class Test(word: String, num: Int)
object StreamWithKafka2Mysql {

  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "hdfs");
    //设置执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //设置kafka相关连接属性
    val pro = new Properties()
    pro.setProperty("bootstrap.servers", "172.16.9.109:9092,172.16.9.110:9092")
    pro.setProperty("group.id", "test")
    pro.setProperty("fs.default-scheme", "hdfs://172.16.9.102:8020")

    val text = env.addSource(new FlinkKafkaConsumer011[String]("test", new SimpleStringSchema(), pro))

    val counts = text.flatMap(_.split(" ")).map((_, 1)).keyBy(0).timeWindow(Time.seconds(5)).sum(1)
      .map(x => Test(x._1, x._2))

    //要使用JDBCOutputFormat将数据写入mysql需要先将数据转换成org.apache.flink.types.Row类型
    val rows = counts.map(x => {
      val row = new Row(2)
      row.setField(0, x.word)
      row.setField(1, x.num)
      row
    });

    //使用JDBCOutputFormat将数据写入mysql
    rows.writeUsingOutputFormat(
      JDBCOutputFormat.buildJDBCOutputFormat()
        .setDrivername("com.mysql.jdbc.Driver")
        .setDBUrl("jdbc:mysql://172.16.9.252:3306/db_base_stat?rewriteBatchedStatements=true&useUnicode=true&characterEncoding=UTF-8")
        .setUsername("root1")
        .setPassword("123456")
        .setBatchInterval(2) //设置写入批次数量
        .setQuery("insert into flink_test(word,num) values(?,?)")
        .finish())

    env.execute("StreamWithKafka Test")

  }

}