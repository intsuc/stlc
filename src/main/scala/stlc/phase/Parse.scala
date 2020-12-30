package stlc.phase

import stlc.CompileException
import stlc.ast.{Parsed => P}
import scala.util.parsing.combinator._

object Parse extends (String => P):
  def apply(source: String): P =
    import StlcParsers._

    parseAll(exp, source) match
    case Success(matched, _) => P(matched)
    case Failure(message, _) => throw CompileException(message)
    case Error(message, _) => throw CompileException(message)

  object StlcParsers extends RegexParsers with PackratParsers:
    import P._

    lazy val typ: PackratParser[Typ]
      = typ ~ "→" ~ typ ^^ { case domain ~ _ ~ codomain => Typ.Fun(domain, codomain) }
      | "bool" ^^ { _ => Typ.Bool }
      | "(" ~> typ <~ ")"

    lazy val exp: PackratParser[Exp]
      = "if" ~ exp ~ "then" ~ exp ~ "else" ~ exp ^^ { case _ ~ antecedent ~ _ ~ consequent ~ _ ~ alternative => Exp.If(antecedent, consequent, alternative) }
      | exp1

    lazy val exp1: PackratParser[Exp]
      = name ~ "→" ~ exp1 ^^ { case parameter ~ _ ~ body => Exp.Abs(parameter, body) }
      | exp2

    lazy val exp2: PackratParser[Exp]
      = exp2 ~ exp3 ^^ { Exp.App(_, _) }
      | exp3

    lazy val exp3: PackratParser[Exp]
      = exp3 ~ ":" ~ typ ^^ { case target ~ _ ~ annotation => Exp.Anno(target, annotation) }
      | exp4

    lazy val exp4: PackratParser[Exp]
      = "true" ^^ { _ => Exp.True }
      | "false" ^^ { _ => Exp.False }
      | name ^^ { Exp.Var(_) }
      | "(" ~> exp <~ ")"

    def keyword: Parser[String] = "bool" | "if" | "then" | "else" | "true" | "false"

    def name: Parser[String] = not(keyword) ~> """[a-z0-9_.-]+""".r
