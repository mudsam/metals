package tests

import scala.concurrent.Future

abstract class BaseCompletionSlowSuite(name: String)
    extends BaseSlowSuite(name) {
  def assertCompletion(
      query: String,
      expected: String,
      project: Char = 'a'
  )(implicit file: sourcecode.File, line: sourcecode.Line): Future[Unit] = {
    val filename = s"$project/src/main/scala/$project/${project.toUpper}.scala"
    server.completion(filename, query).map { completion =>
      assertNoDiff(completion, expected)
    }
  }

  def basicTest(scalaVersion: String): Future[Unit] = {
    cleanWorkspace()
    for {
      _ <- server.initialize(
        s"""/metals.json
           |{
           |  "a": { "scalaVersion": "${scalaVersion}" }
           |}
           |/a/src/main/scala/a/A.scala
           |package a
           |object A {
           |  val x = "".substrin
           |  Stream
           |  TrieMap
           |  locally {
           |    val myLocalVariable = Array("")
           |    myLocalVariable
           |    val source = ""
           |  }
           |}
           |""".stripMargin
      )
      _ <- server.didOpen("a/src/main/scala/a/A.scala")
      _ = assertNotEmpty(client.workspaceDiagnostics)
      _ <- assertCompletion(
        "substrin@@",
        """|substring(beginIndex: Int): String
           |substring(beginIndex: Int, endIndex: Int): String
           |""".stripMargin
      )
      _ <- assertCompletion(
        "Stream@@",
        """|Stream scala.collection.immutable
           |java.util.stream.Stream java.util.stream
           |java.util.stream.IntStream java.util.stream
           |java.rmi.server.LogStream java.rmi.server
           |java.util.stream.BaseStream java.util.stream
           |java.util.stream.LongStream java.util.stream
           |scala.collection.immutable.StreamView scala.collection.immutable
           |java.io.InputStream java.io
           |java.io.PrintStream java.io
           |java.util.stream.DoubleStream java.util.stream
           |java.io.OutputStream java.io
           |scala.collection.immutable.Stream.StreamBuilder scala.collection.immutable.Stream
           |java.util.logging.StreamHandler java.util.logging
           |scala.collection.immutable.Stream.StreamCanBuildFrom scala.collection.immutable.Stream
           |""".stripMargin
      )
      _ <- assertCompletion(
        "TrieMap@@",
        """|scala.collection.concurrent.TrieMap scala.collection.concurrent
           |scala.collection.parallel.mutable.ParTrieMap scala.collection.parallel.mutable
           |scala.collection.immutable.HashMap.HashTrieMap scala.collection.immutable.HashMap
           |scala.collection.parallel.mutable.ParTrieMapCombiner scala.collection.parallel.mutable
           |scala.collection.parallel.mutable.ParTrieMapSplitter scala.collection.parallel.mutable
           |scala.collection.concurrent.TrieMapSerializationEnd scala.collection.concurrent
           |""".stripMargin
      )
      _ <- assertCompletion(
        "  myLocalVariable@@",
        """|myLocalVariable: Array[String]
           |""".stripMargin
      )
    } yield ()
  }
}
