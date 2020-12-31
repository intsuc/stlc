package stlc.ast

final case class Sym private (name: String, id: Int)

object Sym:
  private val Id = java.util.concurrent.atomic.AtomicInteger()

  def fresh(name: String): Sym = Sym(name, Id.getAndIncrement())
