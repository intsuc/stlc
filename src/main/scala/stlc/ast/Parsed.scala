package stlc.ast

final case class Parsed(exp: Parsed.Exp)

object Parsed:
  enum Typ:
    case Bool
    case Fun(domain: Typ, codomain: Typ)

  enum Exp:
    case Var(name: String)
    case True
    case False
    case If(antecedent: Exp, consequent: Exp, alternative: Exp)
    case Abs(parameter: String, body: Exp)
    case App(operator: Exp, operand: Exp)
    case Anno(target: Exp, annotation: Typ)
