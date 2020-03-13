package controllers

import javax.inject._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.i18n._

import models._

case class DirNum(num_adh: String)

/**
 * This controller creates an `Sessioned` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(storage: DirecteurRepository, cc: ControllerComponents, actionBuilder: DefaultActionBuilder, langs: Langs, implicit val messagesApi: MessagesApi) extends BaseController {

  implicit val lang: Lang = langs.availables.head
  implicit val messages: Messages = MessagesImpl(lang, messagesApi)


  val dirForm = Form(
    mapping(
      "num_adh" -> text
      )(DirNum.apply)(DirNum.unapply).verifying(
        "Je ne te trouve pas 😭",
        fields => fields match {
          case num => storage.getDirecteur(num.num_adh).isDefined
        })
      )

  case class SessionedRequest[A](dir: Directeur, private val request: Request[A]) extends WrappedRequest(request)

  private def extractSessionedRequest[A](request: Request[A]): Option[SessionedRequest[A]] = for {
    num <- request.session.get("num_adh")
    dir <- storage.getDirecteur(num)
  } yield SessionedRequest(dir, request)


  def Sessioned(f: SessionedRequest[AnyContent] => Result): Action[AnyContent]  = {
    Sessioned(cc.parsers.defaultBodyParser)(f)
  }

  def Sessioned[A](p: BodyParser[A])(f: SessionedRequest[A] => Result): Action[A] = actionBuilder(p) { request =>
    extractSessionedRequest(request)
      .fold(Redirect(routes.HomeController.loginPage).withNewSession)(f(_))
  }

  def loginPage() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login(dirForm))
  }

  def doLogin() = Action { implicit request: Request[AnyContent] =>
    val num_adh = request.body.asText.getOrElse("")
    storage.getDirecteur(num_adh) match {
      case Some(dir) => Redirect(routes.HomeController.index).withSession("num_adh" -> num_adh)
      case None      => Redirect(routes.HomeController.loginPage).flashing("msg" -> )
    }
  }

  /**
   * Create an Sessioned to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Sessioned { implicit request =>

    Ok(views.html.index())
  }

  def page_enigme_a() = Sessioned { implicit request =>
    Ok(views.html.enigme_a())
  }

  def upload_enigme_a() = Sessioned(parse.tolerantText) { implicit request =>
    val txt = request.body
    storage.setEnigmeResponse(request.dir.num_adh, "rep_enigme_a", txt)
    Ok(views.html.continuez())
  }

  def page_enigme_b() = Sessioned { implicit request =>
    Ok(views.html.enigme_b())
  }

  def upload_enigme_b() = Sessioned(parse.tolerantText) { implicit request =>
    val txt = request.body
    storage.setEnigmeResponse(request.dir.num_adh, "rep_enigme_b", txt)
    Ok(views.html.continuez())
  }

  def page_enigme_c() = Sessioned { implicit request =>
    Ok(views.html.enigme_c())
  }

  def upload_enigme_c() = Sessioned(parse.tolerantText) { implicit request =>
    val txt = request.body
    storage.setEnigmeResponse(request.dir.num_adh, "rep_enigme_c", txt)
    Ok(views.html.continuez())
  }


  def page_enigme_d() = Sessioned { implicit request =>
    Ok(views.html.enigme_d())
  }

  def upload_enigme_d() = Sessioned(parse.tolerantText) { implicit request =>
    val txt = request.body
    storage.setEnigmeResponse(request.dir.num_adh, "rep_enigme_d", txt)
    Ok(views.html.continuez())
  }


  def page_enigme_e() = Sessioned { implicit request =>
    Ok(views.html.enigme_e())
  }

  def upload_enigme_e() = Sessioned(parse.tolerantText) { implicit request =>
    val txt = request.body
    storage.setEnigmeResponse(request.dir.num_adh, "rep_enigme_e", txt)
    Ok(views.html.continuez())
  }

}
