package com.commen

import java.text.SimpleDateFormat
import java.util.Properties

/**
 * 心跳日志信息
 */
case class BaseLoginRecord(guid: String, serverTimestamp: Long, loginIp: String, serverIp: String, app: String,
                           version: String, channel: String, plat: String, mo: String, vc: String,
                           rlWidth: Long, rlHeight: Long, imei: String, mac: String, var loginReason: String, var isNew: Int, xua: String, pkgName: String)
object BaseLoginLogParse {
  def main(args: Array[String]) {
    val line1 = """10.25.67.116|2019-12-22 02:17:25||868510039553978|SN=ADR_com.upchina.teach&VN=19121004_2.1.9_19121004_GA&MO=HWI-AL00&VC=HUAWEI&RV=HUAWEI_9.1.0.216C00&OS=28&CHID=55042_55042&MN=teach|698e46dec5d2e93ba68fb07acb982030a4f94912|1|1576952244145|36.161.55.173||background|0|404f9ff3c56a66dca9f302afc442f580|4|0|0|ADR_COM.UPCHINA.TEACH|19121004_2.1.9_19121004_GA|55042_55042|"""
    val line2 = """10.27.94.107|2019-12-22 00:34:50||865297034521787|SN=ADR_com.upchina.teach&VN=19121004_2.1.9_19121004_GA&MO=MHA-AL00&VC=HUAWEI&RV=HUAWEI_9.1.0.212C00&OS=28&CHID=55042_55042&MN=teach|4b4b06ea7c09123e98665a50740c522c799898ae|4|1576946088973|223.104.7.99||background|0|2b98afaf09384f5d6de01159961a9463|4|0|0|ADR_COM.UPCHINA.TEACH|19121004_2.1.9_19121004_GA|55042_55042|"""
    val line3 = """10.252.103.142|2016-12-07 11:05:55||863989022016372|SN=ADR_com.upchina&VN=1114_4.3.2&MO=HM NOTE 1W&VC=Xiaomi&RL=720_1280&RV=Xiaomi_V8.1.1.0.KHDCNDI&OS=19&CHID=9510_9510&MN=stock|0c:1d:af:73:54:1f|1|1481079953327|100.97.172.239||guid|0|0c8a4f3054b9f80cfb15cc153a60da2d|3|0|0|ADR_COM.UPCHINA|1114_4.3.2|9510_9510"""
    val line4 = """10.25.67.116|2019-12-22 11:47:21||868629047225757|SN=ADR_com.upchina.teach&VN=19121004_2.1.9_19121004_GA&MO=POT-AL00a&VC=HUAWEI&RV=HUAWEI_9.1.0.241C00&OS=28&CHID=55042_55042&MN=teach|df061041d0b258aad32cc187650a4fd0055c7d0b|1|1576986440407|113.101.39.43||background|0|47414f5a4181acfaac6d9567a6ad650d|4|0|0|ADR_COM.UPCHINA.TEACH|19121004_2.1.9_19121004_GA|55042_55042|"""
    val line5 = """10.161.235.252|2019-12-22 00:35:35|||SN=ADR_com.upchina.teach&VN=19121004_2.1.9_19121004_GA&MO=VOG-AL10&VC=HUAWEI&RV=HUAWEI_10.0.0.173C00&OS=29&CHID=55042_55042&MN=teach|74a55b7fde14cf887ce3674abd997e3355509916|1|1576946134616|110.87.47.215||active|0|33c65162b0ef065810f38ec6b7139ebf|4|0|0|ADR_COM.UPCHINA.TEACH|19121004_2.1.9_19121004_GA|55042_55042|"""
    println("line1:" + BaseLoginLogParse(line1))
    println("line2:" + BaseLoginLogParse.apply(line2))
    println("line3:" + BaseLoginLogParse.apply(line3))
    println("line3:" + BaseLoginLogParse.apply(line4))
    println("line5:" + BaseLoginLogParse.apply(line5))
  }

  def apply(line: String): Option[BaseLoginRecord] = {
    try {
      val splits = line.split("\\|")
      val serverIp = splits(0)
      val serverTime = splits(1)
      //新增用户的guid字段位置不相同
      val isNew = "".equalsIgnoreCase(splits(2)) || "00000000000000000000000000000000".equalsIgnoreCase(splits(2))
      val guid = if (isNew) splits(12) else splits(2)
      val serverTimestamp = LogUtils.parseTimestamp(serverTime)
      val imei = splits(3)
      //解析XUA
      val xua = splits(4)
      val xuaOption = LogUtils.parseXuaALL(xua);
      if (xuaOption.isDefined) {
        val (app, plat, os, version, channel, mo, vc, width, height, pkgName) = xuaOption.get
        val mac = splits(5)
        val loginIp = splits(8)
        val loginReasion = splits(10)
        Option(BaseLoginRecord(guid, serverTimestamp, loginIp, serverIp, app, version, channel, plat, mo, vc, width.toLong, height.toLong, imei, mac, loginReasion, if (isNew) 1 else 0, xua, pkgName))
      } else {
        Option[BaseLoginRecord](null)
      }
    } catch {
      case e: Throwable => {
        Option[BaseLoginRecord](null)
      }
    }
  }
}