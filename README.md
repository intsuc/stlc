# stlc

Proof of concept for simply typed lambda calculus compiler for Minecraft.

## Usage

<pre>
sbt> run <i>input_path</i> <i>output_path</i>
</pre>

## Example

```agda
((x → y → (if x then (if y then true else false) else false)) : bool → bool → bool) true false
```

## Language

### Syntax

<pre><code>      Type <i>τ</i> ::= bool
               | <i>τ</i> → <i>τ</i>

Expression <i>e</i> ::= <i>x</i>
               | true
               | false
               | if <i>e</i> then <i>e</i> else <i>e</i>
               | <i>x</i> → <i>e</i>
               | <i>e</i> <i>e</i>
               | <i>e</i> : <i>τ</i>

     Value <i>v</i> ::= true
               | false
               | <i>x</i> → <i>e</i>
</code></pre>

### Typing rules

<pre><code><i>Γ</i> ⊢ <i>e</i> : <i>τ</i>    Under context <i>Γ</i>, expression <i>e</i> has type <i>τ</i>.

──────────────── Var
<i>Γ</i>, <i>x</i> : <i>τ</i> ⊢ <i>x</i> : <i>τ</i>

──────────── True
<i>Γ</i> ⊢ true : <i>τ</i>

───────────── False
<i>Γ</i> ⊢ false : <i>τ</i>

<i>Γ</i> ⊢ <i>e<sub>1</sub></i> : bool    <i>Γ</i> ⊢ <i>e<sub>2</sub></i> : <i>τ</i>    <i>Γ</i> ⊢ <i>e<sub>3</sub></i> : <i>τ</i>
───────────────────────────────────────── If
<i>Γ</i> ⊢ if <i>e<sub>1</sub></i> then <i>e<sub>2</sub></i> else <i>e<sub>3</sub></i> : <i>τ</i>

<i>Γ</i>, <i>x</i> : <i>τ<sub>1</sub></i> ⊢ <i>e</i> : <i>τ<sub>2</sub></i>
───────────────────── Abs
<i>Γ</i> ⊢ <i>x</i> → <i>e</i> : <i>τ<sub>1</sub></i> → <i>τ<sub>2</sub></i>

<i>Γ</i> ⊢ <i>e<sub>1</sub></i> : <i>τ<sub>1</sub></i> → <i>τ<sub>2</sub></i>    <i>Γ</i> ⊢ <i>e<sub>2</sub></i> : <i>τ<sub>1</sub></i>
──────────────────────────────── App
<i>Γ</i> ⊢ <i>e<sub>1</sub></i> <i>e<sub>2</sub></i> : <i>τ<sub>2</sub></i>

<i>Γ</i> ⊢ <i>e</i> : <i>τ</i>
─────────────── Anno
<i>Γ</i> ⊢ (<i>e</i> : <i>τ</i>) : <i>τ</i>
</code></pre>

### Operational semantics

<pre><code><i>e</i> ↦ <i>e'</i>    Expression <i>e</i> evaluates to <i>e'</i>.

──────────── True
true ↦ true

────────────── False
false ↦ false

<i>e<sub>1</sub></i> ↦ <i>e<sub>1</sub>'</i>
────────────────────────────────────────────── If
if <i>e<sub>1</sub></i> then <i>e<sub>2</sub></i> else <i>e<sub>3</sub></i> ↦ if <i>e<sub>1</sub>'</i> then <i>e<sub>2</sub></i> else <i>e<sub>3</sub></i>

───────────────────────────── If-True
if true then <i>e<sub>2</sub></i> else <i>e<sub>3</sub></i> ↦ <i>e<sub>2</sub></i>

────────────────────────────── If-False
if false then <i>e<sub>2</sub></i> else <i>e<sub>3</sub></i> ↦ <i>e<sub>3</sub></i>

──────────────── Abs
<i>x</i> → <i>e</i> ↦ <i>x</i> → <i>e</i>

<i>e<sub>1</sub></i> ↦ <i>e<sub>1</sub>'</i>
─────────────── App-1
<i>e<sub>1</sub></i> <i>e<sub>2</sub></i> ↦ <i>e<sub>1</sub>'</i> <i>e<sub>2</sub></i>

<i>e<sub>2</sub></i> ↦ <i>e<sub>2</sub>'</i>
─────────────── App-2
<i>v<sub>1</sub></i> <i>e<sub>2</sub></i> ↦ <i>v<sub>1</sub></i> <i>e<sub>2</sub>'</i>

────────────────────── App
(<i>x</i> → <i>e</i>) <i>v<sub>2</sub></i> ↦ [<i>v<sub>2</sub></i>/<i>x</i>]<i>e</i>

──────────── Anno
(<i>e</i> : <i>τ</i>) ↦ <i>e</i>
</code></pre>
