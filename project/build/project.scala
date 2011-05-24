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

import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) with assembly.AssemblyBuilder {
  val uf_vers = "0.3.3"
  val uf_filter = "net.databinder" %% "unfiltered-filter" % uf_vers
  val uf_jetty  = "net.databinder" %% "unfiltered-jetty" % uf_vers
  val uf_json   = "net.databinder" %% "unfiltered-json" % uf_vers

  val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()
  val d_vers = "0.8.1"
  val dispatch_http = "net.databinder" %% "dispatch-http" % d_vers
  val dispatch_json = "net.databinder" %% "dispatch-lift-json" % d_vers
  val time = "org.scala-tools.time" %% "time" % "0.3"

  val specs2 = "org.specs2" %% "specs2" % "1.3"
  def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
  override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)

  val snapshots = "Scala Tools Snapshots" at "http://www.scala-tools.org/repo-snapshots/"
  val scalaToolsNexus  = "Scala Tools Nexus Releases" at "http://nexus.scala-tools.org/content/repositories/releases/"
}
