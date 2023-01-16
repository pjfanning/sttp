package sttp.client3.httpclient.fs2

import cats.effect.IO
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3._
import sttp.client3.impl.cats.CatsTestBase

trait HttpClientFs2TestBase extends CatsTestBase {
  implicit val backend: WebSocketStreamBackend[IO, Fs2Streams[IO]] =
    HttpClientFs2Backend[IO](blocker).unsafeRunSync()
}
