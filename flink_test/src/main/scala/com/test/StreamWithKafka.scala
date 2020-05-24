package com.test

import org.apache.flink.streaming.api.scala._
import java.util.Properties
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink
import org.apache.flink.streaming.connectors.fs.StringWriter
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

/**
 * 执行shell
 * /opt/cloudera/parcels/KAFKA-3.0.0-1.3.0.0.p0.40/lib/kafka/bin/kafka-console-producer.sh --broker-list 172.16.9.109:9092,172.16.9.110:9092 --topic test
 *
 * /flink/flink-1.7.2/bin/flink run --classpath file:///flink/flink-1.7.2/lib/flink-connector-kafka-0.11_2.11-1.7.2.jar --classpath file:///flink/flink-1.7.2/lib/flink-connector-kafka-0.10_2.11-1.7.2.jar --classpath file:///flink/flink-1.7.2/lib/flink-connector-kafka-0.9_2.11-1.7.2.jar --classpath file:///flink/flink-1.7.2/lib/flink-connector-kafka-base_2.11-1.7.2.jar --classpath file:///flink/flink-1.7.2/lib/flink-connector-filesystem_2.11-1.7.2.jar  --classpath file:///flink/flink-1.7.2/lib/kafka-clients-0.11.0.1.jar   -c com.test.StreamWithKafka -p 4 /root/flink_test-0.0.1-SNAPSHOT.jar
 * 重启集群后不需=需要在任务提交脚本中添加如上依赖
 * /flink/flink-1.7.2/bin/flink run    -c com.test.StreamWithKafka -p 4 /root/flink_test-0.0.1-SNAPSHOT.jar
 */
object StreamWithKafka {

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

    //hdfs写入工具
    val sink = new BucketingSink[(String, Int)]("hdfs://172.16.9.102:8020/tmp/flinkoutput")

    //设置sink属性
    sink.setWriter(new StringWriter()) // 自定义输出类型，还有MemberWriter等
      .setBatchSize(120 * 1024 * 1024) // 设置每个文件的最大大小 ,默认是384M。这里设置为120M
      .setBatchRolloverInterval(Long.MaxValue) // 滚动写入新文件的时间，默认无限大
      .setInactiveBucketCheckInterval(60 * 1000) // 1分钟检查一次不写入的文件
      .setInactiveBucketThreshold(5 * 60 * 1000) // 5min不写入，就滚动写入新的文件
      .setPartSuffix(".log") // 文件后缀

    //以5秒为固定时间窗口 将数据写入hdfs与输出 也可以使用不同的窗口
    val counts = text.flatMap(_.split(" ")).map((_, 1)).keyBy(0).timeWindow(Time.seconds(5)).sum(1)

    counts.print()

    counts.addSink(sink)

    env.execute("StreamWithKafka Test")

  }

}