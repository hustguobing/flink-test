package com.test

import org.apache.flink.api.scala._
import com.commen.BaseLoginLogParse
import org.apache.flink.core.fs.FileSystem.WriteMode

object TestSpeed {
  def main(args: Array[String]): Unit = {
    val starttime = System.currentTimeMillis()
    println("開始時間：" + starttime)
    System.setProperty("HADOOP_USER_NAME", "hdfs");

    //1.创建执行环境
    val env = ExecutionEnvironment.getExecutionEnvironment

    val guidLoginPath = "hdfs://172.16.9.102:8020/taf/warehouse/Base/LoginServer/20200305"
    val inputDataSet = env.readTextFile(guidLoginPath)

    val data = inputDataSet.map(x => BaseLoginLogParse.apply(x))
      .filter(x => x.isDefined)
      .map(x => x.get)
      .map(x => x.app + "|" + x.channel + "|" + x.guid + "|" + x.imei)
      .setParallelism(1)
      .writeAsText("hdfs://172.16.9.102:8020/tmp/flinktest", WriteMode.OVERWRITE)
    //最後面直接是文件名      WriteMode.OVERWRITE 可以直接設置為覆盖原文件

    env.execute("TestSpeed")

    val endtime = System.currentTimeMillis()
    println("結束時間：" + endtime)
    val time = (endtime - starttime)
    println("flink任務的執行時間是：" + time + " 毫秒")

    //執行時間：19424ms

    //同样功能的spark程序執行時間：37826ms

  }

}