import com.despegar.sbt.madonna.Madonna.{MadonnaKeys, _}

import scala.concurrent.duration._

madonnaSettings

MadonnaKeys.healthCheckURI := "/sifin/health-check"
MadonnaKeys.healthCheckTimeout := 400.seconds

mainClass := Some("manuellimaf.sifin.SiFinMain")

publish <<= MadonnaKeys.tarPublish

publishLocal <<= MadonnaKeys.tarPublishLocal
