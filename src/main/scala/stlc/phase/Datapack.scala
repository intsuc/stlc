package stlc.phase

import stlc.ast.{Defunctionalized => D, Sym}
import java.nio.file.{Files, Path, Paths}
import java.util.zip.{ZipEntry, ZipOutputStream}
import scala.util.Using

object Datapack extends (D => (Path => Unit)):
  def apply(defunctionalized: D): Path => Unit =
    path => Using(ZipOutputStream(Files.newOutputStream(path))) { out =>
      out.write("pack.mcmeta", Seq("""{"pack": {"description": "", "pack_format": 7}}"""))

      out.write("data/minecraft/functions/apply.mcfunction",
        "data modify storage _ _ set from storage _ stack[-2]._" +:
        (defunctionalized.ctx.map((fun, _, _) => s"execute if data storage _ {_: ${fun.id}} run function ${fun.id}")) :+
        "data remove storage _ stack[-2]" :+
        "data remove storage _ stack[-2]"
      )

      defunctionalized.ctx foreach { (fun, ctx, exp) =>
        visit(exp)(using fun, ctx) foreach { (fun, cmds) =>
          out.write(s"data/minecraft/functions/${fun.id}.mcfunction", cmds :+ s"data modify storage _ _ set value ${fun.id}")
        }
      }

      out.write("data/minecraft/functions/preload.mcfunction", Seq("data modify storage _ stack set value []"))
      val load = Sym.fresh()
      visit(defunctionalized.exp)(using load, Set.empty) foreach { (fun, cmds) =>
        out.write(s"data/minecraft/functions/${fun.id}.mcfunction", cmds)
      }
      out.write("data/minecraft/tags/functions/load.json", Seq(s"""{"values": ["preload", "${load.id}"]}"""))
    }

  private def visit(exp: D.Exp)(using fun: Sym, ctx: Set[Sym]): Map[Sym, Seq[String]] =
    exp match

    // Var-Data
    case D.Exp.Var(name) if ctx contains name =>
      Map(fun -> Seq(s"data modify storage _ stack append from storage _ stack[-2].${name.id}"))

    // Var-Arg
    case D.Exp.Var(name) =>
      Map(fun -> Seq("data modify storage _ stack append from storage _ stack[-1]"))

    // True
    case D.Exp.True =>
      Map(fun -> Seq("data modify storage _ stack append value {_: true}"))

    // False
    case D.Exp.False =>
      Map(fun -> Seq("data modify storage _ stack append value {_: false}"))

    // If
    case D.Exp.If(antecedent, consequent, alternative) =>
      val fun1 = Sym.fresh()
      val fun2 = Sym.fresh()

      val antecedent1 = visit(antecedent)
      val consequent1 = visit(consequent)(using fun1)
      val alternative1 = visit(alternative)(using fun2)

      antecedent1 ++
      consequent1 ++
      alternative1 +
      (fun -> (antecedent1(fun) ++ Seq(
        "data modify storage _ _ set from storage _ stack[-1]._",
        "data remove storage _ stack[-1]",
        s"execute if data storage _ {_: true} run function ${fun1.id}",
        s"execute if data storage _ {_: false} run function ${fun2.id}"
      ))) +
      (fun1 -> (consequent1(fun1) :+ "data modify storage _ _ set value true"))

    // Clos
    case D.Exp.Clos(tag, data) =>
      Map(fun -> (
        s"data modify storage _ stack append value {_: ${tag.id}}" +:
        data.map(datum =>
          if ctx contains datum then
            s"data modify storage _ stack[-1].${datum.id} set from storage _ stack[-3].${datum.id}"
          else
            s"data modify storage _ stack[-1].${datum.id} set from storage _ stack[-2]"
        ).toSeq
      ))

    // Call
    case D.Exp.Call(operator, operand) =>
      val operator1 = visit(operator)
      val operand1 = visit(operand)

      operator1 ++
      operand1 +
      (fun -> (operator1(fun) ++ operand1(fun) :+ "function apply"))

  extension (out: ZipOutputStream) private def write(name: String, lines: Seq[String]): Unit =
    out.putNextEntry(ZipEntry(name))
    lines foreach { line =>
      out.write(line.getBytes)
      out.write('\n')
    }
    out.closeEntry()
