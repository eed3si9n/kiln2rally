/*
 * Copyright (c) 2011 The Burgiss Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package kiln2rally

import net.lag.configgy.Config
import java.net.URI
import net.liftweb.json.JsonAST._

case class RallyApi(config: Config) extends DefectProcessor with StoryProcessor with TaskProcessor {
  /** @params line, commit **/
  val processCommitLine = processDefects orElse processStories orElse  processTasks orElse processNone
}

// https://rally1.rallydev.com/slm/webservice/1.23/defect.js?pagesize=1&query=(FormattedID%20=%20%22DE261%22)&fetch=true
trait DefectProcessor extends RallyBase {
  import net.liftweb.json.JsonDSL._

  case class DefectRef(ref: URI) extends RallyRef
  def defectRef(name: String) = objectRef[DefectRef]("defect.js", DefectRef.apply, formattedIdEq(name))

  private val prefix = config.getString("defect_prefix", "DE")
  private val states = config.getList("defect_states") match {
    case Nil => List("Fixed")
    case xs  => xs
  }
  private val DefectState = """%s([0-9]+) (%s)( in [a-zA-Z0-9._-]+)?( for [a-zA-Z@-_]+)?""".format(
    prefix, states.mkString("|")).r
  private val BadDefect   = """%s([0-9]+).*""".format(prefix).r

  val processDefects: (String, List[JField]) =>? Option[String] = {
    case (DefectState(id, state, fixedIn, owner), commit) =>
      defectRef(prefix + id) flatMap { ref =>
        ref add Discussion(commit)
        ref update ("Defect" ->
          ("State" -> state) ~
          ("FixedInBuild" -> (Option(fixedIn) map {_ drop 4})) ~
          ("Owner" -> (Option(owner) flatMap {findUser})) )
      }

    case (line@BadDefect(id), _) => Some(line)
  }
}

// https://rally1.rallydev.com/slm/webservice/1.23/task.js?pagesize=1&query=(FormattedID%20=%20%22TA3775%22)&fetch=true
trait TaskProcessor extends RallyBase {
  import net.liftweb.json.JsonDSL._

  case class TaskRef(ref: URI) extends RallyRef
  def taskRef(name: String) = objectRef[TaskRef]("task.js", TaskRef.apply, formattedIdEq(name))

  private val prefix = config.getString("task_prefix", "TA")
  private val states = config.getList("task_states") match {
    case Nil => List("Completed")
    case xs  => xs
  }
  private val TaskState   = """%s([0-9]+) (%s)( in [0-9]*\.?[0-9]*h)?( for [a-zA-Z@-_]+)?""".format(
    prefix, states.mkString("|")).r
  private val BadTask     = """%s([0-9]+).*""".format(prefix).r

  val processTasks: (String, List[JField]) =>? Option[String] = {
    case (TaskState(id, state, actuals, owner), commit) =>
      taskRef(prefix + id) flatMap { ref =>
        ref add Discussion(commit)
        ref update ("Task" ->
            ("State" -> state) ~
            ("Actuals" -> (Option(actuals) map {_ drop 4 dropRight 1})) ~
            ("Owner" -> (Option(owner) flatMap {findUser})))
      }

    case (line@BadTask(id), _) => Some(line)
  }
}

// https://rally1.rallydev.com/slm/webservice/1.23/hierarchicalRequirement.js?pagesize=1&query=(FormattedID%20=%20%22US1604%22)&fetch=true
trait StoryProcessor extends RallyBase {
  import net.liftweb.json.JsonDSL._

  case class StoryRef(ref: URI) extends RallyRef
  def storyRef(name: String) = objectRef[StoryRef]("hierarchicalRequirement.js", StoryRef.apply, formattedIdEq(name))

  private val prefix = config.getString("story_prefix", "US")
  private val states = config.getList("story_states") match {
    case Nil => List("Completed")
    case xs  => xs
  }
  private val StoryState  = """%s([0-9]+) (%s)( in [a-zA-Z0-9._-]+)?( for [a-zA-Z@-_]+)?""".format(
    prefix, states.mkString("|")).r
  private val BadStory    = """%s([0-9]+).*""".format(prefix).r

  val processStories: (String, List[JField]) =>? Option[String] = {
    case (StoryState(id, state, fixedIn, owner), commit) =>
      storyRef(prefix + id) flatMap { ref =>
        ref add Discussion(commit)
        ref update ("HierarchicalRequirement" ->
          ("ScheduleState" -> state) ~
          ("AddressedIn" -> (Option(fixedIn) map {_ drop 4})) ~
          ("Owner" -> (Option(owner) flatMap {findUser})))
      }

    case (line@BadStory(id), _) => Some(line)
  }
}

// https://rally1.rallydev.com/slm/doc/webservice/
trait RallyBase {
  type =>?[A, B] = PartialFunction[A, B]
  import dispatch._
  import dispatch.liftjson.Js._
  import net.liftweb.json.JsonAST._

  val config: Config

  val log = net.lag.logging.Logger.get
  val http = new Http {
    override lazy val log: dispatch.Logger = new dispatch.Logger {
      def info(msg: String, items: Any*) {
        net.lag.logging.Logger.get.info(msg, items: _*)
      }
    }
  }

  /** represents a Rally reference **/
  trait RallyRef {
    import net.liftweb.json.compact

    val ref: URI

    def update(json: => JObject): Option[String] = {
      val s = compact(render(json))
      log.info("update %s" format (s))
      val req = url(ref.toString) as (userName, password)
      http(req.secure << s as_str)
      None
    }

    def add(discussion: Discussion): Option[String] = {
      val s = compact(render(discussion json this))
      log.info("addDiscussion %s" format (s))
      val req = url(baseAddress) / "conversionpost" / "create.js" as (userName, password)
      http(req.secure << s as_str)
      None
    }
  }

  // https://rally1.rallydev.com/slm/webservice/1.23/conversationpost/3473610150.js?fetch=true
  case class Discussion(commit: JObject) {
    import net.liftweb.json.compact

    def modified = commit transform {
      case JField("url", JString(s)) => JField("url", JString("<a href='%s'>%s</a>".format(s, s)))
    }

    def html: String = """\\n""".r.replaceAllIn(compact(render(modified)), "<br>")

    def json(artifact: RallyRef): JObject = {
      import net.liftweb.json.JsonDSL._
      ("ConversationPost" ->
        ("Artifact" -> artifact.ref.toString) ~
        ("Text" -> html))
    }
  }

  // https://rally1.rallydev.com/slm/webservice/1.23/user.js?query=(DisplayName%20contains%20Richard)&fetch=true
  def findUser(forWhom: String): Option[String] = userRef(forWhom drop 5) map {_.ref.toString}
  case class UserRef(ref: URI) extends RallyRef
  def userRef(name: String) = objectRef[UserRef]("user.js", UserRef.apply, displayNameContains(name))

  val processNone: (String, List[JField]) =>? Option[String] = { case _ => None }

  def objectRef[A <: RallyRef](fileName: String, factory: URI => A, query: String): Option[A] = {
    val req = url(baseAddress) / fileName as (userName, password)
    http(req.secure <<? Map("query" -> query) ># { js =>
      for {
        JArray(results) <- js \ "QueryResult" \ "Results"
        item <- results
        JString(ref) <- item \ "_ref"
      } yield factory(new URI(ref.toString))
    }).headOption
  }

  def formattedIdEq(name: String) = """(FormattedID = "%s")""".format(name)
  def displayNameContains(name: String) = """(DisplayName contains "%s")""".format(name)

  val baseAddress = config.getString("base_address", "http://rally1.rallydev.com/slm/webservice/1.23")
  val userName = config.getString("user_name") getOrElse {"user_name is missing."}
  val password = config.getString("password") getOrElse {"password is missing."}
}
