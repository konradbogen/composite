# Obsidian → LilyPond Builder

_A compositional music-notation pipeline based on design patterns and formal language theory_

## Overview

This project implements a **compositional system for musical notation** that translates **Obsidian Markdown** into **LilyPond** code. The system is built using classic **object-oriented design patterns** and is formally interpretable as a **regular-language constructor** over musical symbols.

Rather than treating LilyPond as a flat output format, the project models music as a **structured language**, assembled from smaller expressions using algebraic operators.

---

## Architectural Foundations

The implementation follows the **Composite Pattern** (Gamma et al., 1994), enabling recursive construction of complex musical forms from simple components.

Every musical element implements the interface:

```java
public interface Component {
    String print();
}
```

This ensures **referential transparency**: each component maps deterministically to a LilyPond string.

---

## Core Structural Classes

### `Composite` — Concatenation

Represents sequential composition of musical material.

Formal analogue:

```
AB
```

### `VerticalComposite` — Parallel Composition / Choice

Represents simultaneous musical structures (e.g. multiple staves).

Formal analogue:

```
A | B
```

### `Repeat` — Iteration

Represents repeated musical material.

Formal analogue:

```
A*
```

---

## Theory

### 1. Music as a Formal Language

In formal language theory, a language **L** is defined over an alphabet **Σ** as a set of strings:

```
L ⊆ Σ*
```

In this project:

- **Alphabet (Σ)**
  Primitive LilyPond tokens (notes, rests, chords, commands)

- **Strings**
  Valid LilyPond source files

- **Language (L)**
  The set of all LilyPond scores constructible from Obsidian input

Each `Component` corresponds to a **language fragment**, and `print()` computes a concrete string in **Σ\***.

---

### 2. Algebra of Musical Expressions

The system defines a small **algebra of musical expressions**:

| Operator      | Class               | Meaning                       |
| ------------- | ------------------- | ----------------------------- |
| Concatenation | `Composite`         | Sequential music              |
| Alternation   | `VerticalComposite` | Simultaneous / parallel music |
| Kleene Star   | `Repeat`            | Repetition                    |

These operators are **closed** over the set of components, allowing arbitrary nesting.

Formally, if `A` and `B` are components producing languages `LA` and `LB`:

- **Concatenation**

  ```
  L(Composite(A, B)) = { xy | x ∈ LA, y ∈ LB }
  ```

- **Alternation**

  ```
  L(VerticalComposite(A, B)) = LA ∪ LB
  ```

- **Repetition**

  ```
  L(Repeat(A)) = LA*
  ```

This establishes the system as a **regular language generator**.

---

### 3. Relation to Regular Expressions

The three fundamental operations of regular expressions are:

1. **Concatenation**
2. **Choice (`|`)**
3. **Kleene star (`*`)**

These are mapped _directly_ onto concrete classes:

| Regex Operator | Class               |
| -------------- | ------------------- |
| `AB`           | `Composite`         |
| `A+B`          | `VerticalComposite` |
| `A*`           | `Repeat`            |

Thus, the component tree is isomorphic to a **regular expression syntax tree**.

---

### 4. Denotational Semantics

Each component implements a denotation function:

```
⟦ Component ⟧ → String
```

Composition is homomorphic:

```
⟦ Composite(A, B) ⟧ = ⟦A⟧ · ⟦B⟧
⟦ VerticalComposite(A, B) ⟧ = "<< " ⟦A⟧ ⟦B⟧ " >>"
⟦ Repeat(A) ⟧ = ⟦A⟧ repeated n times
```

This ensures that **structural composition is preserved during rendering**, a key property of denotational semantics.

---

### 5. Markdown as a Domain-Specific Language

Obsidian Markdown is treated as a **surface syntax** for the formal language.

| Markdown Construct | Semantic Meaning              |     |     |
| ------------------ | ----------------------------- | --- | --- |
| `[[file]]`         | Component inclusion           |     |     |
| `# Heading`        | Vertical composition boundary |     |     |
| `*` suffix         | Kleene star                   |     |     |
| `?`                | Layout control                |     |     |

Thus, Markdown becomes a **DSL (Domain-Specific Language)** whose semantics are defined by the component algebra.

### 6. Design Patterns as Grammar Operators

The project demonstrates a strong correspondence between **object-oriented design patterns** and **formal grammar operators**:

| Pattern     | Grammar Role    |
| ----------- | --------------- |
| Composite   | Production rule |
| Decorator   | Unary operator  |
| Builder     | Parser          |
| Interpreter | Renderer        |

This supports the broader thesis that **design patterns encode grammatical structure**.

---

## Implications

- Music notation can be modeled as a **formal language**
- Design patterns provide a **syntax tree API**
- Markdown can function as a **musical programming language**
- LilyPond is a **target language**, not an authoring language

---

## References

- Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994).
  _Design Patterns: Elements of Reusable Object-Oriented Software._
  Addison-Wesley.

- Hopcroft, J. E., Motwani, R., & Ullman, J. D. (2006).
  _Introduction to Automata Theory, Languages, and Computation._
  Pearson.

- LilyPond Music Notation Project
  [https://lilypond.org](https://lilypond.org)

---
