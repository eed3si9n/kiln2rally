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

trait KilnPlanSpec extends HostedSpec {
  import dispatch._

  startServer

//  "post works" in {
//    val http = new Http with NoLogging
//    val response = http(host << Map("payload" -> payload) as_str)
//    response must_== ""
//  }

  stopServer

  val payload = """{
  "commits": [
    {
      "author": "eugene <eugene@example.com>",
      "branch": "default",
      "id": "16265aef917cfe137e250c4f71a4a77ff4ad0974",
      "message": "DE2319 Fixed in 2011.05.21 for Eugene\nUS1608 In-Progress in 2011.05.20 for Eugene\nTA3775 In-Progress in 3h for Eugene",
      "revision": 14,
      "timestamp": "3\/23\/2010 3:42:42 PM",
      "url": "http:\/\/kamens.kilnhg.com\/Repo\/Personal\/Playground\/DNForever\/History\/16265aef917cfe137e250c4f71a4a77ff4ad0974",
      "tags": [
        "tip"
      ]
    },
    {
      "author": "eugene <eugene@example.com>",
      "branch": "default",
      "id": "a077d19afed224dc7c46dc535d5f7e90546ed5bd",
      "message": "This version is now shippable",
      "revision": 15,
      "timestamp": "3\/23\/2010 3:42:46 PM",
      "url": "http:\/\/kamens.kilnhg.com\/Repo\/Personal\/Playground\/DNForever\/History\/a077d19afed224dc7c46dc535d5f7e90546ed5bd",
      "tags": []
    }
  ],
  "pusher": {
    "email": "eugene@example.com",
    "fullName": "eugene"
  },
  "repository": {
    "central": true,
    "description": "Picking up where the others left off",
    "id": 1,
    "name": "DNForever",
    "url": "http:\/\/kamens.kilnhg.com\/Repo\/Personal\/Playground\/DNForever"
  }
}"""
}

object KilnPlanSpecJettyServed extends JettyServedSpec with KilnPlanSpec {
  import unfiltered.request._
  import unfiltered.response._
  import net.lag.configgy.Configgy
  Configgy.configure("Web.config")

  def setup = {
    _.filter(new KilnPlan(Configgy.config))
  }
}
