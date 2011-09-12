package org.proofcafe.nfcdemo.util

object Preconditions {

  def checkNotNull[A](x : A): A =
    if (x != null) x else throw new IllegalArgumentException()

  def checkArgument(b: Boolean): Unit =
    if (b) () else throw new IllegalArgumentException()
}
