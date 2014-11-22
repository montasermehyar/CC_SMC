package smc.implementers;

import org.junit.Before;
import org.junit.Test;
import smcsrc.smc.StateMachine;
import smcsrc.smc.generators.nestedSwitchCaseGenerator.NSCGenerator;
import smcsrc.smc.implementers.JavaNestedSwitchCaseImplementer;
import smcsrc.smc.lexer.Lexer;
import smcsrc.smc.optimizer.Optimizer;
import smcsrc.smc.parser.Parser;
import smcsrc.smc.parser.SyntaxBuilder;
import smcsrc.smc.semanticAnalyzer.AbstractSyntaxTree;
import smcsrc.smc.semanticAnalyzer.SemanticAnalyzer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static smcsrc.smc.parser.ParserEvent.EOF;

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
        "public abstract class fsm implements acts {\n" +
        "public abstract void unhandledTransition(String state, String event);\n" +
        "private enum State {I}\n" +
        "private enum Event {E}\n" +
        "private State state = State.I;\n" +
        "private void setState(State s) {state = s;}\n" +
        "public void E() {handleEvent(Event.E);}\n" +
        "private void handleEvent(Event event) {\n" +
        "switch(state) {\n" +
        "case I:\n" +
        "switch(event) {\n" +
        "case E:\n" +
        "setState(State.I);\n" +
        "A();\n" +
        "break;\n" +
        "default: unhandledTransition(state.name(), event.name()); break;\n" +
        "}\n" +
        "break;\n" +
        "}\n" +
        "}\n" +
        "}\n");
  }
}