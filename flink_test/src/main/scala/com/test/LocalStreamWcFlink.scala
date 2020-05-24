package com.test

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.typeinfo.TypeInformation

object LocalStreamWcFlink {
  def main(args: Array[String]): Unit = {

    //1.创建流处理执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //2.接受一个socket文本流           win本地安裝ncat      nc -l -p7777
    val dataStream = env.socketTextStream("localhost", 7777)
    //    implicit val typeInfo = TypeInformation.of(classOf[String])   //这种做法稍显复杂，可以采用在下面引入隱式轉換的做法
    import org.apache.flink.streaming.api.scala._
    val wcStreamSet = dataStream.flatMap(t => t.split(" "))
      .filter(_.nonEmpty)
      .map((_, 1))
      .keyBy(0)
      .sum(1)

    wcStreamSet.print().setParallelism(2)
    //setParallelism设置并行度，並行度设置超过集群资源会导致提交任务不成功卡住，表現为页面一直转圈

    env.execute()
  }
}