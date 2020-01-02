package com.cloriko.master.http

import cats.effect.IO
import com.cloriko.DecoderImplicits._
import com.cloriko.master.{ Cloriko, UserAuthenticator }
import com.cloriko.protobuf.protocol.{ Delete, FetchRequest, File, FileReference, Update }
import monix.execution.Scheduler.Implicits.global
import org.http4s.HttpRoutes
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.{ ->, /, Ok, POST, Root }

import scala.util.Random
import cats.effect._
import com.google.protobuf.ByteString
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.multipart.Multipart
import cats.implicits._
import com.cloriko.master.UserAuthenticator.{ SignInResult, SignUpResult }
import com.cloriko.master.UserAuthenticator.SignInResult.SignInResult
import com.cloriko.master.UserAuthenticator.SignUpResult.SignUpResult
import com.cloriko.master.http.UserAuthRoutes.{ SignInEntity, SignUpEntity }
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import scala.concurrent.Future

trait UserAuthRoutes {

  val cloriko: Cloriko
  implicit val signUpRequestEntityDecoder = jsonOf[IO, SignUpEntity]
  implicit val logInRequestEntityDecoder = jsonOf[IO, SignInEntity]

  lazy val userRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root / "signUp" => {
      val signUpRequest: SignUpEntity = req.as[SignUpEntity].unsafeRunSync()
      println(s"SignUp entity received: $signUpRequest")
      val SignUpEntity(username, password, name, lastName, email) = signUpRequest
      println(s"WebServer - SingUpRequest received for user $username")
      val signUpFutureResult: CancelableFuture[SignUpResult] = UserAuthenticator
        .signUp(username, password, name, lastName, email)
        .runAsync
      //TODO Makes no sense, is there no onSuccess( StatusCode()) ?
      IO.fromFuture(IO(signUpFutureResult)).unsafeRunSync() match {
        case SignUpResult.CREATED => Created("UserAuthenticated")
        case SignUpResult.REJECTED => BadRequest("Bad user specifications")
        case SignUpResult.ALREADY_EXISTED => Ok("Username already existed")
        case _ => InternalServerError("")
      }
    }

    case req @ POST -> Root / "signIn" => {
      val logInRequestEntity: SignInEntity = req.as[SignInEntity].unsafeRunSync()
      println(s"LogIn entity received: $logInRequestEntity")
      val SignInEntity(username, password) = logInRequestEntity
      println(s"WebServer - User log in request received for user $username")
      val signInFutureResult: CancelableFuture[SignInResult] = UserAuthenticator.signIn(username, password).runAsync
      (IO.fromFuture(IO(signInFutureResult))).unsafeRunSync() match {
        case SignInResult.AUTHENTICATED => Accepted("UserAuthenticated")
        case SignInResult.REJECTED => Forbidden("Bad authentication, user rejected ")
        case SignInResult.USER_NOT_EXISTS => NotFound("Username does not exist")
      }
    }
  }
}

object UserAuthRoutes {
  case class SignInEntity(username: String, password: String)
  case class SignUpEntity(userName: String, password: String, name: String, lastName: String, email: String)
}
