package smc.implementers;

import org.junit.Before;
import org.junit.Test;
import smc.StateMachine;
import smc.generators.nestedSwitchCaseGenerator.NSCGenerator;
import smc.lexer.Lexer;
import smc.optimizer.Optimizer;
import smc.parser.Parser;
import smc.parser.SyntaxBuilder;
import smc.semanticAnalyzer.AbstractSyntaxTree;
import smc.semanticAnalyzer.SemanticAnalyzer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static smc.parser.ParserEvent.EOF;

public class JavaNestedSwitchCaseImplementerTest {
  private Lexer lexer;
   private Parser parser;
   private SyntaxBuilder builder;
   private SemanticAnalyzer analyzer;
   private Optimizer optimizer;
   private NSCGenerator generator;

   @Before
   public void setUp() throws Exception {
     builder = new SyntaxBuilder();
     parser = new Parser(builder);
     lexer = new Lexer(parser);
     analyzer = new SemanticAnalyzer();
     optimizer = new Optimizer();
     generator = new NSCGenerator();
   }

   private StateMachine produceStateMachine(String fsmSyntax) {
     lexer.lex(fsmSyntax);
     parser.handleEvent(EOF, -1, -1);
     AbstractSyntaxTree ast = analyzer.analyze(builder.getFsm());
     return optimizer.optimize(ast);
   }


   private void assertGenerated(String stt, String switchCase) {
     StateMachine sm = produceStateMachine(stt);
     JavaNestedSwitchCaseImplementer implementer = new JavaNestedSwitchCaseImplementer();
     generator.generate(sm).accept(implementer);
     assertThat(implementer.getOutput(), equalTo(switchCase));
   }

  @Test
  public void oneTransition() throws Exception {
    assertGenerated(
      "" +
        "Initial: I\n" +
        "Fsm: fsm\n" +
        "Actions: acts\n" +
        "{" +
        "  I E I A" +
        "}",
      "" +
        "public class fsm implements acts {\n" +
        "private enum State {I}\n" +
        "private enum Event {E}\n" +
        "State state = State.I;\n" +
        "void setState(State s) {state = s;}\n" +
        "void E() {handleEvent(Event.E);}\n" +
        "void handleEvent(Event event) {\n" +
        "switch(state) {\n" +
        "case I:\n" +
        "switch(event) {\n" +
        "case E:\n" +
        "setState(State.I);\n" +
        "A();\n" +
        "break;\n" +
        "}\n" +
        "break;\n" +
        "}\n" +
        "}\n" +
        "}\n");
  }
}
