package it.ldsoftware.webfleet.driver.service.impl

import java.time.Duration

import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.util.Timeout
import it.ldsoftware.webfleet.driver.actors.BranchContent._
import it.ldsoftware.webfleet.driver.actors.RootContent._
import it.ldsoftware.webfleet.driver.actors.model.{CreationForm, EditingForm, WebContent}
import it.ldsoftware.webfleet.driver.security.User
import it.ldsoftware.webfleet.driver.service.ContentService
import it.ldsoftware.webfleet.driver.service.impl.util.EntityProvider
import it.ldsoftware.webfleet.driver.service.model._

import scala.concurrent.{ExecutionContext, Future}

class ActorContentService(
    askTimeout: Duration,
    root: EntityRef[RootCommand],
    branches: EntityProvider[BranchCommand]
)(implicit ec: ExecutionContext)
    extends ContentService {

  implicit val timeout: Timeout = Timeout.create(askTimeout)

  override def getContent(path: String): Future[ServiceResult[WebContent]] = path match {
    case "/" =>
      root
        .ask[RootResponse](GetRootInfo)
        .map {
          case RootContentResponse(webContent) => success(webContent)
          case _                               => unexpectedMessage
        }
    case _ =>
      branches
        .get(path)
        .ask[BranchResponse](GetBranchInfo)
        .map {
          case BranchContentResponse(webContent) => success(webContent)
          case BranchNotFound                    => notFound(path)
          case _                                 => unexpectedMessage
        }
  }

  override def createContent(
      parentPath: String,
      form: CreationForm,
      user: User
  ): Future[ServiceResult[String]] = parentPath match {
    case "/" =>
      root
        .ask[RootResponse](AddRootChild(form, user, _))
        .map {
          case RootDone                    => success(form.path)
          case InvalidForm(errs)           => invalid(errs)
          case UnexpectedRootFailure(ex)   => unexpectedError(ex, "Error while creating content")
          case InsufficientRootPermissions => forbidden
          case _                           => unexpectedMessage
        }
    case _ =>
      branches
        .get(parentPath)
        .ask[BranchResponse](AddBranchContent(form, user, _))
        .map {
          case BranchDone                    => success(form.path)
          case BranchNotFound                => notFound(parentPath)
          case InvalidBranchForm(errs)       => invalid(errs)
          case UnexpectedBranchFailure(ex)   => unexpectedError(ex, "Error while creating content")
          case InsufficientBranchPermissions => forbidden
          case _                             => unexpectedMessage
        }
  }

  override def editContent(
      path: String,
      form: EditingForm,
      user: User
  ): Future[ServiceResult[NoResult]] = path match {
    case "/" =>
      root
        .ask[RootResponse](EditRootContent(form, user, _))
        .map {
          case RootDone                    => noOutput
          case InvalidForm(errs)           => invalid(errs)
          case UnexpectedRootFailure(ex)   => unexpectedError(ex, "Error while updating root")
          case InsufficientRootPermissions => forbidden
          case _                           => unexpectedMessage
        }
    case _ =>
      branches
        .get(path)
        .ask[BranchResponse](EditBranchContent(form, user, _))
        .map {
          case BranchDone                    => noOutput
          case BranchNotFound                => notFound(path)
          case InvalidBranchForm(errs)       => invalid(errs)
          case UnexpectedBranchFailure(ex)   => unexpectedError(ex, "Error while creating branch")
          case InsufficientBranchPermissions => forbidden
          case _                             => unexpectedMessage
        }
  }

  override def deleteContent(path: String, user: User): Future[ServiceResult[NoResult]] =
    branches
      .get(path)
      .ask[BranchResponse](DeleteBranch(user, _))
      .map {
        case BranchDone                    => noOutput
        case BranchNotFound                => notFound(path)
        case InsufficientBranchPermissions => forbidden
        case _                             => unexpectedMessage
      }

  private def unexpectedMessage[T]: ServiceResult[T] =
    unexpectedError(new Error(), "Unexpected response from actor")
}
