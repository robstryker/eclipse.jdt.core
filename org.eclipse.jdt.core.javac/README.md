# JDT over Javac

This branch is a work in progress experiment to leverage all higher-level JDT IDE features (DOM, IJavaElement, refactorings...) relying on Javac as underlying compiler/parser instead of ECJ.

Why? Some background...
* These days, with more frequent and more features Java releases, it's becoming hard for JDT to **cope with new Java features on time** and **facilitate support for upcoming/preview features before Java is released so JDT can participate to consolidation of the spec**. Over recent releases, JDT has failed at providing the features on time. This is mostly because of the difficulty of maintaining the Eclipse compiler: compilers are difficult bits of code to maintain and it takes a lot of time to implement things well in them. There is no clear sign the situation can improve here.
* The Eclipse compiler has always suffered from occasional **inconsistencies with Javac** which end-users fail at understanding. Sometimes, ECJ is right, sometimes Javac is; but for end-users and for the ecosystem, Javac is the reference implementation and it's behavior is what they perceive as the actual specification
* JDT has a very strong ecosystem (JDT-LS, plugins) a tons of nice features, so it seems profitable to **keep relying higher-level JDT APIs, such as model or DOM** to remain compatible with the ecosystem


🎯 The technical proposal here mostly to **allow Javac to be used at the lowest-level of JDT**, under the hood, to populate higher-level models that are used in many operations; named the JDT DOM and IJavaElement models. It is expected that if we can create a good DOM and IJavaElement structure with another strategy (eg using Javac API), then all higher level operations will remain working as well without modification.

▶️ **To test this**, you'll need to import the code of `org.eclipse.jdt.core` and `org.eclipse.jdt.core.javac` from this branch in your Eclipse workspace; and create a Launch Configuration of type "Eclipse Application" which does include the `org.eclipse.jdt.core` bundle. Go to _Arguments_ tab of this launch configuration, and add the following content to the _VM arguments_ list:

> `--add-opens jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED -DICompilationUnitResolver=org.eclipse.jdt.core.dom.JavacCompilationUnitResolver -DCompilationUnit.DOM_BASED_OPERATIONS=true -DCompilationUnit.codeComplete.DOM_BASED_OPERATIONS=true`

The `--add-opens` allow to access internal API of the JVM; the `ICompilationUnitResolver=org.eclipse.jdt.core.dom.JavacCompilationUnitResolver` and `CompilationUnit.DOM_BASED_OPERATIONS=true`/`-DCompilationUnit.codeComplete.DOM_BASED_OPERATIONS` system properties enables the experimental processing described below, respectfully controlling whether to build DOM using ECJ or Javac, and whether to process other operations using ECJ or DOM. Note that setting only one of those properties can be useful when developing one particular aspect of this proposal, which property to set depends on what you want to focus on.



🥼 **This experiment** here currently mostly involves/support some IDE features thanks for the following design:
* Refactoring ASTParser to allow delegating parsing/resolution to Javac instead of ECJ (system property `ICompilationUnitResolver` defines which parser to use). The Javac-based implementation is defined in the separate `org.eclipse.jdt.core.javac` fragment (so `org.eclipse.jdt.core` has no particular extra dependency on Javac by default) and consists mainly of
  * orchestrating Javac via its API and use its output in...
  * ...a converter from Javac diagnostics to JDT problems (then attached to the compilation unit)
  * ...a converter from Javac to JDT DOM (partial) and
  * ...a JavacBindingResolver relying on Javac "Symbol" to resolve types and references (partial)
* Some methods of the higher-level JDT "IDE" model such as reconciling model with source, or `codeSelect` or populating the index can now process on top of a built DOM directly, without invoking ECJ to re-parse the source (`CompilationUnit.DOM_BASED_OPERATIONS` system property controls whether to parse with ECJ, or use DOM; `CompilationUnit.codeComplete.DOM_BASED_OPERATIONS` specifically controls the Code Completion strategy). It doesn't matter whether the DOM originates from Javac or ECJ conversion, both should lead to same output from those higher-level methods. So these changes are independent of Javac experiment, they're just providing an alternative "DOM-first" strategy  for usual operations (where the only available strategy before was re-parsing/resolving with ECJ).

❓ What is known to be **not yet tried** to consider this experiment capable of getting on par with ECJ-based IDE:
* Support for **annotation processing**, which hopefully will be mostly a matter of looping the `parse` and `attr` steps of compilation with annotation processors, before running (binding) resolver
* Support for **.class generation**, which ideally would start from the javac context of the project, so only `gen` phase would be necessary as other ones such as parsing, resolution... already happened as the code was being edited


🏗️ What works as a proof of concept with no strong design issue known/left, but still requires work to be generally usable:
* about DOM production (use Javac APIs to generate DOM)
  * Complete Javac AST -> JDT DOM converter (estimated difficulty 💪💪💪)
  * Complete Javac AST/Symbols -> IBinding resolver (estimated difficulty 💪💪💪💪)
  * Map all Javac diagnostic types to JDT's IProblem (estimated difficulty 💪💪)
  * Forward all JDT compilerOptions/project configuration to configure Javac execution -currently only source path/class path configured (estimated difficulty 💪💪)
* about DOM consumption (plain JDT)
  * Complete DOM -> Index population (estimated difficulty 💪)
  * More support completion based on DOM: filtering, priority, missing constructs (estimated difficulty 💪💪💪💪)



🤔 What are the potential concerns:
* Currently, the AST is built more times than necessary, when we could just reuse the latest version.
* Memory cost of retaining Javac contexts (can we get rid of the context earlier? Can we share subparts of the concerns across multiple files in the project?...)
* The created `Javac` instances are not yet fully configured according to project settings.
* It seems hard to find reusable parts from the CompletionEngine, although many proposals shouldn't really depend on the parser (so should be reusable)


😧 What are the confirmed concerns:
* **Coupling with particular JDK version** the Javac "API" is not stable but changes between release. As a result a Javac-based JDT would become specific to 1 particular JDK version and would only run with this version (that why making it pluggable can really help).
* **Null analysis** seems to be tightly hooked in ECJ and cannot be used with Javac. Maybe let another analysis engine (eg SpotBugs) deal with it?
* At the moment, Javac cannot be configured to **generate .class despite CompilationError** in them like ECJ can do to allow updating the target application even when some code is not complete yet
  * We may actually be capable of hacking something like this in Eclipse/Javac integration (although it would be best to provide this directly in Javac), but running a 1st attempt of compilation, collecting errors, and then alterating the working copy of the source passed to Javac in case of error. More or less `if (diagnostics.anyMatch(getKind() == "error") { removeBrokenAST(diagnostic); injectAST("throw new CompilationError(diagnostic.getMessage()")`.
