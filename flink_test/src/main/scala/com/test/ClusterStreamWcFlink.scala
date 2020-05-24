package com.test

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.typeinfo.TypeInformation

object ClusterStreamWcFlink {
  def main(args: Array[String]): Unit = {

    /**
     * /flink/flink-1.7.2/bin/flink run -c com.test.ClusterStreamWcFlink -p 2 /root/flink_test-0.0.1-SNAPSHOT.jar 172.16.9.142 7777
     */
    val host = args(0)
    val port = args(1)
    //1.创建流处理执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //2.接受一个socket文本流           win本地安裝ncat      nc -l -p7777    172.16.9.142执行 nc -lk 7777
    val dataStream = env.socketTextStream(host, port.toInt)
    import org.apache.flink.streaming.api.scala._
    val wcStreamSet = dataStream.flatMap(t => t.split(" "))
      .filter(_.nonEmpty)
      .map((_, 1))
      .keyBy(0)
      .sum(1)

    wcStreamSet.print().setParallelism(2)

    env.execute()
  }
}