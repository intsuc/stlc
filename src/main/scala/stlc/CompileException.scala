package stlc

final case class CompileException(message: String) extends Exception(message)
