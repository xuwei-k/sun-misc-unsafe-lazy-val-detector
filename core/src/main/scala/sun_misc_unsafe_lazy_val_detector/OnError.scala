package sun_misc_unsafe_lazy_val_detector

import sbt.util.CacheImplicits.*
import sjsonnew.JsonFormat

sealed abstract class OnError(private[sun_misc_unsafe_lazy_val_detector] val value: Int)
    extends Product
    with Serializable

object OnError {
  case object Default extends OnError(0)
  case object Ignore extends OnError(1)
  case object FailFast extends OnError(2)

  private val values: Seq[OnError] = Seq(
    Default,
    Ignore,
    FailFast
  )

  implicit val instance: JsonFormat[OnError] =
    SunMiscUnsafeLazyValDetector.bimapJsonFormat[Int, OnError](
      implicitly[JsonFormat[Int]],
      n => values.find(_.value == n).getOrElse(Default),
      _.value
    )
}
