package sun_misc_unsafe_lazy_val_detector

import scala.collection.concurrent.TrieMap

private[sun_misc_unsafe_lazy_val_detector] final class SunMiscUnsafeLazyValDetectorCache[A] {
  private[sun_misc_unsafe_lazy_val_detector] val cache
    : TrieMap[SunMiscUnsafeLazyValDetectorCache.Key[A], List[(String, Int)]] =
    TrieMap.empty

  def getOrElseUpdateCache(
    key: SunMiscUnsafeLazyValDetectorCache.Key[A],
    computeValue: () => List[(String, Int)]
  ): List[(String, Int)] =
    cache.getOrElseUpdate(key, computeValue())
}

private[sun_misc_unsafe_lazy_val_detector] object SunMiscUnsafeLazyValDetectorCache {
  final case class Key[A](key: A, directUnsafe: Boolean) {
    def map[B](f: A => B): Key[B] = Key(f(key), directUnsafe)
  }
}
