package controllers

import javax.inject._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.i18n._
import play.api.Logger

import models._

case class DirNum(num_adh: String)
case class Reponse(reponse: String)

/**
 * This controller creates an `Sessioned` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(storage: DirecteurRepository, cc: ControllerComponents, actionBuilder: DefaultActionBuilder, langs: Langs) extends BaseController {

  val logger: Logger = Logger("main")

  override def controllerComponents = cc

  implicit val lang: Lang = langs.availables.head
  implicit val messages: Messages = MessagesImpl(lang, messagesApi)

  val repForm = Form(
    mapping(
      "reponse" -> text
      )(Reponse.apply)(Reponse.unapply)
    )

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
    Sessioned(controllerComponents.parsers.defaultBodyParser)(f)
  }

  def Sessioned[A](p: BodyParser[A])(f: SessionedRequest[A] => Result): Action[A] = actionBuilder(p) { request =>
    extractSessionedRequest(request)
      .fold(Redirect(routes.HomeController.loginPage).withNewSession)(f(_))
  }

  def loginPage() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login(dirForm))
  }

  def doLogin() = Action { implicit request: Request[AnyContent] =>
    dirForm.bindFromRequest.fold(formWithErrors => {
      BadRequest(views.html.login(formWithErrors))
    },
    dirData => {
      Redirect(routes.HomeController.index).withSession("num_adh" -> dirData.num_adh)
    })
  }

  /**
   * Create an Sessioned to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Sessioned { implicit request =>
    Ok(views.html.index(request.dir))
  }

  def page_enigme_a() = Sessioned { implicit request =>
    Ok(views.html.enigme_a(request.dir, repForm))
  }

  def page_enigme_b() = Sessioned { implicit request =>
    Ok(views.html.enigme_b(request.dir, repForm))
  }

  def page_enigme_c() = Sessioned { implicit request =>
    Ok(views.html.enigme_c(request.dir, repForm))
  }

  def page_enigme_d() = Sessioned { implicit request =>
    Ok(views.html.enigme_d(request.dir, repForm))
  }

  def page_enigme_e() = Sessioned { implicit request =>
    Ok(views.html.enigme_e(request.dir, repForm))
  }

  def upload_enigme(enigme: String, p: (Directeur, Form[Reponse]) => play.twirl.api.Html, r: play.api.mvc.Call) = Sessioned { implicit request =>
    repForm.bindFromRequest.fold(formWithErrors => {
      BadRequest(p(request.dir, formWithErrors))
    },
    repData => {
      storage.setEnigmeResponse(request.dir.num_adh, enigme, repData.reponse)
      Redirect(r)
    })
  }

  def upload_enigme_a() = upload_enigme("rep_enigme_a", (d, f) => views.html.enigme_a(d, f), routes.HomeController.page_enigme_a)

  def upload_enigme_b() = upload_enigme("rep_enigme_b", (d, f) => views.html.enigme_b(d, f), routes.HomeController.page_enigme_b)

  def upload_enigme_c() = upload_enigme("rep_enigme_c", (d, f) => views.html.enigme_c(d, f), routes.HomeController.page_enigme_c)

  def upload_enigme_d() = upload_enigme("rep_enigme_d", (d, f) => views.html.enigme_d(d, f), routes.HomeController.page_enigme_d)

  def upload_enigme_e() = upload_enigme("rep_enigme_e", (d, f) => views.html.enigme_e(d, f), routes.HomeController.page_enigme_e)

  def validate_enigme(num_adh: String, enigme: String) = Sessioned { implicit request =>
      storage.setEnigmeResponseOk(num_adh, enigme)
      Redirect(routes.HomeController.listResults)
  }

  def listResults = Sessioned { implicit request =>
    val dirs: List[Directeur] = storage.getAll
    Ok(views.html.list_results(request.dir, dirs))
  }
}
