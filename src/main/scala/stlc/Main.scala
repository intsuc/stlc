package stlc

import stlc.phase._
import java.nio.file.{Path, Paths}
import scala.io.Source
import scala.util.CommandLineParser.FromString

given FromString[Path] = Paths.get(_)

@main def compile(in: Path, out: Path): Unit =
  try
    (
      Parse andThen
      Resolve andThen
      Type andThen
      Defunctionalize andThen
      Datapack
    )(Source.fromFile(in.toFile).mkString)(out)

    println(s"[success] ${in.getFileName} → ${out.getFileName}")
  catch case CompileException(message) =>
    println(s"[error] $message")
