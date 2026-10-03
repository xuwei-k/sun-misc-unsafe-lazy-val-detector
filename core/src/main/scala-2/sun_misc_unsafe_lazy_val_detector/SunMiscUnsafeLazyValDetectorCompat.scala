package sun_misc_unsafe_lazy_val_detector

trait SunMiscUnsafeLazyValDetectorCompat { self: SunMiscUnsafeLazyValDetector.type =>
  private[sun_misc_unsafe_lazy_val_detector] val cache: SunMiscUnsafeLazyValDetectorCache[String] =
    new SunMiscUnsafeLazyValDetectorCache[String]

  final private[sun_misc_unsafe_lazy_val_detector] def getOrElseUpdateCache(
    key: SunMiscUnsafeLazyValDetectorCache.Key[java.io.File],
    computeValue: () => List[(String, Int)]
  ): List[(String, Int)] =
    cache.getOrElseUpdateCache(key.map(_.getCanonicalPath), computeValue)
}
