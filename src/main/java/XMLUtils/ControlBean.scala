package XMLUtils

import java.io.InputStream

import org.apache.commons.lang.StringUtils

import scala.xml.XML


/**
  * Created by Nukil on 2017/5/17.
  */
class ControlBean(val inStream: InputStream) {
    //数据库连接信息
    var dbClass: String = _
    var dbUrl: String = _
    var dbUser: String = _
    var dbPwd: String = _
    //表名称定义
    var tableName: String = _
    //表字段定义
    var id: String = _
    var plateNumber: String = _
    var plateColor: String = _
    var vehicleBrand: String = _
    var vehicleColor: String = _
    var vehicleType: String = _
    var orgId: String = _
    var monitorId: String = _
    var beginTime: String = _
    var endTime: String = _
    var alarmStatus: String = _
    var alarmType: String = _
    var controlLevel: String = _
    var controlUser: String = _

    initControl()

    def initControl(): Unit = {
        try {
            val conf = XML.load(inStream)

            //解析XML数据库连接信息
            (conf \ "connection" \\ "_").foreach(e => {
                e.label match {
                    case "driverClass" => dbClass = StringUtils.trimToNull(e.text)
                    case "url" => dbUrl = StringUtils.trimToNull(e.text)
                    case "user" => dbUser = StringUtils.trimToNull(e.text)
                    case "password" => dbPwd = {
                        val tmp = StringUtils.trimToNull(e.text)
                        if ("null".equalsIgnoreCase(tmp)) null else tmp
                    }
                    case _ =>
                }
            })
            if (null == dbClass || null == dbUrl || null ==dbUser) {
                throw new Exception(s"Error: driverClass || url || user is null, $dbClass, $dbUrl, $dbUser")
            }
            //解析表名称
            tableName = StringUtils.trimToNull((conf \ "table" \ "name").text)
            if (null == tableName) {
                throw new Exception(s"Error: table name is null, $tableName")
            }
            //解析表字段名称
            (conf \ "fields" \\ "_").foreach(e => {
                e.label match {
                    case "id" => id = StringUtils.trimToNull((e \ "field").text)
                    case "platenumber" => plateNumber = StringUtils.trimToNull((e \ "field").text)
                    case "platecolor" => plateColor = StringUtils.trimToNull((e \ "field").text)
                    case "vehiclebrand" => vehicleBrand = StringUtils.trimToNull((e \ "field").text)
                    case "vehiclecolor" => vehicleColor = StringUtils.trimToNull((e \ "field").text)
                    case "vehicletype" => vehicleType = StringUtils.trimToNull((e \ "field").text)
                    case "orgid" => orgId = StringUtils.trimToNull((e \ "field").text)
                    case "monitorid" => monitorId = StringUtils.trimToNull((e \ "field").text)
                    case "begintime" => beginTime = StringUtils.trimToNull((e \ "field").text)
                    case "endtime" => endTime = StringUtils.trimToNull((e \ "field").text)
                    case "alarmstatus" => alarmStatus = StringUtils.trimToNull((e \ "field").text)
                    case "alarmtype" => alarmType = StringUtils.trimToNull((e \ "field").text)
                    case "controllevel" => controlLevel = StringUtils.trimToNull((e \ "field").text)
                    case "controluser" => controlUser = StringUtils.trimToNull((e \ "field").text)
                    case _ =>
                }
            })
            if (null == id || null == plateNumber || null == plateColor
                || null == vehicleBrand || null == vehicleColor || null == vehicleType
                || null == orgId || null == monitorId || null == beginTime
                || null == endTime || null == alarmStatus || null == alarmType
                || null == controlLevel || null == controlUser) {
                throw new Exception("table fields is null")
            }
        } finally {
            try {
                inStream.close()
            } catch {
                case _: Throwable =>
            }
        }
    }

    override def toString: String = {
        s"connections:{db_class=$dbClass,db_url=$dbUrl,db_user=$dbUser,db_pwd=$dbPwd}," +
          s"table:{$tableName},fields:{id=$id,platenumber=$plateNumber,platecolor=$plateColor," +
          s"vehiclebrand=$vehicleBrand,vehiclecolor=$vehicleColor,vehicletype=$vehicleType," +
          s"orgid=$orgId,monitorid=$monitorId,begintime=$beginTime,endtime=$endTime,alarmstatusT=$alarmStatus," +
          s"alarmtype=$alarmType,controllevel=$controlLevel,controluser=$controlUser}"
    }
}
