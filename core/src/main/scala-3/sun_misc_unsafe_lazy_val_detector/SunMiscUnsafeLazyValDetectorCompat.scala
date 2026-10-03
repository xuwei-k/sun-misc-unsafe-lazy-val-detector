package sun_misc_unsafe_lazy_val_detector

trait SunMiscUnsafeLazyValDetectorCompat { self: SunMiscUnsafeLazyValDetector.type =>
  private[sun_misc_unsafe_lazy_val_detector] val cache: SunMiscUnsafeLazyValDetectorCache[xsbti.HashedVirtualFileRef] =
    new SunMiscUnsafeLazyValDetectorCache[xsbti.HashedVirtualFileRef]

  final private[sun_misc_unsafe_lazy_val_detector] def getOrElseUpdateCache(
    key: SunMiscUnsafeLazyValDetectorCache.Key[xsbti.HashedVirtualFileRef],
    computeValue: () => List[(String, Int)]
  ): List[(String, Int)] =
    cache.getOrElseUpdateCache(key, computeValue)
}
