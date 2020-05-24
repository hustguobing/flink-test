package com.test

import org.apache.flink.api.scala._

object LocalWcFlink {
  def main(args: Array[String]): Unit = {

    //1.创建执行环境
    val env = ExecutionEnvironment.getExecutionEnvironment

    val inputPath = s"C:\\Users\\gb\\Desktop\\flink单机安装\\flink_test\\resources\\hello.txt"
    //2.读取数据
    val inputDataSet = env.readTextFile(inputPath)

    val wcDataSet = inputDataSet.flatMap(_.split(" "))
      .map(x => (x, 1))
      .groupBy(0)
      .sum(1)

    wcDataSet.print()

  }
}