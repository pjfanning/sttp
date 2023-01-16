package sttp.client3.asynchttpclient.zio

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.capabilities.Effect
import sttp.client3.impl.zio.{RIOMonadAsyncError, ZioTestBase}
import sttp.client3._
import sttp.model.{Header, StatusCode}
import sttp.monad.MonadError
import zio.Task

class ZioFollowRedirectsBackendTest extends AsyncFlatSpec with Matchers with ZioTestBase {
  it should "properly handle invalid redirect URIs" in {
    val stubBackend: Backend[Task] = new Backend[Task] {
      override def internalSend[T](request: AbstractRequest[T, Any with Effect[Task]]): Task[Response[T]] = {
        Task.succeed(
          if (request.uri.toString.contains("redirect"))
            Response.ok("ok".asInstanceOf[T])
          else
            Response.apply(
              "".asInstanceOf[T],
              StatusCode.PermanentRedirect,
              "",
              List(Header.location("i nvalid redirect"))
            )
        )
      }

      override def close(): Task[Unit] = Task.succeed(())
      override def responseMonad: MonadError[Task] = new RIOMonadAsyncError[Any]
    }

    val result: Task[Response[_]] = basicRequest
      .response(asStringAlways)
      .get(uri"http://localhost")
      .send(FollowRedirectsBackend(stubBackend))

    convertZioTaskToFuture.toFuture(result).map { r =>
      r.body shouldBe "ok"
    }
  }
}
