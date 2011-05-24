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

import org.specs2.mutable._
import dispatch._
import unfiltered.jetty.{Server}

trait HostedSpec extends Specification {
  lazy val host = :/("localhost", port)
  lazy val port = 8080
  def startServer: org.specs2.specification.Step
  def stopServer: org.specs2.specification.Step
}

trait JettyServedSpec extends HostedSpec {
  lazy val server = setup(unfiltered.jetty.Http(port))

  def setup: (Server => Server)

  def startServer = step {
    server.start
  }

  def stopServer = step {
    server.stop
  }
}
